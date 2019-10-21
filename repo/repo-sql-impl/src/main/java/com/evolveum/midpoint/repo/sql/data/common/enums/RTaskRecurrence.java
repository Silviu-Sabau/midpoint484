/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.repo.sql.data.common.enums;

import com.evolveum.midpoint.repo.sql.query.definition.JaxbType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskRecurrenceType;

/**
 * @author lazyman
 */
@JaxbType(type = TaskRecurrenceType.class)
public enum RTaskRecurrence implements SchemaEnum<TaskRecurrenceType> {

    SINGLE(TaskRecurrenceType.SINGLE),
    RECURRING(TaskRecurrenceType.RECURRING);

    private TaskRecurrenceType recurrence;

    RTaskRecurrence(TaskRecurrenceType recurrence) {
        this.recurrence = recurrence;
    }

    @Override
    public TaskRecurrenceType getSchemaValue() {
        return recurrence;
    }
}
