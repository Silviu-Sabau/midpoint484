/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.admin.role.mining.page.panel.cluster;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.impl.page.admin.AbstractObjectMainPanel;
import com.evolveum.midpoint.gui.impl.page.admin.ObjectDetailsModels;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.tables.RoleAnalysisDetectedPatternTable;
import com.evolveum.midpoint.gui.impl.util.DetailsPageUtil;
import com.evolveum.midpoint.web.application.PanelDisplay;
import com.evolveum.midpoint.web.application.PanelInstance;
import com.evolveum.midpoint.web.application.PanelType;
import com.evolveum.midpoint.web.util.OnePageParameterEncoder;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ContainerPanelConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RoleAnalysisSessionType;

@PanelType(name = "topDetectedPattern")
@PanelInstance(
        identifier = "topDetectedPattern",
        applicableForType = RoleAnalysisSessionType.class,
        display = @PanelDisplay(
                label = "RoleAnalysisDetectionPatternType.action.suggestion",
                icon = GuiStyleConstants.CLASS_ICON_SEARCH,
                order = 30
        )
)
public class TopDetectedPatternPanel extends AbstractObjectMainPanel<RoleAnalysisSessionType, ObjectDetailsModels<RoleAnalysisSessionType>> {

    private static final String ID_CONTAINER = "container";
    private static final String ID_PANEL = "panelId";

    public TopDetectedPatternPanel(String id, ObjectDetailsModels<RoleAnalysisSessionType> model,
            ContainerPanelConfigurationType config) {
        super(id, model, config);
    }

    @Override
    protected void initLayout() {

        WebMarkupContainer container = new WebMarkupContainer(ID_CONTAINER);
        container.setOutputMarkupId(true);
        add(container);

        RoleAnalysisDetectedPatternTable components = loadTable();
        container.add(components);
    }

    @NotNull
    private RoleAnalysisDetectedPatternTable loadTable() {

        RoleAnalysisDetectedPatternTable components = new RoleAnalysisDetectedPatternTable(ID_PANEL, getObjectDetailsModels().getObjectType(), true) {
            @Override
            protected void onRefresh(AjaxRequestTarget target) {
                PageParameters parameters = new PageParameters();
                parameters.add(OnePageParameterEncoder.PARAMETER, getObjectDetailsModels().getObjectType().getOid());
                parameters.add(ID_PANEL, getPanelConfiguration().getIdentifier());
                Class<? extends PageBase> detailsPageClass = DetailsPageUtil
                        .getObjectDetailsPage(RoleAnalysisSessionType.class);
                getPageBase().navigateToNext(detailsPageClass, parameters);
            }
        };
        components.setOutputMarkupId(true);
        return components;
    }

    public PageBase getPageBase() {
        return ((PageBase) getPage());
    }

    protected RoleAnalysisDetectedPatternTable getTable() {
        return (RoleAnalysisDetectedPatternTable) get(((PageBase) getPage()).createComponentPath(ID_CONTAINER, ID_PANEL));
    }

}
