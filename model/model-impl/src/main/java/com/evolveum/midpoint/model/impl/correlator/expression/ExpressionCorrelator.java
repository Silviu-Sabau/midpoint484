/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.model.impl.correlator.expression;

import com.evolveum.midpoint.model.api.correlator.CorrelationContext;
import com.evolveum.midpoint.model.api.correlator.CorrelationResult;
import com.evolveum.midpoint.model.api.correlator.Correlator;
import com.evolveum.midpoint.model.common.expression.ModelExpressionThreadLocalHolder;
import com.evolveum.midpoint.model.impl.ModelBeans;
import com.evolveum.midpoint.model.impl.correlator.CorrelatorUtil;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.PrismValueDeltaSetTriple;
import com.evolveum.midpoint.repo.common.expression.Expression;
import com.evolveum.midpoint.repo.common.expression.ExpressionEvaluationContext;
import com.evolveum.midpoint.schema.constants.ExpressionConstants;
import com.evolveum.midpoint.schema.expression.ExpressionProfile;
import com.evolveum.midpoint.schema.expression.VariablesMap;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.MiscSchemaUtil;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.evolveum.midpoint.util.DebugUtil.lazy;

/**
 * A correlator based on expressions that directly provide focal object(s) (or their references) for given resource object.
 * Similar to synchronization sorter, but simpler - it treats only correlation, not the classification part.
 */
class ExpressionCorrelator implements Correlator {

    private static final Trace LOGGER = TraceManager.getTrace(ExpressionCorrelator.class);

    /**
     * Configuration of the correlator.
     */
    @NotNull private final ExpressionCorrelatorType configuration;

    /** Useful beans. */
    @NotNull private final ModelBeans beans;

    ExpressionCorrelator(@NotNull ExpressionCorrelatorType configuration, @NotNull ModelBeans beans) {
        this.configuration = configuration;
        this.beans = beans;
        LOGGER.trace("Instantiated the correlator with the configuration:\n{}", configuration.debugDumpLazily(1));
    }

    @Override
    public CorrelationResult correlate(
            @NotNull ShadowType resourceObject,
            @NotNull CorrelationContext correlationContext,
            @NotNull Task task,
            @NotNull OperationResult result)
            throws SchemaException, ExpressionEvaluationException, CommunicationException, SecurityViolationException,
            ConfigurationException, ObjectNotFoundException {

        LOGGER.trace("Correlating the resource object:\n{}\nwith context:\n{}",
                resourceObject.debugDumpLazily(1),
                correlationContext.debugDumpLazily(1));

        return new Correlation(resourceObject, correlationContext, task)
                .execute(result);
    }

    private class Correlation {

        @NotNull private final ShadowType resourceObject;
        @NotNull private final CorrelationContext correlationContext;
        @NotNull private final Task task;
        @NotNull private final String contextDescription;
        /** TODO: determine from the resource */
        @Nullable private final ExpressionProfile expressionProfile = MiscSchemaUtil.getExpressionProfile();

        Correlation(
                @NotNull ShadowType resourceObject,
                @NotNull CorrelationContext correlationContext,
                @NotNull Task task) {
            this.resourceObject = resourceObject;
            this.correlationContext = correlationContext;
            this.task = task;
            this.contextDescription =
                    ("expression correlator" +
                            (configuration.getName() != null ? " '" + configuration.getName() + "'" : ""))
                            + " for " + correlationContext.getObjectTypeDefinition().getHumanReadableName()
                            + " in " + correlationContext.getResource();
        }

        public CorrelationResult execute(OperationResult result)
                throws SchemaException, ExpressionEvaluationException, CommunicationException, SecurityViolationException,
                ConfigurationException, ObjectNotFoundException {
            return CorrelatorUtil.getCorrelationResultFromReferences(
                    findCandidatesUsingExpressions(result));
        }

        private @NotNull List<ObjectReferenceType> findCandidatesUsingExpressions(OperationResult result)
                throws SchemaException, ObjectNotFoundException, ExpressionEvaluationException, CommunicationException,
                ConfigurationException, SecurityViolationException {

            ExpressionType expressionBean;
            ItemDefinition<?> outputDefinition;
            if (configuration.getOwner() != null) {
                if (configuration.getOwnerRef() != null) {
                    throw new ConfigurationException("Both owner and ownerRef expressions found in " + contextDescription);
                }
                expressionBean = configuration.getOwner();
                outputDefinition =
                        Objects.requireNonNull(
                                PrismContext.get().getSchemaRegistry()
                                        .findObjectDefinitionByCompileTimeClass(correlationContext.getFocusType()),
                                () -> "No definition for focus type " + correlationContext.getFocusType());
            } else {
                if (configuration.getOwnerRef() == null) {
                    throw new ConfigurationException("Neither owner nor ownerRef expression found in " + contextDescription);
                }
                expressionBean = configuration.getOwnerRef();
                outputDefinition =
                        PrismContext.get().definitionFactory().createReferenceDefinition(
                                ExpressionConstants.OUTPUT_ELEMENT_NAME, ObjectReferenceType.COMPLEX_TYPE);
            }

            Expression<PrismValue, ItemDefinition<?>> expression =
                    beans.expressionFactory.makeExpression(
                            expressionBean, outputDefinition, expressionProfile, contextDescription, task, result);

            VariablesMap variables = getVariablesMap();
            ExpressionEvaluationContext params =
                    new ExpressionEvaluationContext(null, variables, contextDescription, task);
            PrismValueDeltaSetTriple<?> outputTriple = ModelExpressionThreadLocalHolder
                    .evaluateAnyExpressionInContext(expression, params, task, result);
            LOGGER.trace("Correlation expression returned:\n{}", DebugUtil.debugDumpLazily(outputTriple, 1));

            List<ObjectReferenceType> allCandidates = new ArrayList<>();
            if (outputTriple != null) {
                for (PrismValue candidateValue : outputTriple.getNonNegativeValues()) {
                    addCandidateOwner(allCandidates, candidateValue);
                }
            }

            LOGGER.debug("Found {} owner candidates for {} using correlation expression in {}: {}",
                    allCandidates.size(), resourceObject, contextDescription,
                    lazy(() -> PrettyPrinter.prettyPrint(allCandidates, 3)));

            return allCandidates;
        }

        private void addCandidateOwner(List<ObjectReferenceType> allCandidates, PrismValue candidateValue)
                throws SchemaException {
            if (candidateValue == null) {
                return;
            }
            ObjectReferenceType reference;
            if (candidateValue instanceof PrismObjectValue) {
                reference = ObjectTypeUtil.createObjectRef(((PrismObjectValue<?>) candidateValue).asPrismObject());
            } else if (candidateValue instanceof PrismReferenceValue) {
                reference = ObjectTypeUtil.createObjectRef((PrismReferenceValue) candidateValue);
            } else {
                throw new IllegalStateException("Unexpected return value " + MiscUtil.getValueWithClass(candidateValue)
                        + " from correlation script in " + contextDescription);
            }

            String oid = reference.getOid();
            if (oid == null) {
                // Or other kind of exception?
                throw new SchemaException("No OID found in value returned from correlation script. Value: "
                        + candidateValue + " in: " + contextDescription);
            }
            if (containsReferenceWithOid(allCandidates, oid)) {
                LOGGER.trace("Candidate owner {} already processed", candidateValue);
            } else {
                LOGGER.trace("Adding {} to the list of candidate owners", candidateValue);
                allCandidates.add(reference);
            }
        }

        private boolean containsReferenceWithOid(List<ObjectReferenceType> allCandidates, String oid) {
            for (ObjectReferenceType existing : allCandidates) {
                if (existing.getOid().equals(oid)) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        private VariablesMap getVariablesMap() {
            return CorrelatorUtil.getVariablesMap(null, resourceObject, correlationContext);
        }
    }
}
