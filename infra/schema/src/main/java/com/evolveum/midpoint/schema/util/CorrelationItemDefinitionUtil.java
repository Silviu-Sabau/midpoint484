/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.schema.util;

import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import org.jetbrains.annotations.Nullable;

/**
 * Utilities for handling correlation item definitions.
 */
public class CorrelationItemDefinitionUtil {

    /**
     * Returns the name under which we will reference this item definition (using "ref" elements).
     */
    public static @NotNull String getName(@NotNull ItemCorrelationType definitionBean) {
        if (definitionBean.getName() != null) {
            return definitionBean.getName();
        }
        String nameFromPath = getNameFromPath(definitionBean.getPath());
        if (nameFromPath != null) {
            return nameFromPath;
        }
        throw new IllegalArgumentException("Item definition with no name " + definitionBean);
    }

    private static @Nullable String getNameFromPath(ItemPathType path) {
        if (path == null) {
            return null;
        }
        ItemName lastName = path.getItemPath().lastName();
        if (lastName != null) {
            return lastName.getLocalPart();
        }
        return null;
    }

    public static Object identifyLazily(@Nullable AbstractCorrelatorType configBean) {
        return DebugUtil.lazy(() -> identify(configBean));
    }

    /**
     * Tries to shortly identify given correlator configuration. Just to able to debug e.g. configuration resolution.
     */
    public static String identify(@Nullable AbstractCorrelatorType configBean) {
        if (configBean == null) {
            return "(none)";
        } else {
            StringBuilder sb = new StringBuilder(configBean.getClass().getSimpleName());
            sb.append(": ");
            if (configBean.getName() != null) {
                sb.append("name: ")
                        .append(configBean.getName())
                        .append(", ");
            } else {
                sb.append("unnamed, ");
            }
            if (configBean.getDisplayName() != null) {
                sb.append("displayName: ")
                        .append(configBean.getDisplayName())
                        .append(", ");
            }
            if (configBean.getUsing() != null) {
                sb.append("using '")
                        .append(configBean.getUsing())
                        .append("', ");
            }
            if (configBean.getExtending() != null) {
                sb.append("extending '")
                        .append(configBean.getExtending())
                        .append("', ");
            }
            if (configBean.getOrder() != null) {
                sb.append("order ")
                        .append(configBean.getOrder())
                        .append(", ");
            }
            if (Boolean.FALSE.equals(configBean.isEnabled())) {
                sb.append("disabled, ");
            }
            if (configBean.getAuthority() != null) {
                sb.append("authority: ")
                        .append(configBean.getAuthority())
                        .append(", ");
            }
            sb.append("having ")
                    .append(configBean.asPrismContainerValue().size())
                    .append(" item(s)");
            return sb.toString();
        }
    }
}
