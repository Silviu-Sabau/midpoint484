/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.web.security;

import static com.evolveum.midpoint.security.api.AuthorizationConstants.*;
import static com.evolveum.midpoint.web.page.admin.configuration.PageAdminConfiguration.AUTH_CONFIGURATION_ALL;

import com.evolveum.midpoint.util.DisplayableValue;
import com.evolveum.midpoint.web.application.AuthorizationActionValue;
import com.evolveum.midpoint.web.page.admin.configuration.PageAdminConfiguration;

/**
 * @author lazyman
 */
public enum PageUrlMapping {

    ADMIN_USER_DETAILS("/admin/user/**",
            new AuthorizationActionValue(AUTZ_UI_USER_DETAILS_URL,
                    "PageAdminUsers.authUri.userDetails.label", "PageAdminUsers.authUri.userDetails.description"),
            new AuthorizationActionValue(AUTZ_UI_USERS_ALL_URL,
                    "PageAdminUsers.authUri.usersAll.label", "PageAdminUsers.authUri.usersAll.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageAdminUsers.authUri.usersAll.label", "PageAdminUsers.authUri.guiAll.description")),
    TASK_DETAILS("/admin/task/**",
            new AuthorizationActionValue(AUTZ_UI_TASK_DETAIL_URL,
                    "PageAdminTasks.authUri.taskDetails.label", "PageAdminTasks.authUri.taskDetails.description"),
            new AuthorizationActionValue(AUTZ_UI_TASKS_ALL_URL,
                    "PageAdminTasks.authUri.tasksAll.label", "PageAdminTasks.authUri.tasksAll.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageAdminTasks.authUri.tasksAll.label", "PageAdminTasks.authUri.guiAll.description")),
    ROLE_DETAILS("/admin/role/**",
            new AuthorizationActionValue(AUTZ_UI_ROLE_DETAILS_URL,
                    "PageAdminRoles.authUri.roleDetails.label", "PageAdminRoles.authUri.roleDetails.description"),
            new AuthorizationActionValue(AUTZ_UI_ROLES_ALL_URL,
                    "PageAdminRoles.authUri.rolesAll.label", "PageAdminRoles.authUri.rolesAll.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageAdminRoles.authUri.rolesAll.label", "PageAdminRoles.authUri.guiAll.description")),
    ORG_DETAILS("/admin/org/unit/**",
            new AuthorizationActionValue(AUTZ_UI_ORG_ALL_URL,
                    "PageAdminUsers.auth.orgAll.label", "PageAdminUsers.auth.orgAll.description"),
            new AuthorizationActionValue(AUTZ_UI_ORG_UNIT_URL,
                    "PageOrgUnit.auth.orgUnit.label", "PageOrgUnit.auth.orgUnit.description")),
    RESOURCE_DETAILS("/admin/resource/**",
            new AuthorizationActionValue(AUTZ_UI_RESOURCE_DETAILS_URL,
                    "PageAdminResources.authUri.resourceDetails.label", "PageAdminResources.authUri.resourceDetails.description"),
            new AuthorizationActionValue(AUTZ_UI_RESOURCES_ALL_URL,
                    "PageAdminResources.authUri.resourcesAll.label", "PageAdminResources.authUri.resourcesAll.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageAdminRoles.authUri.rolesAll.label", "PageAdminRoles.authUri.guiAll.description")),
    WORK_ITEM_DETAILS("/admin/workItem/**",
            new AuthorizationActionValue(AUTZ_UI_WORK_ITEM_URL,
                    "PageCaseWorkItem.authUri.workItemDetails.label", "PageCaseWorkItem.authUri.workItemDetails.description"),
            new AuthorizationActionValue(AUTZ_UI_WORK_ITEMS_ALL_URL,
                    "PageCaseWorkItems.authUri.workItemsAll.label", "PageAdminResources.authUri.workItemsAll.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageCaseWorkItems.authUri.guiAll.label", "PageAdminRoles.authUri.guiAll.description")),
    CASE_DETAILS("/admin/case/**",
            new AuthorizationActionValue(AUTZ_UI_CASES_ALL_URL,
                    "PageAdminCases.auth.casesAll.label", "PageAdminCases.auth.casesAll.description"),
            new AuthorizationActionValue(AUTZ_UI_CASE_URL,
                    "PageCase.auth.case.label", "PageCase.auth.case.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageCaseWorkItems.authUri.guiAll.label", "PageAdminRoles.authUri.guiAll.description")),
    OBJECT_COLLECTION_DETAILS("/admin/objectCollection/**",
            new AuthorizationActionValue(AUTH_CONFIGURATION_ALL,
                    PageAdminConfiguration.AUTH_CONFIGURATION_ALL_LABEL, PageAdminConfiguration.AUTH_CONFIGURATION_ALL_DESCRIPTION),
            new AuthorizationActionValue(AUTZ_UI_OBJECT_COLLECTIONS_ALL_URL,
                    "PageObjectCollection.auth.objectCollectionsAll.label", "PageObjectCollection.auth.objectCollectionsAll.description"),
            new AuthorizationActionValue(AUTZ_UI_OBJECT_COLLECTION_URL,
                    "PageObjectCollection.auth.objectCollection.label", "PageObjectCollection.auth.objectCollection.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageCaseWorkItems.authUri.guiAll.label", "PageAdminRoles.authUri.guiAll.description")),
    ARCHETYPE_DETAILS("/admin/archetype/**",
            new AuthorizationActionValue(AUTH_CONFIGURATION_ALL,
                    PageAdminConfiguration.AUTH_CONFIGURATION_ALL_LABEL, PageAdminConfiguration.AUTH_CONFIGURATION_ALL_DESCRIPTION),
            new AuthorizationActionValue(AUTZ_UI_ARCHETYPES_ALL_URL,
                    "PageArchetypes.auth.archetypesAll.label", "PageArchetypes.auth.archetypesAll.description"),
            new AuthorizationActionValue(AUTZ_UI_ARCHETYPE_URL,
                    "PageArchetype.auth.user.label", "PageArchetype.auth.archetype.description"),
            new AuthorizationActionValue(AUTZ_GUI_ALL_URL,
                    "PageCaseWorkItems.authUri.guiAll.label", "PageAdminRoles.authUri.guiAll.description")),
    ACTUATOR("/actuator/**",
            new AuthorizationActionValue(AUTZ_ACTUATOR_ALL_URL,
                    "ActuatorEndpoint.authActuator.all.label", "ActuatorEndpoint.authActuator.all.description")),
    ACTUATOR_THREAD_DUMP("/actuator/threaddump",
            new AuthorizationActionValue(AUTZ_ACTUATOR_THREAD_DUMP_URL,
                    "ActuatorEndpoint.authActuator.threadDump.label", "ActuatorEndpoint.authActuator.threadDump.description")),
    ACTUATOR_HEAP_DUMP("/actuator/heapdump",
            new AuthorizationActionValue(AUTZ_ACTUATOR_HEAP_DUMP_URL,
                    "ActuatorEndpoint.authActuator.heapDump.label", "ActuatorEndpoint.authActuator.heapDump.description")),
    ACTUATOR_ENV("/actuator/env/**",
            new AuthorizationActionValue(AUTZ_ACTUATOR_ENV_URL,
                    "ActuatorEndpoint.authActuator.env.label", "ActuatorEndpoint.authActuator.env.description")),
    ACTUATOR_INFO("/actuator/info",
            new AuthorizationActionValue(AUTZ_ACTUATOR_INFO_URL,
                    "ActuatorEndpoint.authActuator.info.label", "ActuatorEndpoint.authActuator.info.description")),
    ACTUATOR_METRICS("/actuator/metrics/**",
            new AuthorizationActionValue(AUTZ_ACTUATOR_METRICS_URL,
                    "ActuatorEndpoint.authActuator.metrics.label", "ActuatorEndpoint.authActuator.metrics.description")),
    REST("/ws/**",
            new AuthorizationActionValue(AUTZ_REST_ALL_URL,
                    "RestEndpoint.authRest.all.label", "RestEndpoint.authRest.all.description")),
    REST2("/rest/**",
            new AuthorizationActionValue(AUTZ_REST_ALL_URL,
                    "RestEndpoint.authRest.all.label", "RestEndpoint.authRest.all.description")),
    REST3("/api/**",
            new AuthorizationActionValue(AUTZ_REST_ALL_URL,
                    "RestEndpoint.authRest.all.label", "RestEndpoint.authRest.all.description"));

    private final String url;

    // final, but array is still mutable
    private final DisplayableValue[] action;

    PageUrlMapping(String url, DisplayableValue... action) {
        this.url = url;
        this.action = action;
    }

    public DisplayableValue[] getAction() {
        return action;
    }

    public String getUrl() {
        return url;
    }
}
