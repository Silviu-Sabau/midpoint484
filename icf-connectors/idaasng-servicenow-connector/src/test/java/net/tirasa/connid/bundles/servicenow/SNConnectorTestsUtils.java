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
package net.tirasa.connid.bundles.servicenow;

import java.util.Map;
import net.tirasa.connid.bundles.servicenow.utils.SNUtils;
import org.identityconnectors.common.logging.Log;

public class SNConnectorTestsUtils {

    private static final Log LOG = Log.getLog(SNConnectorTestsUtils.class);

    public static final String USERNAME = "testuser_";

    public static final String PASSWORD = "Password01";

    public static final String PASSWORD_UPDATE = "Password0100";

    public static final String VALUE_EMPLOYEE_NUMBER = "Employee number";

    public static final String VALUE_EMPLOYEE_NUMBER_UPDATE = "Updated employee number";

    public static final String VALUE_CITY = "Rome";

    public static final String VALUE_COMMENT = "My comment";

    public static final String VALUE_CITY_UPDATE = "Milan";

    public static final String VALUE_LOCATION = "location value";

    public static final String VALUE_MANAGER = "manager value";

    public static final String RESOURCE_ATTRIBUTE_EMPLOYEE_NUMBER = "employee_number";

    public static final String RESOURCE_ATTRIBUTE_LOCATION = "location";

    public static final String RESOURCE_ATTRIBUTE_MANAGER = "manager";

    public static final String RESOURCE_ATTRIBUTE_CITY = "city";

    public static SNConnectorConfiguration buildConfiguration(Map<String, String> configuration) {
        SNConnectorConfiguration connectorConfiguration = new SNConnectorConfiguration();

        for (Map.Entry<String, String> entry : configuration.entrySet()) {

            switch (entry.getKey()) {
                case "auth.baseAddress":
                    connectorConfiguration.setBaseAddress(entry.getValue());
                    break;
                case "auth.password":
                    connectorConfiguration.setPassword(SNUtils.createProtectedPassword(entry.getValue()));
                    break;
                case "auth.username":
                    connectorConfiguration.setUsername(entry.getValue());
                    break;
                default:
                    LOG.warn("Occurrence of an non defined parameter");
                    break;
            }
        }
        return connectorConfiguration;
    }

    public static boolean isConfigurationValid(final SNConnectorConfiguration connectorConfiguration) {
        connectorConfiguration.validate();
        return true;
    }

}
