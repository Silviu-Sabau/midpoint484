/*
 * Copyright (c) 2010-2022 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.gui.impl.page.admin.messagetemplate;

import org.apache.wicket.model.IModel;

import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.web.component.ObjectSummaryPanel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MessageTemplateType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SummaryPanelSpecificationType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MessageTemplateSummaryPanel extends ObjectSummaryPanel<MessageTemplateType> {

    public MessageTemplateSummaryPanel(String id, IModel<MessageTemplateType> model, SummaryPanelSpecificationType specification) {
        super(id, MessageTemplateType.class, model, specification);
    }

    @Override
    protected String getDefaultIconCssClass() {
        return GuiStyleConstants.CLASS_VALUE_POLICY_ICON;
    }

    @Override
    protected String getIconBoxAdditionalCssClass() {
        return null;
    }

    @Override
    protected String getBoxAdditionalCssClass() {
        return null;
    }

    @Override
    protected boolean isIdentifierVisible() {
        return false;
    }
}
