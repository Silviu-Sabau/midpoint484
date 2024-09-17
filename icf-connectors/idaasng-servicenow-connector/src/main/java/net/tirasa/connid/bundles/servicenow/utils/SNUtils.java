/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.servicenow.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class SNUtils {

    private static final Log LOG = Log.getLog(SNUtils.class);

    public final static ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL); // can send "" values

    public static GuardedString createProtectedPassword(final String password) {
        GuardedString guardedString = new GuardedString(password.toCharArray());
        return guardedString;
    }

    public static void handleGeneralError(final String message) {
        LOG.error("General error : {0}", message);
        throw new ConnectorException(message);
    }

    public static void handleGeneralError(final String message, final Exception ex) {
        LOG.error(ex, message);
        throw new ConnectorException(message, ex);
    }

    public static void wrapGeneralError(final String message, final Exception ex) {
        LOG.error(ex, message);
        throw ConnectorException.wrap(ex);
    }

    public static boolean isEmptyObject(final Object obj) {
        return obj == null
                || (obj instanceof List ? new ArrayList<>((List<?>) obj).isEmpty() : false)
                || (obj instanceof String ? StringUtil.isBlank(String.class.cast(obj)) : false);
    }

    public static String fromUnderscoredToCamelCase(final String str) {
        if (!str.contains("_") || str.contains("__")) {
            return str;
        }
        String camelCaseString = "";
        for (String part : str.split("_")) {
            camelCaseString += part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
        }
        return camelCaseString.substring(0, 1).toLowerCase()
                + camelCaseString.substring(1); // first letter must be lowerCase
    }

    public static String fromCamelCaseToUnderscored(final String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

}
