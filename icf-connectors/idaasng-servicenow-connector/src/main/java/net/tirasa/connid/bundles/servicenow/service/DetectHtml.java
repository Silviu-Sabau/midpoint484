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
package net.tirasa.connid.bundles.servicenow.service;

import java.util.regex.Pattern;

public class DetectHtml {

    public final static String TAGSTART =
            "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>";

    public final static String TAGEND =
            "\\</\\w+\\>";

    public final static String TAGSELFCLOSING =
            "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>";

    public final static String HTMLENTITY =
            "&[a-zA-Z][a-zA-Z0-9]+;";

    public final static Pattern HTMLPATTERN = Pattern.compile("(" + TAGSTART + ".*" + TAGEND + ")|(" + TAGSELFCLOSING
            + ")|(" + HTMLENTITY + ")", Pattern.DOTALL
    );

    public static boolean isHtml(final String text) {
        boolean ret = false;
        if (text != null) {
            ret = HTMLPATTERN.matcher(text).find();
        }
        return ret;
    }

}
