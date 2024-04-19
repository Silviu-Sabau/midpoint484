/*
 * Copyright (C) 2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.admin.role.mining.model;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.wicket.chartjs.*;

import org.jetbrains.annotations.NotNull;

/**
 * The RoleAnalysisAggregateChartModel class is a LoadableModel that generates aggregate
 * charts for role analysis based on provided data.
 */
public class RoleAnalysisAggregateChartModel extends LoadableModel<ChartConfiguration> {

    LoadableDetachableModel<List<RoleAnalysisModel>> roleAnalysisModels;
    boolean isLineChart = true;

    public RoleAnalysisAggregateChartModel(LoadableDetachableModel<List<RoleAnalysisModel>> roleAnalysisModel, boolean isLineChart) {
        this.roleAnalysisModels = roleAnalysisModel;
        this.isLineChart = isLineChart;
    }

    @Override
    protected ChartConfiguration load() {
        return createChartConfiguration();
    }

    private @NotNull ChartConfiguration createChartConfiguration() {

        if (isLineChart) {
            LineChartConfiguration chart = new LineChartConfiguration();
            ChartData chartData = createDataset();
            chart.setData(chartData);
            chart.setOptions(createChartOptions());
            return chart;
        } else {
            BarChartConfiguration chart = new BarChartConfiguration();
            ChartData chartData = createDataset();
            chart.setData(chartData);
            chart.setOptions(createChartOptions());
            return chart;
        }

    }

    private @NotNull ChartData createDataset() {
        ChartData chartData = new ChartData();

        ChartDataset datasetUsers = new ChartDataset();
        datasetUsers.setLabel("Users");
        datasetUsers.addBackgroudColor("Red");

        ChartDataset datasetRoles = new ChartDataset();
        datasetRoles.setLabel("Roles");
        datasetRoles.addBackgroudColor("Green");

        if (isLineChart) {
            datasetUsers.addBorderColor("Red");
            datasetUsers.setBorderWidth(1);
            datasetRoles.addBorderColor("Green");
            datasetRoles.setBorderWidth(1);
        }

        List<RoleAnalysisModel> object = roleAnalysisModels.getObject();
        for (RoleAnalysisModel roleAnalysisModel : object) {
            int rolesCount = roleAnalysisModel.getRolesCount();
            datasetUsers.addData(roleAnalysisModel.getUsersCount());
            datasetRoles.addData(rolesCount);
            chartData.addLabel(String.valueOf(rolesCount));
        }

        chartData.addDataset(datasetRoles);
        chartData.addDataset(datasetUsers);

        return chartData;
    }

    private @NotNull RoleAnalysisChartOptions createChartOptions() {
        RoleAnalysisChartOptions options = new RoleAnalysisChartOptions();
        options.setLegend(createLegendOptions());
        options.setIndexAxis(IndexAxis.AXIS_X.getValue());

//        if (roleAnalysisModels.getObject().size() < 10) {
//            options.setBarPercentage(0.2);
//        }
        return options;
    }

    private ChartLegendOption createLegendOptions() {
        ChartLegendOption legend = new ChartLegendOption();
        legend.setDisplay(false);
        ChartLegendLabel label = new ChartLegendLabel();
        label.setBoxWidth(15);
        legend.setLabels(label);
        return legend;
    }

}
