/*
 * Copyright (c) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.web.page.admin.certification.component;

import com.evolveum.midpoint.gui.api.component.Badge;
import com.evolveum.midpoint.gui.api.component.BadgePanel;
import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.component.button.DropdownButtonDto;
import com.evolveum.midpoint.gui.api.component.button.DropdownButtonPanel;
import com.evolveum.midpoint.gui.api.component.progressbar.ProgressBar;
import com.evolveum.midpoint.gui.api.component.progressbar.ProgressBarPanel;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.impl.page.admin.resource.component.TemplateTile;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.util.CertCampaignTypeUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.DateLabelComponent;
import com.evolveum.midpoint.web.component.data.column.IsolatedCheckBoxPanel;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItemAction;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.page.admin.certification.helpers.CampaignProcessingHelper;
import com.evolveum.midpoint.web.page.admin.certification.helpers.CampaignStateHelper;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignType;

import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationStageType;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CampaignTilePanel extends BasePanel<TemplateTile<SelectableBean<AccessCertificationCampaignType>>> {

    @Serial private static final long serialVersionUID = 1L;
    private static final Trace LOGGER = TraceManager.getTrace(CampaignTilePanel.class);

    private static final String ID_SELECT_TILE_CHECKBOX = "selectTileCheckbox";
    private static final String ID_STATUS = "status";
    private static final String ID_MENU = "menu";
    private static final String ID_TITLE = "title";
    private static final String ID_DESCRIPTION = "description";
    private static final String ID_PROGRESS_BAR = "progressBar";
    private static final String ID_DEADLINE = "deadline";
    private static final String ID_STAGE = "stage";
    private static final String ID_ITERATION = "iteration";
    private static final String ID_ACTION_BUTTON = "actionButton";
    private static final String ID_ACTION_BUTTON_LABEL = "actionButtonLabel";
    private static final String ID_ACTION_BUTTON_ICON = "actionButtonIcon";
    private static final String ID_DETAILS = "details";

    CampaignStateHelper campaignStateHelper;

    public CampaignTilePanel(String id, IModel<TemplateTile<SelectableBean<AccessCertificationCampaignType>>> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        campaignStateHelper = new CampaignStateHelper(getCampaign());

        initLayout();
    }

    protected void initLayout() {
        add(AttributeAppender.append("class",
                "campaign-tile-panel catalog-tile-panel d-flex flex-column align-items-center bordered p-3"));

        setOutputMarkupId(true);

        IsolatedCheckBoxPanel selectTileCheckbox = new IsolatedCheckBoxPanel(ID_SELECT_TILE_CHECKBOX, getSelectedModel());
        selectTileCheckbox.setOutputMarkupId(true);
        add(selectTileCheckbox);

        BadgePanel status = new BadgePanel(ID_STATUS, getStatusModel());
        status.setOutputMarkupId(true);
        add(status);

        DropdownButtonPanel menu = new DropdownButtonPanel(ID_MENU, createMenuDropDownButtonModel().getObject()) {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected String getSpecialButtonClass() {
                return "";
            }

            @Override
            protected boolean hasToggleIcon() {
                return false;
            }

        };
        menu.setOutputMarkupId(true);
        add(menu);

        Label title = new Label(ID_TITLE, getTitleModel());
        title.setOutputMarkupId(true);
        add(title);

        Label description = new Label(ID_DESCRIPTION, Model.of(getModelObject().getDescription()));
        description.setOutputMarkupId(true);
        add(description);

        ProgressBarPanel progressBar = new ProgressBarPanel(ID_PROGRESS_BAR, createProgressBarModel());
        progressBar.setOutputMarkupId(true);
        add(progressBar);

        DateLabelComponent deadline = new DateLabelComponent(ID_DEADLINE, getDeadlineModel(), DateLabelComponent.SHORT_SHORT_STYLE);
        deadline.setOutputMarkupId(true);
        add(deadline);

        Label stage = new Label(ID_STAGE, getStageModel());
        stage.setOutputMarkupId(true);
        add(stage);

        Label iteration = new Label(ID_ITERATION, getIterationModel());
        iteration.setOutputMarkupId(true);
        add(iteration);

        AjaxLink<Void> actionButton = new AjaxLink<>(ID_ACTION_BUTTON) {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                CampaignProcessingHelper.campaignActionPerformed(getCampaign(), getPageBase(), target);
            }
        };
        actionButton.add(AttributeModifier.append("class", campaignStateHelper.getNextAction().getActionCssClass()));
        actionButton.setOutputMarkupId(true);
        add(actionButton);

        Label actionButtonLabel = new Label(ID_ACTION_BUTTON_LABEL,
                createStringResource(campaignStateHelper.getNextAction().getActionLabelKey()));
        actionButtonLabel.setOutputMarkupId(true);
        actionButton.add(actionButtonLabel);

        WebMarkupContainer actionButtonIcon = new WebMarkupContainer(ID_ACTION_BUTTON_ICON);
        actionButtonIcon.add(AttributeModifier.append("class", campaignStateHelper.getNextAction().getActionIcon().getCssClass()));
        actionButtonIcon.setOutputMarkupId(true);
        actionButton.add(actionButtonIcon);

        AjaxLink<Void> details = new AjaxLink<>(ID_DETAILS) {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                CampaignProcessingHelper.campaignDetailsPerformed(getCampaign().getOid(), getPageBase());
            }
        };
        details.setOutputMarkupId(true);
        add(details);
    }

    private IModel<Boolean> getSelectedModel() {
        return new IModel<>() {
            @Serial private static final long serialVersionUID = 1L;
            @Override
            public Boolean getObject() {
                return getModelObject().isSelected();
            }

            @Override
            public void setObject(Boolean object) {
                getModelObject().setSelected(object);
            }
        };
    }

    private LoadableModel<Badge> getStatusModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected Badge load() {
                return campaignStateHelper.createBadge();
            }
        };
    }

    private LoadableModel<DropdownButtonDto> createMenuDropDownButtonModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected DropdownButtonDto load() {
                DropdownButtonDto button = new DropdownButtonDto(null, "fa fa-ellipsis-v", null,
                        createMenuItemsModel().getObject());
                return button;
            }
        };
    }

    private LoadableModel<List<InlineMenuItem>> createMenuItemsModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected List<InlineMenuItem> load() {
                List<CampaignStateHelper.CampaignAction> actionsList = campaignStateHelper.getAvailableActions();
                return actionsList
                        .stream()
                        .map(this::createMenuItem)
                        .toList();
            }

            private InlineMenuItem createMenuItem(CampaignStateHelper.CampaignAction action) {
                return new InlineMenuItem(createStringResource(action.getActionLabelKey())) {
                    @Serial private static final long serialVersionUID = 1L;

                    @Override
                    public InlineMenuItemAction initAction() {
                        return new InlineMenuItemAction() {
                            @Serial private static final long serialVersionUID = 1L;

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                CampaignProcessingHelper.campaignActionPerformed(getCampaign(), action, getPageBase(), target);
                            }
                        };
                    }
                };
            }
        };
    }

    private LoadableModel<String> getTitleModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected String load() {
                return WebComponentUtil.getName(getCampaign());
            }
        };
    }

    private AccessCertificationCampaignType getCampaign() {
        return getModelObject().getValue().getValue();
    }

    protected @NotNull LoadableModel<List<ProgressBar>> createProgressBarModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected List<ProgressBar> load() {
                AccessCertificationCampaignType campaign = getCampaign();
                float completed = CertCampaignTypeUtil.getCasesCompletedPercentageAllStagesAllIterations(campaign);

                ProgressBar progressBar = new ProgressBar(completed, ProgressBar.State.INFO);
                return Collections.singletonList(progressBar);
            }
        };
    }

    private LoadableModel<Date> getDeadlineModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected Date load() {
                AccessCertificationStageType currentStage = CertCampaignTypeUtil.getCurrentStage(getCampaign());
                return currentStage != null ? XmlTypeConverter.toDate(currentStage.getDeadline()) : null;
            }
        };
    }

    private LoadableModel<String> getStageModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected String load() {
                int stageNumber = getCampaign().getStageNumber();
                int numberOfStages = CertCampaignTypeUtil.getNumberOfStages(getCampaign());
                return stageNumber + "/" + numberOfStages;
            }
        };
    }

    private LoadableModel<String> getIterationModel() {
        return new LoadableModel<>() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected String load() {
                return "" + CertCampaignTypeUtil.norm(getCampaign().getIteration());
            }
        };
    }


}
