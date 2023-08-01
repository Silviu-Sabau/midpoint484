/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.certification.impl;

import static com.evolveum.midpoint.schema.util.CertCampaignTypeUtil.norm;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType.*;

import java.util.*;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.schema.util.AccessCertificationWorkItemId;

import com.evolveum.midpoint.security.enforcer.api.ValueAuthorizationParameters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.evolveum.midpoint.certification.api.AccessCertificationEventListener;
import com.evolveum.midpoint.certification.api.CertificationManager;
import com.evolveum.midpoint.certification.api.OutcomeUtils;
import com.evolveum.midpoint.certification.impl.handlers.CertificationHandler;
import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.builder.S_FilterEntry;
import com.evolveum.midpoint.prism.query.builder.S_FilterExit;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.CertCampaignTypeUtil;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.security.api.SecurityContextManager;
import com.evolveum.midpoint.security.enforcer.api.AuthorizationParameters;
import com.evolveum.midpoint.security.enforcer.api.SecurityEnforcer;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

/**
 * All operations carried out by CertificationManager have to be authorized by it. ModelController does NOT execute
 * any authorizations before passing method calls to this module.
 *
 * All repository read operations are invoked on repository cache (even if we currently don't enter the cache).
 *
 * All write operations are passed to model, with the flag of preAuthorized set to TRUE (and currently in raw mode).
 * The reason is that we want the changes to be audited.
 *
 * In the future, the raw mode could be eventually changed to non-raw, in order to allow e.g. lower-level notifications
 * to be sent (that means, for example, notifications related to changing certification campaign as a result of carrying
 * out open/close stage operations). But currently we are satisfied with notifications that are emitted by the
 * certification module itself.
 *
 * Also, in the future, we could do some higher-level audit by this module. But for now we are OK with the lower-level
 * audit generated by the model.
 *
 * TODO: consider the enormous size of audit events in case of big campaigns (e.g. thousands or tens of thousands
 * certification cases).
 */
@Service(value = "certificationManager")
public class CertificationManagerImpl implements CertificationManager {

    private static final Trace LOGGER = TraceManager.getTrace(CertificationManagerImpl.class);

    private static final String INTERFACE_DOT = CertificationManager.class.getName() + ".";
    //public static final String CLASS_DOT = CertificationManagerImpl.class.getName() + ".";
    private static final String OPERATION_CREATE_CAMPAIGN = INTERFACE_DOT + "createCampaign";
    private static final String OPERATION_CREATE_AD_HOC_CAMPAIGNS = INTERFACE_DOT + "createAdHocCampaigns";
    private static final String OPERATION_OPEN_NEXT_STAGE = INTERFACE_DOT + "openNextStage";
    private static final String OPERATION_CLOSE_CURRENT_STAGE = INTERFACE_DOT + "closeCurrentStage";
    private static final String OPERATION_RECORD_DECISION = INTERFACE_DOT + "recordDecision";
    private static final String OPERATION_CLOSE_CAMPAIGN = INTERFACE_DOT + "closeCampaign";
    private static final String OPERATION_REITERATE_CAMPAIGN = INTERFACE_DOT + "reiterateCampaign";
    private static final String OPERATION_DELEGATE_WORK_ITEMS = INTERFACE_DOT + "delegateWorkItems";
    private static final String OPERATION_GET_CAMPAIGN_STATISTICS = INTERFACE_DOT + "getCampaignStatistics";
    private static final String OPERATION_CLEANUP_CAMPAIGNS = INTERFACE_DOT + "cleanupCampaigns";

    @Autowired private PrismContext prismContext;
    @Autowired @Qualifier("cacheRepositoryService") private RepositoryService repositoryService;
    @Autowired private ModelService modelService;
    @Autowired protected SecurityEnforcer securityEnforcer;
    @Autowired protected SecurityContextManager securityContextManager;
    @Autowired protected AccCertGeneralHelper generalHelper;
    @Autowired protected AccCertEventHelper eventHelper;
    @Autowired protected AccCertQueryHelper queryHelper;
    @Autowired protected AccCertUpdateHelper updateHelper;
    @Autowired protected AccCertOpenerHelper openerHelper;
    @Autowired protected AccCertCloserHelper closerHelper;
    @Autowired protected AccCertCaseOperationsHelper operationsHelper;
    @Autowired private AccessCertificationRemediationTaskHandler remediationTaskHandler;
    @Autowired private AccessCertificationClosingTaskHandler closingTaskHandler;

    private final Map<String,CertificationHandler> registeredHandlers = new HashMap<>();

    public void registerHandler(String handlerUri, CertificationHandler handler) {
        if (registeredHandlers.containsKey(handlerUri)) {
            throw new IllegalStateException("There is already a handler with URI " + handlerUri);
        }
        registeredHandlers.put(handlerUri, handler);
    }

    CertificationHandler findCertificationHandler(AccessCertificationCampaignType campaign) {
        if (StringUtils.isBlank(campaign.getHandlerUri())) {
            throw new IllegalArgumentException(
                    "No handler URI for access certification campaign " + ObjectTypeUtil.toShortString(campaign));
        }
        CertificationHandler handler = registeredHandlers.get(campaign.getHandlerUri());
        if (handler == null) {
            throw new IllegalStateException("No handler for URI " + campaign.getHandlerUri());
        }
        return handler;
    }

    @Override
    public AccessCertificationCampaignType createCampaign(String definitionOid, Task task, OperationResult parentResult)
            throws SchemaException, SecurityViolationException, ObjectNotFoundException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        Validate.notNull(definitionOid, "definitionOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CREATE_CAMPAIGN);
        try {
            PrismObject<AccessCertificationDefinitionType> definition =
                    repositoryService.getObject(AccessCertificationDefinitionType.class, definitionOid, null, result);
            securityEnforcer.authorize(
                    ModelAuthorizationAction.CREATE_CERTIFICATION_CAMPAIGN.getUrl(),
                    null,
                    AuthorizationParameters.Builder.buildObject(definition),
                    task,
                    result);
            return openerHelper.createCampaign(definition, result, task);
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't create certification campaign: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    /**
     * This is an action that can be run in unprivileged context. No authorizations are checked.
     * Take care when and where you call it. Child result is intentionally created only when a certification campaign
     * is to be started (to avoid useless creation of many empty records)
     */
    <O extends ObjectType> void startAdHocCertifications(PrismObject<O> focus,
            List<CertificationPolicyActionType> actions, Task task, OperationResult parentResult)
            throws SchemaException, ObjectNotFoundException, CommunicationException, ConfigurationException,
            SecurityViolationException, ExpressionEvaluationException {
        Set<String> definitionOids = new HashSet<>();
        for (CertificationPolicyActionType action : actions) {
            if (action.getDefinitionRef() != null) {
                for (ObjectReferenceType definitionRef : action.getDefinitionRef()) {
                    if (definitionRef.getOid() != null) {
                        definitionOids.add(definitionRef.getOid());
                    } else {
                        // TODO resolve dynamic reference
                        LOGGER.warn(
                                "Certification action having definition reference with no OID; the reference will be ignored: {}",
                                definitionRef);
                    }
                }
            } else {
                LOGGER.warn("Certification action without definition reference; will be ignored: {}", action);
            }
        }
        if (!definitionOids.isEmpty()) {
            OperationResult result = parentResult.createSubresult(OPERATION_CREATE_AD_HOC_CAMPAIGNS);
            result.addParam("focus", focus);
            result.addArbitraryObjectCollectionAsParam("definitionOids", definitionOids);
            try {
                PrismObject<UserType> administrator = repositoryService
                        .getObject(UserType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), null, result);
                securityContextManager.runAs(
                        (lResult) -> { // TODO reconsider this "runAs"
                            for (String definitionOid : definitionOids) {
                                startAdHocCertification(focus, definitionOid, task, lResult);
                            }
                            return null;
                        },
                        administrator,
                        false,
                        result);
            } catch (Throwable e) {
                result.recordException(e);
                throw e;
            } finally {
                result.close();
            }
        }
    }

    private <O extends ObjectType> void startAdHocCertification(
            PrismObject<O> focus, String definitionOid, Task task, OperationResult result) {
        try {
            AccessCertificationDefinitionType definition =
                    repositoryService
                            .getObject(AccessCertificationDefinitionType.class, definitionOid, null, result)
                            .asObjectable();
            AccessCertificationCampaignType newCampaign = openerHelper.createAdHocCampaignObject(definition, focus, task, result);
            updateHelper.addObjectPreAuthorized(newCampaign, task, result);
            openNextStage(newCampaign.getOid(), task, result);
        } catch (CommonException | RuntimeException e) {
            result.recordException("Couldn't create ad-hoc certification campaign: " + e.getMessage(), e);
            // Wrapping because of "runAs" in the caller -- TODO reconsider
            throw new SystemException("Couldn't create ad-hoc certification campaign: " + e.getMessage(), e);
        }
    }

    @Override
    public void openNextStage(@NotNull String campaignOid, @NotNull Task task, @NotNull OperationResult parentResult)
            throws SchemaException, SecurityViolationException, ObjectNotFoundException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        OperationResult result = parentResult.createSubresult(OPERATION_OPEN_NEXT_STAGE);
        result.addParam("campaignOid", campaignOid);

        try {
            AccessCertificationCampaignType campaign = generalHelper.getCampaign(campaignOid, null, task, result);
            result.addParam("campaign", ObjectTypeUtil.toShortString(campaign));

            LOGGER.debug("openNextStage starting for {}", ObjectTypeUtil.toShortStringLazy(campaign));

            securityEnforcer.authorize(
                    ModelAuthorizationAction.OPEN_CERTIFICATION_CAMPAIGN_REVIEW_STAGE.getUrl(), null,
                    AuthorizationParameters.Builder.buildObject(campaign.asPrismObject()), task, result);

            final int currentStageNumber = campaign.getStageNumber();
            final int stages = CertCampaignTypeUtil.getNumberOfStages(campaign);
            final AccessCertificationCampaignStateType state = campaign.getState();
            LOGGER.trace("openNextStage: iteration={}, currentStageNumber={}, stages={}, state={}", norm(campaign.getIteration()), currentStageNumber, stages, state);
            if (IN_REVIEW_STAGE.equals(state)) {
                result.recordFatalError("Couldn't advance to the next review stage as the stage " + currentStageNumber + " is currently open.");
            } else if (IN_REMEDIATION.equals(state)) {
                result.recordFatalError("Couldn't advance to the next review stage as the campaign is currently in the remediation phase.");
            } else if (CLOSED.equals(state)) {
                result.recordFatalError("Couldn't advance to the next review stage as the campaign is already closed.");
            } else if (!REVIEW_STAGE_DONE.equals(state) && !CREATED.equals(state)) {
                throw new IllegalStateException("Unexpected campaign state: " + state);
            } else if (currentStageNumber >= stages) {
                result.recordFatalError(
                        "Couldn't advance to the next review stage as the campaign has only " + stages + " stages");
            } else {
                CertificationHandler handler = findCertificationHandler(campaign);
                openerHelper.openNextStage(campaign, handler, task, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't move to the next certification campaign stage: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public void closeCurrentStage(String campaignOid, Task task, OperationResult parentResult)
            throws SchemaException, SecurityViolationException, ObjectNotFoundException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CLOSE_CURRENT_STAGE);
        result.addParam("campaignOid", campaignOid);

        try {
            AccessCertificationCampaignType campaign = generalHelper.getCampaign(campaignOid, null, task, result);
            result.addParam("campaign", ObjectTypeUtil.toShortString(campaign));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("closeCurrentStage starting for {}", ObjectTypeUtil.toShortString(campaign));
            }

            securityEnforcer.authorize(
                    ModelAuthorizationAction.CLOSE_CERTIFICATION_CAMPAIGN_REVIEW_STAGE.getUrl(), null,
                    AuthorizationParameters.Builder.buildObject(campaign.asPrismObject()), task, result);

            final int currentStageNumber = campaign.getStageNumber();
            final int stages = CertCampaignTypeUtil.getNumberOfStages(campaign);
            final AccessCertificationCampaignStateType state = campaign.getState();
            LOGGER.trace("closeCurrentStage: currentStageNumber={}, stages={}, state={}", currentStageNumber, stages, state);

            if (!IN_REVIEW_STAGE.equals(state)) {
                result.recordFatalError("Couldn't close the current review stage as it is currently not open");
            } else {
                closerHelper.closeStage(campaign, task, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't close current certification campaign stage: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public void startRemediation(String campaignOid, Task task, OperationResult parentResult)
            throws ObjectNotFoundException, SchemaException, SecurityViolationException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CLOSE_CURRENT_STAGE);
        result.addParam("campaignOid", campaignOid);

        try {
            AccessCertificationCampaignType campaign = generalHelper.getCampaign(campaignOid, null, task, result);
            result.addParam("campaign", ObjectTypeUtil.toShortString(campaign));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("startRemediation starting for {}", ObjectTypeUtil.toShortString(campaign));
            }

            securityEnforcer.authorize(
                    ModelAuthorizationAction.START_CERTIFICATION_REMEDIATION.getUrl(), null,
                    AuthorizationParameters.Builder.buildObject(campaign.asPrismObject()), task, result);

            final int currentStageNumber = campaign.getStageNumber();
            final int lastStageNumber = CertCampaignTypeUtil.getNumberOfStages(campaign);
            final AccessCertificationCampaignStateType state = campaign.getState();
            LOGGER.trace("startRemediation: currentStageNumber={}, stages={}, state={}", currentStageNumber, lastStageNumber, state);

            if (currentStageNumber != lastStageNumber) {
                result.recordFatalError("Couldn't start the remediation as the campaign is not in its last stage ("+lastStageNumber+"); current stage: "+currentStageNumber);
            } else if (!REVIEW_STAGE_DONE.equals(state)) {
                result.recordFatalError("Couldn't start the remediation as the last stage was not properly closed.");
            } else {
                List<ItemDelta<?,?>> deltas = updateHelper.createDeltasForStageNumberAndState(lastStageNumber + 1, IN_REMEDIATION);
                updateHelper.modifyObjectPreAuthorized(AccessCertificationCampaignType.class, campaignOid, deltas, task, result);

                if (CertCampaignTypeUtil.isRemediationAutomatic(campaign)) {
                    remediationTaskHandler.launch(campaign, result);
                } else {
                    result.recordWarning("The automated remediation is not configured. The campaign state was set to IN REMEDIATION, but all remediation actions have to be done by hand.");
                }

                campaign = updateHelper.refreshCampaign(campaign, result);
                eventHelper.onCampaignStageStart(campaign, task, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't start the remediation: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public void recordDecision(
            @NotNull AccessCertificationWorkItemId workItemId,
            @Nullable AccessCertificationResponseType response,
            @Nullable String comment,
            boolean preAuthorized,
            Task task,
            OperationResult parentResult)
            throws ObjectNotFoundException, SchemaException, SecurityViolationException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {

        OperationResult result = parentResult.createSubresult(OPERATION_RECORD_DECISION);
        try {
            var workItemInContext = queryHelper.getWorkItemInContext(workItemId, result);
            if (!preAuthorized) {
                securityEnforcer.authorize(
                        ModelAuthorizationAction.COMPLETE_WORK_ITEM.getUrl(),
                        null,
                        ValueAuthorizationParameters.of(workItemInContext.workItem()),
                        task, result);
            }
            operationsHelper.recordDecision(workItemId, workItemInContext, response, comment, task, result);
        } catch (Throwable t) {
            result.recordException(t);
            throw t;
        } finally {
            result.close();
        }
    }

    public void delegateWorkItems(
            @NotNull String campaignOid, @NotNull List<AccessCertificationWorkItemType> workItems,
            @NotNull DelegateWorkItemActionType delegateAction, Task task, OperationResult parentResult)
            throws SchemaException, SecurityViolationException, ExpressionEvaluationException, ObjectNotFoundException,
            ObjectAlreadyExistsException, ConfigurationException, CommunicationException {
        OperationResult result = parentResult.createSubresult(OPERATION_DELEGATE_WORK_ITEMS);
        result.addParam("campaignOid", campaignOid);
        result.addArbitraryObjectCollectionAsParam("workItems", workItems); // TODO only IDs?
        result.addArbitraryObjectAsParam("delegateAction", delegateAction);
        try {
            for (AccessCertificationWorkItemType workItem : workItems) {
                securityEnforcer.authorize(
                        ModelAuthorizationAction.DELEGATE_WORK_ITEM.getUrl(),
                        null,
                        ValueAuthorizationParameters.of(workItem),
                        task, result);
            }
            operationsHelper.delegateWorkItems(campaignOid, workItems, delegateAction, task, result);
        } catch (RuntimeException|CommonException e) {
            result.recordFatalError("Couldn't delegate work items: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public void closeCampaign(String campaignOid, Task task, OperationResult parentResult)
            throws ObjectNotFoundException, SchemaException, SecurityViolationException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        closeCampaign(campaignOid, false, task, parentResult);
    }

    // the parameter noBackgroundTask is used only for testing purposes
    public void closeCampaign(String campaignOid, boolean noBackgroundTask, Task task, OperationResult parentResult)
            throws ObjectNotFoundException, SchemaException, SecurityViolationException, ObjectAlreadyExistsException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CLOSE_CAMPAIGN);

        try {
            AccessCertificationCampaignType campaign = generalHelper.getCampaign(campaignOid, null, task, result);
            securityEnforcer.authorize(
                    ModelAuthorizationAction.CLOSE_CERTIFICATION_CAMPAIGN.getUrl(), null,
                    AuthorizationParameters.Builder.buildObject(campaign.asPrismObject()), task, result);
            closerHelper.closeCampaign(campaign, task, result);
            if (!noBackgroundTask) {
                closingTaskHandler.launch(campaign, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't close certification campaign: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public void reiterateCampaign(String campaignOid, Task task, OperationResult parentResult) throws ObjectNotFoundException,
            SchemaException, SecurityViolationException, ObjectAlreadyExistsException, ExpressionEvaluationException,
            CommunicationException, ConfigurationException {
        OperationResult result = parentResult.createSubresult(OPERATION_REITERATE_CAMPAIGN);
        try {
            AccessCertificationCampaignType campaign = generalHelper.getCampaign(campaignOid, null, task, result);
            securityEnforcer.authorize(
                    ModelAuthorizationAction.REITERATE_CERTIFICATION_CAMPAIGN.getUrl(), null,
                    AuthorizationParameters.Builder.buildObject(campaign.asPrismObject()), task, result);
            openerHelper.reiterateCampaign(campaign, task, result);
        } catch (RuntimeException e) {
            result.recordException("Couldn't reiterate certification campaign: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.close();
        }
    }


    // this method delegates the authorization to the model
    @Override
    public AccessCertificationCasesStatisticsType getCampaignStatistics(String campaignOid, boolean currentStageOnly, Task task,
            OperationResult parentResult) throws ObjectNotFoundException, SchemaException, SecurityViolationException,
            ExpressionEvaluationException, CommunicationException, ConfigurationException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_GET_CAMPAIGN_STATISTICS);
        try {
            AccessCertificationCampaignType campaign;
            try {
                campaign = modelService.getObject(AccessCertificationCampaignType.class, campaignOid, null, task, parentResult).asObjectable();
            } catch (CommunicationException|ConfigurationException|ExpressionEvaluationException e) {
                throw new SystemException("Unexpected exception while getting campaign object: " + e.getMessage(), e);
            }

            Integer stage = currentStageOnly ? campaign.getStageNumber() : null;

            AccessCertificationCasesStatisticsType stat = new AccessCertificationCasesStatisticsType();

            stat.setMarkedAsAccept(getCount(campaignOid, stage, AccessCertificationResponseType.ACCEPT, false, task, result));
            stat.setMarkedAsRevoke(getCount(campaignOid, stage, AccessCertificationResponseType.REVOKE, false, task, result));
            stat.setMarkedAsRevokeAndRemedied(getCount(campaignOid, stage, AccessCertificationResponseType.REVOKE, true, task, result));
            stat.setMarkedAsReduce(getCount(campaignOid, stage, AccessCertificationResponseType.REDUCE, false, task, result));
            stat.setMarkedAsReduceAndRemedied(getCount(campaignOid, stage, AccessCertificationResponseType.REDUCE, true, task, result));
            stat.setMarkedAsNotDecide(getCount(campaignOid, stage, AccessCertificationResponseType.NOT_DECIDED, false, task, result));
            stat.setWithoutResponse(getCount(campaignOid, stage, AccessCertificationResponseType.NO_RESPONSE, false, task, result));
            return stat;
        } catch (RuntimeException e) {
            result.recordException("Couldn't get campaign statistics: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.close();
        }
    }

    private int getCount(String campaignOid, Integer stage, AccessCertificationResponseType response, boolean onlyRemedied, Task task,
            OperationResult result) throws SchemaException, SecurityViolationException, ObjectNotFoundException, ExpressionEvaluationException, CommunicationException, ConfigurationException {
        QName outcomeItem;
        String responseUri = OutcomeUtils.toUri(response);
        S_FilterEntry entry;
        if (stage != null) {
            outcomeItem = AccessCertificationCaseType.F_CURRENT_STAGE_OUTCOME;
            entry = prismContext.queryFor(AccessCertificationCaseType.class)
                    .item(AccessCertificationCaseType.F_STAGE_NUMBER).eq(stage)
                    .and();
        } else {
            outcomeItem = AccessCertificationCaseType.F_OUTCOME;
            entry = prismContext.queryFor(AccessCertificationCaseType.class);
        }
        S_FilterExit exit;
        if (response == AccessCertificationResponseType.NO_RESPONSE) {
            exit = entry.item(outcomeItem).isNull().or().item(outcomeItem).eq(responseUri);
        } else {
            exit = entry.item(outcomeItem).eq(responseUri);
        }
        if (onlyRemedied) {
            exit = exit.and().not().item(AccessCertificationCaseType.F_REMEDIED_TIMESTAMP).isNull();
        }
        exit = exit.and().ownerId(campaignOid);
        ObjectQuery query = exit.build();
        return modelService.countContainers(AccessCertificationCaseType.class, query, null, task, result);
    }

    @Override
    public void registerCertificationEventListener(AccessCertificationEventListener listener) {
        eventHelper.registerEventListener(listener);
    }

    @Override
    public void cleanupCampaigns(@NotNull CleanupPolicyType policy, Task task, OperationResult parentResult) {
        OperationResult result = parentResult.createSubresult(OPERATION_CLEANUP_CAMPAIGNS);
        try {
            closerHelper.cleanupCampaigns(policy, task, result);
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't cleanup campaigns: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }
}
