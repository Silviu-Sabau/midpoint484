/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.admin.role.mining.page.tmp.context;

import com.evolveum.midpoint.gui.api.component.wizard.TileEnum;
import com.evolveum.midpoint.gui.impl.page.admin.role.mining.page.tmp.modes.*;
import com.evolveum.midpoint.model.api.mining.RoleAnalysisService;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;

public enum AnalysisCategory implements TileEnum {

    STANDARD("fa fa-cogs"),
    BALANCED_COVERAGE("fa fa-balance-scale"),
    EXACT_ACCESS_SIMILARITY("fa fa-key"),
    EXPLORATORY("fa fa-search"),
    MINIMAL("fa fa-random"),
    ADVANCED("fa fa-sliders-h"),
    OUTLIER("fa fa-wrench");

    private final String iconClass;

    AnalysisCategory(String iconClass) {
        this.iconClass = iconClass;
    }

    @Override
    public String getIcon() {
        return iconClass;
    }

    public AbstractAnalysisOption generateConfiguration(RoleAnalysisService service, Task task, OperationResult result) {
        return switch (this) {
            case STANDARD -> new StandardModeConfiguration(service, task, result);
            case BALANCED_COVERAGE -> new BalancedCoverageModeConfiguration(service, task, result);
            case EXACT_ACCESS_SIMILARITY -> new ExactSimilarityModeConfiguration(service, task, result);
            case EXPLORATORY -> new ExploratoryModeConfiguration(service, task, result);
            case MINIMAL -> new MinimalConditionModeConfiguration(service, task, result);
            case ADVANCED -> new AdvancedModeConfiguration(service, task, result);
            case OUTLIER -> new OutlierModeConfiguration(service, task, result);
        };
    }
}
