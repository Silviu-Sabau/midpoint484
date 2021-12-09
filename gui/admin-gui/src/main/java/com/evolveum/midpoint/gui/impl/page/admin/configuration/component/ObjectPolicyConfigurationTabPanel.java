/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.admin.configuration.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.gui.api.prism.wrapper.ItemWrapper;
import com.evolveum.midpoint.gui.api.prism.wrapper.ItemMandatoryHandler;
import com.evolveum.midpoint.model.api.AssignmentObjectRelation;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.web.component.search.AbstractSearchItemDefinition;
import com.evolveum.midpoint.web.session.SessionStorage;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.component.DisplayNamePanel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.prism.wrapper.PrismContainerWrapper;
import com.evolveum.midpoint.gui.impl.component.MultivalueContainerDetailsPanel;
import com.evolveum.midpoint.gui.impl.component.MultivalueContainerListPanelWithDetailsPanel;
import com.evolveum.midpoint.gui.impl.component.data.column.AbstractItemWrapperColumn.ColumnType;
import com.evolveum.midpoint.gui.impl.component.data.column.PrismContainerWrapperColumn;
import com.evolveum.midpoint.gui.impl.component.data.column.PrismPropertyWrapperColumn;
import com.evolveum.midpoint.gui.impl.component.data.column.PrismReferenceWrapperColumn;
import com.evolveum.midpoint.gui.impl.factory.panel.ItemRealValueModel;
import com.evolveum.midpoint.gui.api.prism.wrapper.PrismContainerValueWrapper;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.data.column.CheckBoxHeaderColumn;
import com.evolveum.midpoint.web.component.data.column.InlineMenuButtonColumn;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.search.SearchFactory;
import com.evolveum.midpoint.web.component.search.SearchItemDefinition;
import com.evolveum.midpoint.web.page.admin.configuration.PageSystemConfiguration;
import com.evolveum.midpoint.web.session.UserProfileStorage;

/**
 * @author skublik
 */
public class ObjectPolicyConfigurationTabPanel extends BasePanel<PrismContainerWrapper<ObjectPolicyConfigurationType>> {

    private static final long serialVersionUID = 1L;

    private static final Trace LOGGER = TraceManager.getTrace(ObjectPolicyConfigurationTabPanel.class);

    private static final String ID_OBJECTS_POLICY = "objectsPolicy";

    public ObjectPolicyConfigurationTabPanel(String id, IModel<PrismContainerWrapper<ObjectPolicyConfigurationType>> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
            super.onInitialize();

            PageParameters params = getPage().getPageParameters();
            StringValue val = params.get(PageSystemConfiguration.SELECTED_TAB_INDEX);
            if (val != null && !val.isNull()) {
                params.remove(params.getPosition(PageSystemConfiguration.SELECTED_TAB_INDEX));
            }
            params.set(PageSystemConfiguration.SELECTED_TAB_INDEX, PageSystemConfiguration.CONFIGURATION_TAB_OBJECT_POLICY);

            initLayout();
    }

    protected void initLayout() {
        MultivalueContainerListPanelWithDetailsPanel<ObjectPolicyConfigurationType> multivalueContainerListPanel
                = new MultivalueContainerListPanelWithDetailsPanel<ObjectPolicyConfigurationType>(ID_OBJECTS_POLICY, ObjectPolicyConfigurationType.class) {

            @Override
            protected boolean isCreateNewObjectVisible() {
                return true;
            }

            @Override
            protected IModel<PrismContainerWrapper<ObjectPolicyConfigurationType>> getContainerModel() {
                return ObjectPolicyConfigurationTabPanel.this.getModel();
            }

            @Override
            protected String getStorageKey() {
                return SessionStorage.KEY_OBJECT_POLICIES_TAB;
            }

            @Override
            protected UserProfileStorage.TableId getTableId() {
                return UserProfileStorage.TableId.OBJECT_POLICIES_TAB_TABLE;
            }

            @Override
            protected List<IColumn<PrismContainerValueWrapper<ObjectPolicyConfigurationType>, String>> createDefaultColumns() {
                return initBasicColumns();
            }

            @Override
            protected MultivalueContainerDetailsPanel<ObjectPolicyConfigurationType> getMultivalueContainerDetailsPanel(
                    ListItem<PrismContainerValueWrapper<ObjectPolicyConfigurationType>> item) {
                return ObjectPolicyConfigurationTabPanel.this.getMultivalueContainerDetailsPanel(item);
            }

            @Override
            protected List<AbstractSearchItemDefinition> initSearchableItems(
                    PrismContainerDefinition<ObjectPolicyConfigurationType> containerDef) {
                List<AbstractSearchItemDefinition> defs = new ArrayList<>();

                SearchFactory.addSearchRefDef(containerDef, ObjectPolicyConfigurationType.F_OBJECT_TEMPLATE_REF, defs, AreaCategoryType.ADMINISTRATION, getPageBase());
                SearchFactory.addSearchPropertyDef(containerDef, ObjectPolicyConfigurationType.F_SUBTYPE, defs);
                SearchFactory.addSearchPropertyDef(containerDef, ItemPath
                        .create(ObjectPolicyConfigurationType.F_LIFECYCLE_STATE_MODEL, LifecycleStateModelType.F_STATE, LifecycleStateType.F_NAME), defs);

                return defs;
            }
        };
        add(multivalueContainerListPanel);
        setOutputMarkupId(true);
    }

    protected void newObjectPolicyClickPerformed(AjaxRequestTarget target) {
        PrismContainerValue<ObjectPolicyConfigurationType> newObjectPolicy = getModel().getObject().getItem().createNewValue();
        PrismContainerValueWrapper<ObjectPolicyConfigurationType> newObjectPolicyWrapper = getMultivalueContainerListPanel().createNewItemContainerValueWrapper(newObjectPolicy, getModelObject(), target);
        getMultivalueContainerListPanel().itemDetailsPerformed(target, Arrays.asList(newObjectPolicyWrapper));
    }

    private MultivalueContainerDetailsPanel<ObjectPolicyConfigurationType> getMultivalueContainerDetailsPanel(
            ListItem<PrismContainerValueWrapper<ObjectPolicyConfigurationType>> item) {
        MultivalueContainerDetailsPanel<ObjectPolicyConfigurationType> detailsPanel = new  MultivalueContainerDetailsPanel<ObjectPolicyConfigurationType>(MultivalueContainerListPanelWithDetailsPanel.ID_ITEM_DETAILS, item.getModel()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected DisplayNamePanel<ObjectPolicyConfigurationType> createDisplayNamePanel(String displayNamePanelId) {
                ItemRealValueModel<ObjectPolicyConfigurationType> displayNameModel =
                        new ItemRealValueModel<ObjectPolicyConfigurationType>(item.getModel());
                return new DisplayNamePanel<ObjectPolicyConfigurationType>(displayNamePanelId, displayNameModel);
            }

            @Override
            protected ItemMandatoryHandler getMandatoryHandler() {
                return wrapper -> getMandatoryOverrideFor(wrapper);

            }
        };
        return detailsPanel;
    }

    private boolean getMandatoryOverrideFor(ItemWrapper<?, ?> itemWrapper) {
        ItemPath conflictResolutionPath = ItemPath.create(SystemConfigurationType.F_DEFAULT_OBJECT_POLICY_CONFIGURATION,
                ObjectPolicyConfigurationType.F_CONFLICT_RESOLUTION, ConflictResolutionType.F_ACTION);
        if (conflictResolutionPath.equivalent(itemWrapper.getPath().namedSegmentsOnly())) {
            return false;
        }

        ItemPath adminGuiConfigDetails  = ItemPath.create(SystemConfigurationType.F_DEFAULT_OBJECT_POLICY_CONFIGURATION,
                ObjectPolicyConfigurationType.F_ADMIN_GUI_CONFIGURATION, ArchetypeAdminGuiConfigurationType.F_OBJECT_DETAILS);

        ItemPath detailsType = ItemPath.create(adminGuiConfigDetails, GuiObjectDetailsPageType.F_TYPE);
        if (detailsType.equivalent(itemWrapper.getPath().namedSegmentsOnly())) {
            return false;
        }

        ItemPath formType = ItemPath.create(adminGuiConfigDetails, GuiObjectDetailsPageType.F_FORMS, ObjectFormType.F_TYPE);
        if (formType.equivalent(itemWrapper.getPath().namedSegmentsOnly())) {
            return false;
        }

        return itemWrapper.isMandatory();
    }

    private MultivalueContainerListPanelWithDetailsPanel<ObjectPolicyConfigurationType> getMultivalueContainerListPanel(){
        return ((MultivalueContainerListPanelWithDetailsPanel<ObjectPolicyConfigurationType>)get(ID_OBJECTS_POLICY));
    }

    private List<IColumn<PrismContainerValueWrapper<ObjectPolicyConfigurationType>, String>> initBasicColumns() {
        List<IColumn<PrismContainerValueWrapper<ObjectPolicyConfigurationType>, String>> columns = new ArrayList<>();

        columns.add(new CheckBoxHeaderColumn<>());

        columns.add(new PrismPropertyWrapperColumn<ObjectPolicyConfigurationType, QName>(getModel(), ObjectPolicyConfigurationType.F_TYPE,
                ColumnType.LINK, getPageBase()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onClick(AjaxRequestTarget target, IModel<PrismContainerValueWrapper<ObjectPolicyConfigurationType>> rowModel) {
                getMultivalueContainerListPanel().itemDetailsPerformed(target, rowModel);
            }

            @Override
            public String getCssClass() {
                return " col-lg-1 col-md-2 ";
            }

        });

        columns.add(new PrismPropertyWrapperColumn(getModel(), ObjectPolicyConfigurationType.F_SUBTYPE, ColumnType.VALUE, getPageBase()) {

            @Override
            public String getCssClass() {
                return " col-md-2 ";
            }

        });

        columns.add(new PrismReferenceWrapperColumn(getModel(), ObjectPolicyConfigurationType.F_OBJECT_TEMPLATE_REF, ColumnType.VALUE, getPageBase()));

        columns.add(new PrismReferenceWrapperColumn(getModel(), ItemPath.create(ObjectPolicyConfigurationType.F_APPLICABLE_POLICIES, ApplicablePoliciesType.F_POLICY_GROUP_REF), ColumnType.VALUE, getPageBase()));

        columns.add(new PrismContainerWrapperColumn<ObjectPolicyConfigurationType>(getModel(),
                ItemPath.create(ObjectPolicyConfigurationType.F_LIFECYCLE_STATE_MODEL, LifecycleStateModelType.F_STATE), getPageBase()) {

            @Override
            public String getCssClass() {
                return " col-md-2 ";
            }

        });

        List<InlineMenuItem> menuActionsList = getMultivalueContainerListPanel().getDefaultMenuActions();
        columns.add(new InlineMenuButtonColumn(menuActionsList, getPageBase()) {

            @Override
            public String getCssClass() {
                return " col-md-1 ";
            }
        });

        return columns;
    }
}

