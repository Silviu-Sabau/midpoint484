/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.gui.impl.page.self.requestAccess;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Conflict implements Serializable {

    private ConflictItem item1;

    private ConflictItem item2;

    private ConflictState state;

    private boolean warning;


}
