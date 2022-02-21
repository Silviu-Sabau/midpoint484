/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.admin.systemconfiguration.component;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.gui.impl.page.admin.AbstractObjectMainPanel;
import com.evolveum.midpoint.gui.impl.page.admin.assignmentholder.AssignmentHolderDetailsModel;
import com.evolveum.midpoint.gui.impl.prism.panel.SingleContainerPanel;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.web.application.PanelDisplay;
import com.evolveum.midpoint.web.application.PanelInstance;
import com.evolveum.midpoint.web.application.PanelInstances;
import com.evolveum.midpoint.web.application.PanelType;
import com.evolveum.midpoint.web.model.PrismContainerWrapperModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ContainerPanelConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProfilingConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;

@PanelType(name = "singleContainerPanel")
@PanelInstances(
        instances = {
                @PanelInstance(
                        identifier = "infrastructurePanel",
                        applicableForType = SystemConfigurationType.class,
                        display = @PanelDisplay(
                                label = "InfrastructureContentPanel.label",
                                icon = GuiStyleConstants.CLASS_CIRCLE_FULL,
                                order = 30
                        ),
                        containerPath = "infrastructure",
                        type = "InfrastructureConfigurationType"
                ),
                @PanelInstance(
                        identifier = "fullTextSearchPanel",
                        applicableForType = SystemConfigurationType.class,
                        display = @PanelDisplay(
                                label = "FullTextSearchPanel.label",
                                icon = GuiStyleConstants.CLASS_CIRCLE_FULL,
                                order = 40
                        ),
                        containerPath = "fullTextSearch",
                        type = "FullTextSearchConfigurationType"
                ),
                @PanelInstance(
                        identifier = "profilingPanel",
                        applicableForType = ProfilingConfigurationType.class,
                        display = @PanelDisplay(
                                label = "ProfilingConfiguration.label",
                                icon = GuiStyleConstants.CLASS_CIRCLE_FULL,
                                order = 10
                        ),
                        containerPath = "profilingConfiguration",
                        type = "ProfilingConfigurationType"
                )}
)
public class SingleContainerContentPanel extends AbstractObjectMainPanel<SystemConfigurationType, AssignmentHolderDetailsModel<SystemConfigurationType>> {

    private static final String ID_MAIN_PANEL = "mainPanel";

    public SingleContainerContentPanel(String id, AssignmentHolderDetailsModel<SystemConfigurationType> model, ContainerPanelConfigurationType config) {
        super(id, model, config);
    }

    @Override
    protected void initLayout() {
        ContainerPanelConfigurationType config = getPanelConfiguration();
        ItemPath path = config.getPath().getItemPath();
        QName type = config.getType();

        add(new SingleContainerPanel(ID_MAIN_PANEL, PrismContainerWrapperModel.fromContainerWrapper(getObjectWrapperModel(), path), type));
    }
}
