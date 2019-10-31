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
package org.autorefactor.jdt.internal.ui.fix;

import static org.autorefactor.jdt.internal.corext.dom.ASTNodes.getModifiersOnly;
import static org.autorefactor.jdt.internal.corext.dom.ASTNodes.modifiers;

import java.util.List;

import org.autorefactor.jdt.internal.corext.dom.ASTNodeFactory;
import org.autorefactor.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/** See {@link #getDescription()} method. */
public class RedundantModifiersCleanUp extends AbstractCleanUpRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return MultiFixMessages.CleanUpRefactoringWizard_RedundantModifiersCleanUp_name;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return MultiFixMessages.CleanUpRefactoringWizard_RedundantModifiersCleanUp_description;
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    public String getReason() {
        return MultiFixMessages.CleanUpRefactoringWizard_RedundantModifiersCleanUp_reason;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if (isInterface(node.getParent())) {
            return removePublicStaticFinalModifiers(node);
        }
        if (Modifier.isProtected(node.getModifiers()) && isFinalClass(node.getParent())) {
            return removeProtectedModifier(node);
        }
        return ensureModifiersOrder(node);
    }

    private boolean removePublicStaticFinalModifiers(FieldDeclaration node) {
        // Remove modifiers implied by the context
        boolean result= true;
        for (Modifier m : getModifiersOnly(ASTNodes.modifiers(node))) {
            if (m.isPublic() || m.isStatic() || m.isFinal()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    private boolean isInterface(ASTNode node) {
        return node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface();
    }

    private boolean isFinalClass(ASTNode node) {
        return node instanceof TypeDeclaration && Modifier.isFinal(((TypeDeclaration) node).getModifiers());
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (isInterface(node.getParent())) {
            // Remove modifiers implied by the context
            return removePublicAbstractModifiers(node);
        }
        int modifiers= node.getModifiers();
        if (Modifier.isFinal(modifiers) && (isFinalClass(node.getParent()) || Modifier.isPrivate(modifiers))) {
            return removeFinalModifier(ASTNodes.modifiers(node));
        }
        if (Modifier.isProtected(node.getModifiers()) && (node.isConstructor() ? isFinalClass(node.getParent())
                : isFinalClassWithoutInheritance(node.getParent()))) {
            return removeProtectedModifier(node);
        }
        return ensureModifiersOrder(node);
    }

    private boolean isFinalClassWithoutInheritance(ASTNode node) {
        if (node instanceof TypeDeclaration) {
            TypeDeclaration clazz= (TypeDeclaration) node;
            return isFinalClass(clazz) && clazz.superInterfaceTypes().isEmpty() && (clazz.getSuperclassType() == null
                    || ASTNodes.hasType(clazz.getSuperclassType().resolveBinding(), Object.class.getCanonicalName()));
        }

        return false;
    }

    private boolean removeProtectedModifier(BodyDeclaration node) {
        for (Modifier modifier : getModifiersOnly(ASTNodes.modifiers(node))) {
            if (modifier.isProtected()) {
                ctx.getRefactorings().remove(modifier);
                return false;
            }
        }
        return true;
    }

    private boolean removePublicAbstractModifiers(BodyDeclaration node) {
        boolean result= true;
        for (Modifier m : getModifiersOnly(ASTNodes.modifiers(node))) {
            if (m.isPublic() || m.isAbstract()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        return ensureModifiersOrder(node);
    }

    @Override
    public boolean visit(AnnotationTypeMemberDeclaration node) {
        return removePublicAbstractModifiers(node);
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        return removeStaticAbstractModifier(ASTNodes.modifiers(node)) && ensureModifiersOrder(node);
    }

    @Override
    public boolean visit(TryStatement node) {
        boolean result= true;
        for (VariableDeclarationExpression resource : ASTNodes.resources(node)) {
            result&= removeFinalModifier(ASTNodes.modifiers(resource));
        }
        return result;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        return (!isInterface(node) || removeStaticAbstractModifier(ASTNodes.modifiers(node))) && ensureModifiersOrder(node);
    }

    private boolean ensureModifiersOrder(BodyDeclaration node) {
        final List<IExtendedModifier> extendedModifiers= ASTNodes.modifiers(node);
        final List<IExtendedModifier> reorderedModifiers = ASTNodes.reorder(extendedModifiers);

        if (!extendedModifiers.equals(reorderedModifiers)) {
            reorderModifiers(reorderedModifiers);
            return false;
        }

        return true;
    }

    private void reorderModifiers(final List<IExtendedModifier> reorderedModifiers) {
        final ASTNodeFactory b= ctx.getASTBuilder();

        for (int i= 0; i < reorderedModifiers.size(); i++) {
            IExtendedModifier m= reorderedModifiers.get(i);
            if (m.isModifier()) {
                ctx.getRefactorings().moveToIndex((Modifier) m, i, b.move((Modifier) m));
            } else {
                ctx.getRefactorings().moveToIndex((Annotation) m, i, b.move((Annotation) m));
            }
        }
    }

    private boolean removeStaticAbstractModifier(List<IExtendedModifier> modifiers) {
        boolean result= true;
        for (Modifier m : getModifiersOnly(modifiers)) {
            if (m.isStatic() || m.isAbstract()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        return !isInterface(node.getParent().getParent()) || removeFinalModifier(ASTNodes.modifiers(node));
    }

    private boolean removeFinalModifier(List<IExtendedModifier> modifiers) {
        boolean result= true;
        for (Modifier m : getModifiersOnly(modifiers)) {
            if (m.isFinal()) {
                ctx.getRefactorings().remove(m);
                result= false;
            }
        }
        return result;
    }

}
