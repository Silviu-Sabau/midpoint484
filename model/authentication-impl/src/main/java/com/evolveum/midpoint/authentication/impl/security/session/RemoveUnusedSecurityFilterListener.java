/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.authentication.impl.security.session;

import com.evolveum.midpoint.authentication.api.AuthModule;

import com.evolveum.midpoint.authentication.impl.security.MidpointAutowiredBeanFactoryObjectPostProcessor;

import com.evolveum.midpoint.authentication.impl.security.util.AuthModuleImpl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.stereotype.Component;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * @author skublik
 */

@Component
public class RemoveUnusedSecurityFilterListener  implements ApplicationListener<RemoveUnusedSecurityFilterEvent> {

    private static final Trace LOGGER = TraceManager.getTrace(RemoveUnusedSecurityFilterListener.class);

    @Autowired private ObjectPostProcessor<Object> objectObjectPostProcessor;

    @Override
    public void onApplicationEvent(RemoveUnusedSecurityFilterEvent event) {
        LOGGER.trace("Received spring RemoveUnusedSecurityFilterEvent event - " + event.getMpAuthentication());

        if (event.getMpAuthentication() != null && CollectionUtils.isNotEmpty(event.getMpAuthentication().getAuthModules())
                && objectObjectPostProcessor instanceof MidpointAutowiredBeanFactoryObjectPostProcessor) {
            for (AuthModule module : event.getMpAuthentication().getAuthModules()) {
                if (((AuthModuleImpl)module).getSecurityFilterChain() != null
                        && CollectionUtils.isNotEmpty(((AuthModuleImpl)module).getSecurityFilterChain().getFilters())) {
                    ((MidpointAutowiredBeanFactoryObjectPostProcessor)objectObjectPostProcessor).destroyAndRemoveFilters(
                            ((AuthModuleImpl)module).getSecurityFilterChain().getFilters());
                }
            }
        }

    }
}
