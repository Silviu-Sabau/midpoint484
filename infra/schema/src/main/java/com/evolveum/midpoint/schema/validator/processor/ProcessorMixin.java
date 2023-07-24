/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.schema.validator.processor;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public interface ProcessorMixin {

    default String getIdentifier(Class<?> processor) {
        return processor.getSimpleName().replaceFirst("Processor$", "");
    }

    /**
     * Matches object type and path template (without container ids in case of multivalue containers).
     *
     * @param object tested object
     * @param path validation item path
     * @param type expected type (ObjectType)
     * @param expected exptected path template
     * @param <O>
     * @return true if matches
     */
    default <O extends ObjectType> boolean matchObjectTypeAndPathTemplate(
            @NotNull PrismObject<?> object, @NotNull ItemPath path, @NotNull Class<O> type, @NotNull ItemPath expected) {

        if (!type.isAssignableFrom(object.getCompileTimeClass())) {
            return false;
        }

        if (!path.namedSegmentsOnly().equivalent(expected)) {
            return false;
        }

        Item item = object.findItem(path);
        if (item == null || item.isEmpty()) {
            return false;
        }

        return true;
    }

    default <O extends Containerable> boolean matchParentTypeAndItemName(
            PrismObject<?> object, ItemPath path, Class<O> type, ItemName itemName) {

        ItemName name = path.lastName();
        if (name == null || !itemName.equivalent(name)) {
            return false;
        }

        Item item = object.findItem(path);
        if (item == null) {
            return false;
        }

        PrismContainerValue value = item.getParent();
        return type.isAssignableFrom(value.getRealValue().getClass());
    }

    default <C extends Containerable> C getItemParent(PrismObject<?> object, ItemPath path) {
        Item item = object.findItem(path);
        if (item == null) {
            return null;
        }

        PrismContainerValue<C> value = item.getParent();
        if (value == null) {
            return null;
        }

        return value.asContainerable();
    }
}
