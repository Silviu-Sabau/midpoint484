/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.provisioning.api;

import com.evolveum.midpoint.schema.expression.ExpressionProfile;
import com.evolveum.midpoint.schema.util.ObjectDeltaSchemaLevelUtil;

public class ProvisioningOperationContext {

    private String requestIdentifier;

    private ExpressionProfile expressionProfile;

    private ObjectDeltaSchemaLevelUtil.NameResolver nameResolver;

    public String requestIdentifier() {
        return requestIdentifier;
    }

    public ProvisioningOperationContext requestIdentifier(String requestIdentifier) {
        this.requestIdentifier = requestIdentifier;
        return this;
    }

    public ExpressionProfile expressionProfile() {
        return expressionProfile;
    }

    public ProvisioningOperationContext expressionProfile(ExpressionProfile expressionProfile) {
        this.expressionProfile = expressionProfile;
        return this;
    }

    public ObjectDeltaSchemaLevelUtil.NameResolver nameResolver() {
        return nameResolver;
    }

    public ProvisioningOperationContext nameResolver(ObjectDeltaSchemaLevelUtil.NameResolver nameResolver) {
        this.nameResolver = nameResolver;
        return this;
    }
}
