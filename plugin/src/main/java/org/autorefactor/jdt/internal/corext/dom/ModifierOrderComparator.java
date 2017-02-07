/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013-2018 Jean-Noël Rouvignac - initial API and implementation
 * Copyright (C) 2018 Andrei Paikin - Remove protected modifier for final class not inherited members.
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
package org.autorefactor.jdt.internal.corext.dom;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.autorefactor.util.NotImplementedException;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

/**
 * Comparator for {@link IExtendedModifier}s.
 */
public final class ModifierOrderComparator implements Comparator<IExtendedModifier> {
    /**
     * Compare objects.
     *
     * @param o1
     *            First item
     * @param o2
     *            Second item
     *
     * @return -1, 0 or 1
     */
    public int compare(IExtendedModifier o1, IExtendedModifier o2) {
        if (o1.isAnnotation()) {
            if (o2.isAnnotation()) {
                return 0;
            } else {
                return -1;
            }
        } else if (o2.isAnnotation()) {
            return 1;
        } else {
            final int i1 = ORDERED_MODIFIERS.indexOf(((Modifier) o1).getKeyword());
            final int i2 = ORDERED_MODIFIERS.indexOf(((Modifier) o2).getKeyword());
            if (i1 == -1) {
                throw new NotImplementedException(((Modifier) o1), "cannot determine order for modifier " + o1); //$NON-NLS-1$
            }
            if (i2 == -1) {
                throw new NotImplementedException(((Modifier) o2), "cannot compare modifier " + o2); //$NON-NLS-1$
            }
            return i1 - i2;
        }
    }

    private static final List<ModifierKeyword> ORDERED_MODIFIERS= Collections.unmodifiableList(Arrays.asList(
            ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD,
            ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.STATIC_KEYWORD, ModifierKeyword.FINAL_KEYWORD,
            ModifierKeyword.TRANSIENT_KEYWORD, ModifierKeyword.VOLATILE_KEYWORD, ModifierKeyword.SYNCHRONIZED_KEYWORD,
            ModifierKeyword.NATIVE_KEYWORD, ModifierKeyword.STRICTFP_KEYWORD));

}
