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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.tirasa.connid.bundles.servicenow.SNConnector;
import net.tirasa.connid.bundles.servicenow.dto.Resource;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

public final class SNAttributes {

    public static final String RESOURCE_ATTRIBUTE_ID = "sys_id";

    public static final String USER_ATTRIBUTE_USERNAME = "user_name";

    public static final String RESOURCE_ATTRIBUTE_NAME = "name";

    public static Schema buildSchema() {

        SchemaBuilder builder = new SchemaBuilder(SNConnector.class);

        ObjectClassInfoBuilder userBuilder = new ObjectClassInfoBuilder().setType(ObjectClass.ACCOUNT_NAME);
        ObjectClassInfo user;
        userBuilder.addAttributeInfo(Name.INFO);

        buildAttributes(userBuilder);

        user = userBuilder.build();
        builder.defineObjectClass(user);

        ObjectClassInfoBuilder groupBuilder = new ObjectClassInfoBuilder().setType(ObjectClass.GROUP_NAME);
        ObjectClassInfo group;
        groupBuilder.addAttributeInfo(Name.INFO);

        buildAttributes(groupBuilder);

        group = groupBuilder.build();
        builder.defineObjectClass(group);

        return builder.build();
    }

    private static void buildAttributes(final ObjectClassInfoBuilder builder) {
        for (String attributeName : Resource.asMapAttributeField().keySet()) {
            builder.addAttributeInfo(AttributeInfoBuilder.define(attributeName).build());
        }
    }

    public static AttributeBuilder buildAttributeFromClassField(final Field field,
            final String name,
            final Object value) throws IllegalArgumentException, IllegalAccessException {
        return doBuildAttributeFromClassField(name, value, field.getType());
    }

    public static AttributeBuilder doBuildAttributeFromClassField(final String name,
            final Object value,
            final Class<?> clazz) {
        AttributeBuilder attributeBuilder = new AttributeBuilder();
        if (value != null) {
            if (clazz == boolean.class || clazz == Boolean.class) {
                attributeBuilder.addValue(Boolean.class.cast(value));
            } else if (value instanceof List<?>) {
                ArrayList<?> list = new ArrayList<>((List<?>) value);
                if (list.size() > 1) {
                    for (Object elem : list) {
                        doBuildAttributeFromClassField(name, elem, clazz);
                    }
                } else if (!list.isEmpty()) {
                    attributeBuilder.addValue(list.get(0).toString());
                }
            } else {
                attributeBuilder.addValue(value.toString());
            }
        }
        if (name != null) {
            attributeBuilder.setName(name);
        }
        return attributeBuilder;
    }

}
