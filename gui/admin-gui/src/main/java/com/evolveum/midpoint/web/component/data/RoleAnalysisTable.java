/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.web.component.data;

import static com.evolveum.midpoint.common.mining.utils.RoleAnalysisAttributeDefUtils.getObjectNameDef;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.table.RoleAnalysisTableCellFillResolver.resolveCellTypeUserTable;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.table.RoleAnalysisTableCellFillResolver.updateFrequencyBased;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.table.RoleAnalysisTableTools.applySquareTableCell;
import static com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.table.RoleAnalysisTableTools.applyTableScaleScript;

import java.io.Serial;
import java.util.*;

import com.evolveum.midpoint.common.mining.objects.chunk.MiningBaseTypeChunk;
import com.evolveum.midpoint.common.mining.objects.detection.DetectionOption;
import com.evolveum.midpoint.common.mining.utils.values.*;
import com.evolveum.midpoint.gui.impl.component.data.column.CompositedIconColumn;
import com.evolveum.midpoint.gui.impl.component.icon.CompositedIcon;
import com.evolveum.midpoint.gui.impl.component.icon.IconCssStyle;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.page.panel.cluster.MembersDetailsPopupPanel;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.page.panel.experimental.RoleAnalysisTableSettingPanel;

import com.evolveum.midpoint.gui.impl.page.admin.role.mining.tables.operation.OutlierPatternResolver;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.tables.operation.RoleAnalysisMatrixTable;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.tables.operation.SimpleHeatPattern;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.table.RoleAnalysisTableCellFillResolver;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.utils.table.RoleAnalysisTableTools;
import com.evolveum.midpoint.gui.impl.util.IconAndStylesUtil;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.web.component.data.column.AjaxLinkPanel;
import com.evolveum.midpoint.web.component.data.column.AjaxLinkTruncatePanelAction;
import com.evolveum.midpoint.web.component.data.column.LinkIconPanelStatus;
import com.evolveum.midpoint.web.util.TooltipBehavior;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import com.google.common.collect.ListMultimap;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.common.mining.objects.chunk.DisplayValueOption;
import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.impl.component.data.provider.SelectableBeanContainerDataProvider;
import com.evolveum.midpoint.gui.impl.component.icon.CompositedIconBuilder;
import com.evolveum.midpoint.gui.impl.component.icon.LayeredIconCssStyle;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.web.component.AjaxCompositedIconSubmitButton;
import com.evolveum.midpoint.web.component.data.paging.NavigatorPanel;
import com.evolveum.midpoint.web.component.form.MidpointForm;
import com.evolveum.midpoint.web.component.util.RoleAnalysisTablePageable;
import com.evolveum.midpoint.web.component.util.VisibleBehaviour;
import com.evolveum.midpoint.web.security.MidPointAuthWebSession;
import com.evolveum.midpoint.web.session.UserProfileStorage;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

public class RoleAnalysisTable<A extends MiningBaseTypeChunk> extends BasePanel<A> implements Table {

    @Serial private static final long serialVersionUID = 1L;

    private static final String ID_HEADER_FOOTER = "headerFooter";
    private static final String ID_HEADER_PAGING = "pagingFooterHeader";
    private static final String ID_HEADER = "header";
    private static final String ID_FOOTER = "footer";
    private static final String ID_TABLE = "table";
    private static final String ID_TABLE_CONTAINER = "tableContainer";

    private static final String ID_PAGING_FOOTER = "pagingFooter";
    private static final String ID_PAGING = "paging";
    private static final String ID_COUNT = "count";
    private static final String ID_PAGE_SIZE = "pageSize";
    private static final String ID_FOOTER_CONTAINER = "footerContainer";
    private static final String ID_BUTTON_TOOLBAR = "buttonToolbar";
    private static final String ID_FORM = "form";


    private final boolean showAsCard = true;
    private final UserProfileStorage.TableId tableId;
    private String additionalBoxCssClasses = null;
    int columnCount;
    static boolean isRoleMining = false;

    private String valueTitle = null;
    private int currentPageView = 0;
    private int columnPageCount = 100;
    private int fromCol = 1 ;
    private int toCol = 100;
    private int specialColumnCount;

    LoadableDetachableModel<DisplayValueOption> displayValueOptionModel;

    public RoleAnalysisTable(String id, ISortableDataProvider<A, ?> provider, List<IColumn<A, String>> columns,
            UserProfileStorage.TableId tableId, boolean isRoleMining, int columnCount,
            LoadableDetachableModel<DisplayValueOption> displayValueOptionModel) {
        super(id);
        this.tableId = tableId;
        RoleAnalysisTable.isRoleMining = isRoleMining;
        this.displayValueOptionModel = displayValueOptionModel;

        initLayout(columns, provider);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(OnDomReadyHeaderItem
                .forScript("MidPointTheme.initResponsiveTable(); MidPointTheme.initScaleResize('#tableScaleContainer');"));
    }

    private void initLayout(List<IColumn<A, String>> columns, ISortableDataProvider<A, ?> provider) {
        setOutputMarkupId(true);
        add(AttributeAppender.prepend("class", () -> showAsCard ? "card" : ""));
        add(AttributeAppender.append("class", this::getAdditionalBoxCssClasses));

        WebMarkupContainer tableContainer = new WebMarkupContainer(ID_TABLE_CONTAINER);
        tableContainer.setOutputMarkupId(true);

        int pageSize = getItemsPerPage(tableId);


        DataTable<A, String> table = new SelectableDataTable<>(ID_TABLE, columns, provider, pageSize) {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            protected Item<A> newRowItem(String id, int index, IModel<A> rowModel) {
                Item<A> item = super.newRowItem(id, index, rowModel);
                return customizeNewRowItem(item);
            }

        };
        table.setOutputMarkupId(true);
        tableContainer.add(table);
        add(tableContainer);

        if (!isRoleMining) {
            TableHeadersToolbar<?> headersTop = new TableHeadersToolbar<>(table, provider) {

                @Override
                protected void refreshTable(AjaxRequestTarget target) {
                    super.refreshTable(target);
                    target.add(getFooter());
                }
            };

            headersTop.setOutputMarkupId(true);
            table.addTopToolbar(headersTop);
        } else {
            RoleAnalysisTableHeadersToolbar<?> headersTop = new RoleAnalysisTableHeadersToolbar<>(table, provider) {

                @Override
                protected void refreshTable(AjaxRequestTarget target) {
                    resetTable(target);
                    target.add(getFooter());
                }
            };

            headersTop.setOutputMarkupId(true);
            table.addTopToolbar(headersTop);

        }
        add(createHeader(ID_HEADER));
        WebMarkupContainer footer = createFooter();
        footer.add(new VisibleBehaviour(() -> !hideFooterIfSinglePage() || provider.size() > pageSize));

        WebMarkupContainer footer2 = createHeaderPaging();
        footer2.add(new VisibleBehaviour(() -> !hideFooterIfSinglePage() || getColumnCount() > pageSize));
        add(footer2);
        add(footer);
    }

    public String getAdditionalBoxCssClasses() {
        return additionalBoxCssClasses;
    }

    public void setAdditionalBoxCssClasses(String boxCssClasses) {
        this.additionalBoxCssClasses = boxCssClasses;
    }

    protected Item<A> customizeNewRowItem(Item<A> item) {
        return item;
    }

    protected boolean hideFooterIfSinglePage() {
        return false;
    }

    @Override
    public DataTable<?, ?> getDataTable() {
        return (DataTable<?, ?>) get(ID_TABLE_CONTAINER).get(ID_TABLE);
    }

    public Component getHeaderFooter() {
        return get("headerFooter");
    }

    @Override
    public UserProfileStorage.TableId getTableId() {
        return tableId;
    }

    @Override
    public boolean enableSavePageSize() {
        return true;
    }

    @Override
    public void setItemsPerPage(int size) {
        getDataTable().setItemsPerPage(size);
    }

    @Override
    public int getItemsPerPage() {
        return (int) getDataTable().getItemsPerPage();
    }

    private int getItemsPerPage(UserProfileStorage.TableId tableId) {
        if (tableId == null) {
            return UserProfileStorage.DEFAULT_PAGING_SIZE;
        }
        MidPointAuthWebSession session = getSession();
        UserProfileStorage userProfile = session.getSessionStorage().getUserProfile();
        return userProfile.getPagingSize(tableId);
    }

    @Override
    public void setShowPaging(boolean show) {
        if (!show) {
            setItemsPerPage(Integer.MAX_VALUE);
        } else {
            setItemsPerPage(UserProfileStorage.DEFAULT_PAGING_SIZE);
            if (isRoleMining) {
                setItemsPerPage(100);
            }
        }
    }

    public WebMarkupContainer getHeader() {
        return (WebMarkupContainer) get(ID_HEADER);
    }

    public WebMarkupContainer getFooter() {
        return (WebMarkupContainer) get(ID_FOOTER);
    }

    protected Component createHeader(String headerId) {
        WebMarkupContainer header = new WebMarkupContainer(headerId);
        header.setVisible(false);
        header.setOutputMarkupId(true);
        return header;
    }

    protected WebMarkupContainer createFooter() {
        return new PagingFooter(RoleAnalysisTable.ID_FOOTER, ID_PAGING_FOOTER, this, this) {

            @Override
            protected String getPaginationCssClass() {
                return RoleAnalysisTable.this.getPaginationCssClass();
            }

            @Override
            protected boolean isPagingVisible() {
                return RoleAnalysisTable.this.isPagingVisible();
            }
        };
    }

    protected WebMarkupContainer createHeaderPaging() {
        return new PagingFooterColumn(RoleAnalysisTable.ID_HEADER_FOOTER, ID_HEADER_PAGING, this) {

            @Override
            protected String getPaginationCssClass() {
                return RoleAnalysisTable.this.getPaginationCssClass();
            }

            @Override
            protected boolean isPagingVisible() {
                return RoleAnalysisTable.this.isPagingVisible();
            }
        };
    }

    protected boolean isPagingVisible() {
        return true;
    }

    protected String getPaginationCssClass() {
        return "pagination-sm";
    }

    @Override
    public void setCurrentPage(ObjectPaging paging) {
        WebComponentUtil.setCurrentPage(this, paging);
    }

    @Override
    public void setCurrentPage(long page) {
        getDataTable().setCurrentPage(page);
    }

    protected WebMarkupContainer createButtonToolbar(String id) {
        RepeatingView repeatingView = new RepeatingView(id);
        repeatingView.setOutputMarkupId(true);

        //Shouldn't it be reset?
        CompositedIconBuilder refreshIconBuilder = new CompositedIconBuilder().setBasicIcon(
                GuiStyleConstants.CLASS_REFRESH, LayeredIconCssStyle.IN_ROW_STYLE);
        AjaxCompositedIconSubmitButton refreshIcon = buildRefreshTableButton(repeatingView, refreshIconBuilder);
        refreshIcon.add(AttributeAppender.replace("class", "btn btn-default btn-sm"));
        repeatingView.add(refreshIcon);

        CompositedIconBuilder iconBuilder = new CompositedIconBuilder().setBasicIcon(
                "fa fa-cog", LayeredIconCssStyle.IN_ROW_STYLE);
        AjaxCompositedIconSubmitButton tableSetting = buildTableSettingButton(repeatingView, iconBuilder,
                displayValueOptionModel);
        tableSetting.add(AttributeAppender.replace("class", "btn btn-default btn-sm"));
        repeatingView.add(tableSetting);

        return repeatingView;

//        return new WebMarkupContainer(id);
    }

    @NotNull
    private AjaxCompositedIconSubmitButton buildRefreshTableButton(
            @NotNull RepeatingView repeatingView,
            @NotNull CompositedIconBuilder refreshIconBuilder ) {
        AjaxCompositedIconSubmitButton refreshIcon = new AjaxCompositedIconSubmitButton(
                repeatingView.newChildId(), refreshIconBuilder.build(), createStringResource("Refresh")) {

            @Serial private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target) {
                resetTable(target);
            }

        };

        refreshIcon.titleAsLabel(true);
        return refreshIcon;
    }

    @NotNull
    private AjaxCompositedIconSubmitButton buildTableSettingButton(@NotNull RepeatingView repeatingView,
            @NotNull CompositedIconBuilder iconBuilder,
            @NotNull LoadableDetachableModel<DisplayValueOption> displayValueOptionModel) {
        AjaxCompositedIconSubmitButton tableSetting = new AjaxCompositedIconSubmitButton(
                repeatingView.newChildId(), iconBuilder.build(), createStringResource("Table settings")) {

            @Serial private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target) {
                RoleAnalysisTableSettingPanel selector = new RoleAnalysisTableSettingPanel(
                        ((PageBase) getPage()).getMainPopupBodyId(),
                        createStringResource("RoleAnalysisPathTableSelector.title"),
                        displayValueOptionModel) {

                    @Override
                    public void performAfterFinish(AjaxRequestTarget target) {
                        resetTable(target);
                    }
                };
                ((PageBase) getPage()).showMainPopup(selector, target);
            }

        };

        tableSetting.titleAsLabel(true);
        return tableSetting;
    }

    protected void resetTable(AjaxRequestTarget target) {

    }

    protected void refreshTable(AjaxRequestTarget target) {

    }

    private static class PagingFooter extends Fragment {

        public PagingFooter(String id, String markupId, RoleAnalysisTable markupProvider, Table table) {
            super(id, markupId, markupProvider);
            setOutputMarkupId(true);

            initLayout(markupProvider, table);
        }

        private void initLayout(final RoleAnalysisTable<?> boxedTablePanel, final Table table) {
            WebMarkupContainer buttonToolbar = boxedTablePanel.createButtonToolbar(ID_BUTTON_TOOLBAR);
            add(buttonToolbar);

            final DataTable<?, ?> dataTable = table.getDataTable();
            WebMarkupContainer footerContainer = new WebMarkupContainer(ID_FOOTER_CONTAINER);
            footerContainer.setOutputMarkupId(true);
            footerContainer.add(new VisibleBehaviour(this::isPagingVisible));

            final Label count = new Label(ID_COUNT, () -> CountToolbar.createCountString(dataTable));
            count.setOutputMarkupId(true);
            footerContainer.add(count);

            NavigatorPanel nb2 = new NavigatorPanel(ID_PAGING, dataTable, true) {

                @Override
                protected void onPageChanged(AjaxRequestTarget target, long page) {
                    target.add(count);
                    target.appendJavaScript(applyTableScaleScript());

                }

                @Override
                protected boolean isCountingDisabled() {
                    if (dataTable.getDataProvider() instanceof SelectableBeanContainerDataProvider) {
                        return !((SelectableBeanContainerDataProvider<?>) dataTable.getDataProvider()).isUseObjectCounting();
                    }
                    return super.isCountingDisabled();
                }

                @Override
                protected String getPaginationCssClass() {
                    return RoleAnalysisTable.PagingFooter.this.getPaginationCssClass();
                }
            };
            footerContainer.add(nb2);

            Form<?> form = new MidpointForm<>(ID_FORM);
            footerContainer.add(form);
            PagingSizePanel menu = new PagingSizePanel(ID_PAGE_SIZE) {

                @Override
                protected List<Integer> getPagingSizes() {

                    if (isRoleMining) {
                        return List.of(new Integer[] { 50, 100, 150, 200 });
                    }
                    return super.getPagingSizes();
                }

                @Override
                protected void onPageSizeChangePerformed(AjaxRequestTarget target) {
                    Table table = findParent(Table.class);
                    UserProfileStorage.TableId tableId = table.getTableId();

                    if (tableId != null && table.enableSavePageSize()) {
                        int pageSize = (int) getPageBase().getItemsPerPage(tableId);

                        table.setItemsPerPage(pageSize);
                    }
                    target.appendJavaScript(applyTableScaleScript());
                    target.add(findParent(RoleAnalysisTable.PagingFooter.class));
                    target.add((Component) table);
                }
            };
            // todo nasty hack, we should decide whether paging should be normal or "small"
            menu.setSmall(getPaginationCssClass() != null);
            form.add(menu);
            add(footerContainer);
        }

        protected String getPaginationCssClass() {
            return "pagination-sm";
        }

        protected boolean isPagingVisible() {
            return true;
        }
    }

    private class PagingFooterColumn extends Fragment {

        public PagingFooterColumn(String id, String markupId, RoleAnalysisTable markupProvider) {
            super(id, markupId, markupProvider);
            setOutputMarkupId(true);

            initLayout();
        }

        int pagingSize = getColumnPageCount();
        long pages = 0;

        private void initLayout() {


            WebMarkupContainer footerContainer = new WebMarkupContainer(ID_FOOTER_CONTAINER);
            footerContainer.setOutputMarkupId(true);
            footerContainer.add(new VisibleBehaviour(this::isPagingVisible));

            Form<?> form = new MidpointForm<>(ID_FORM);
            footerContainer.add(form);

            Form<?> formBsProcess = new MidpointForm<>("form_bs_process");
            footerContainer.add(formBsProcess);

            CompositedIconBuilder iconBuilder = new CompositedIconBuilder().setBasicIcon(GuiStyleConstants.CLASS_PLUS_CIRCLE,
                    LayeredIconCssStyle.IN_ROW_STYLE);

            AjaxCompositedIconSubmitButton editButton = new AjaxCompositedIconSubmitButton("process_selections_id",
                    iconBuilder.build(), new LoadableModel<>() {
                @Override
                protected String load() {
                    @Nullable Set<RoleAnalysisCandidateRoleType> candidateRoleContainers = getCandidateRoleContainer();
                    if (candidateRoleContainers != null) {
                        List<RoleAnalysisCandidateRoleType> candidateRoleTypes = new ArrayList<>(candidateRoleContainers);
                        if (candidateRoleTypes.size() == 1) {
                            PolyStringType targetName = candidateRoleTypes.get(0).getCandidateRoleRef().getTargetName();
                            return createStringResource("RoleMining.button.title.edit.candidate",
                                    targetName).getString();
                        } else {
                            return createStringResource("RoleMining.button.title.edit.candidate").getString();
                        }
                    } else {
                        return createStringResource("RoleMining.button.title.candidate").getString();
                    }
                }
            }) {
                @Serial private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    onSubmitEditButton(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target) {
                    target.add(((PageBase) getPage()).getFeedbackPanel());
                }
            };
            editButton.add(new AttributeModifier("style", "min-width: 150px;"));
            editButton.add(new VisibleBehaviour(RoleAnalysisTable.this::getMigrationButtonVisibility));
            editButton.titleAsLabel(true);
            editButton.setOutputMarkupId(true);
            editButton.add(AttributeAppender.append("class", "btn btn-default btn-sm"));

            formBsProcess.add(editButton);

            List<Integer> integers = List.of(new Integer[] { 100, 200, 400 });
            DropDownChoice<Integer> colPerPage = new DropDownChoice<>("colCountOnPage",
                    new Model<>(getColumnPageCount()), integers);
            colPerPage.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    onChangeSize(colPerPage.getModelObject(), target);
                }
            });
            colPerPage.setOutputMarkupId(true);
            form.add(colPerPage);

            Label colPerPageLabel = new Label("label_dropdown", Model.of("Cols per page"));
            colPerPageLabel.setOutputMarkupId(true);
            footerContainer.add(colPerPageLabel);

            int from = 1;
            int to = getColumnPageCount();
            pagingSize = getColumnPageCount();
            String separator = " - ";
            List<String> navigation = new ArrayList<>();

            int columnCount = getColumnCount();
            if (columnCount <= to) {
                navigation.add(from + separator + columnCount);
            } else {
                while (columnCount > to) {
                    navigation.add(from + separator + to);
                    from += pagingSize;
                    to += pagingSize;
                }
                navigation.add(from + separator + columnCount);
            }

            String[] rangeParts = getColumnPagingTitle().split(" - ");
            String title = (Integer.parseInt(rangeParts[0]) + 1) + " to "
                    + Integer.parseInt(rangeParts[1])
                    + " of "
                    + columnCount;

            Label count = new Label(ID_COUNT, Model.of(title));
            count.setOutputMarkupId(true);
            footerContainer.add(count);

            RoleAnalysisTablePageable<?> roleAnalysisTablePageable = new RoleAnalysisTablePageable<>(navigation.size(),
                    getCurrentPage());

            NavigatorPanel colNavigator = new NavigatorPanel(ID_PAGING, roleAnalysisTablePageable, true) {

                @Override
                protected boolean isComponent() {
                    return false;
                }

                @Override
                protected void onPageChanged(AjaxRequestTarget target, long page) {
                    pages = page;
                    String newPageRange = navigation.get((int) page);
                    target.add(this);
                    onChange(newPageRange, target, (int) page);
                }

                @Override
                protected boolean isCountingDisabled() {
                    return super.isCountingDisabled();
                }

                @Override
                protected String getPaginationCssClass() {
                    return RoleAnalysisTable.PagingFooterColumn.this.getPaginationCssClass();
                }
            };
            footerContainer.add(colNavigator);

            add(footerContainer);
        }

        protected String getPaginationCssClass() {
            return "pagination-sm";
        }

        protected boolean isPagingVisible() {
            return true;
        }
    }

    public void onChange(String value, AjaxRequestTarget target, int currentPage) {
        currentPageView = currentPage;
        String[] rangeParts = value.split(" - ");
        valueTitle = value;
        fromCol = Integer.parseInt(rangeParts[0]);
        toCol = Integer.parseInt(rangeParts[1]);

        refreshTable(target);
    }

    public void onChangeSize(int value, AjaxRequestTarget target) {
        currentPageView = 0;
        columnPageCount = value;
        fromCol = 1;
        toCol = Math.min(value, specialColumnCount);
        valueTitle = "0 - " + toCol;

        refreshTable(target);
    }

    protected int getCurrentPage() {
        return currentPageView;
    }

    protected int getColumnCount() {
        return 100;
    }

    private int getColumnPageCount() {
        return columnPageCount;
    }

    private String getColumnPagingTitle() {
        if (valueTitle == null) {
            int columnCount = getColumnCount();
            if (columnCount < getColumnPageCount()) {
                return "0 - " + columnCount;
            }
            return "0 - " + getColumnPageCount();
        } else {
            return valueTitle;
        }
    }

    protected void onSubmitEditButton(AjaxRequestTarget target) {

    }

    protected @Nullable Set<RoleAnalysisCandidateRoleType> getCandidateRoleContainer() {
        return null;
    }

    protected boolean getMigrationButtonVisibility() {
        return true;
    }



}
