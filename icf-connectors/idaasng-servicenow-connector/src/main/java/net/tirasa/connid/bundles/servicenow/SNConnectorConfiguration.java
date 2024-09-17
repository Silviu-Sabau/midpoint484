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

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

/**
 *
 * Connector configuration class. It contains all the needed methods for
 * processing the connector configuration.
 *
 */
public class SNConnectorConfiguration extends AbstractConfiguration implements StatefulConfiguration {

    private String username;

    private GuardedString password;

    private String baseAddress;

    @ConfigurationProperty(order = 1, displayMessageKey = "baseAddress.display",
            helpMessageKey = "baseAddress.help", required = true)
    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(final String baseAddress) {
        this.baseAddress = baseAddress;
    }

    @ConfigurationProperty(order = 2, displayMessageKey = "username.display",
            helpMessageKey = "username.help", required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @ConfigurationProperty(order = 3, displayMessageKey = "password.display",
            helpMessageKey = "password.help", required = true, confidential = true)
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(final GuardedString password) {
        this.password = password;
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(baseAddress)) {
            failValidation("Base URL cannot be null or empty.");
        }
        if (StringUtil.isBlank(username)) {
            failValidation("Username cannot be null or empty.");
        }
        if (StringUtil.isBlank(SecurityUtil.decrypt(password))) {
            failValidation("Password Id cannot be null or empty.");
        }
    }

    @Override
    public void release() {
    }

    private void failValidation(String key, Object... args) {
        String message = getConnectorMessages().format(key, null, args);
        throw new ConfigurationException(message);
    }

}
