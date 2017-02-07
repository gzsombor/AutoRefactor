package org.autorefactor.jdt.internal.corext.dom;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.autorefactor.util.NotImplementedException;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

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
