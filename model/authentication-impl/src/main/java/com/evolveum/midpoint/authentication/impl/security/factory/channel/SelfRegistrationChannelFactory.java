/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.authentication.impl.security.factory.channel;

import com.evolveum.midpoint.authentication.api.AuthenticationChannel;

import org.springframework.stereotype.Component;

import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.authentication.impl.security.channel.SelfRegistrationAuthenticationChannel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AuthenticationSequenceChannelType;

/**
 * @author skublik
 */
@Component
public class SelfRegistrationChannelFactory extends AbstractChannelFactory {
    @Override
    public boolean match(String channelId) {
        return SchemaConstants.CHANNEL_SELF_REGISTRATION_URI.equals(channelId);
    }

    @Override
    public AuthenticationChannel createAuthChannel(AuthenticationSequenceChannelType channel) {
        return new SelfRegistrationAuthenticationChannel(channel);
    }

    @Override
    protected Integer getOrder() {
        return 10;
    }
}
