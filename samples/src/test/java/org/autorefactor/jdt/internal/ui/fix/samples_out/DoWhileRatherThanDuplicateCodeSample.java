/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2017 Fabrice Tiercelin - Initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.jdt.internal.ui.fix.samples_out;

import java.util.List;

public class DoWhileRatherThanDuplicateCodeSample {

    public void replaceWhileByDoWhile(int i) {

        // Keep this comment
        do {
            System.out.println("Statement 1");
            System.out.println("Statement 2");
        } while (i-- > 0);
    }

    public void replaceWhileWithoutBlock(int i) {

        // Keep this comment
        do i--;
        while (i > 0);
    }

    public void doNotReplaceWhileByDoWhile(int i) {
        System.out.println("A statement");
        while (i-- > 0) {
            System.out.println("Another statement");
        }
    }

    public void doNotReplaceWithMissingStatement(int i) {
        System.out.println("A statement");
        while (i-- > 0) {
            System.out.println("A statement");
            System.out.println("A statement");
        }
    }

    public void doNotRemoveVariableDeclaration(int i) {
        System.out.println("A statement");
        int j = 0;
        while (i-- > 0) {
            System.out.println("A statement");
            j = 0;
        }
    }

    public void doNotRemoveEmptyLoop(List listToEmpty) {
        while (listToEmpty.remove("foo")) {
        }
    }
}
