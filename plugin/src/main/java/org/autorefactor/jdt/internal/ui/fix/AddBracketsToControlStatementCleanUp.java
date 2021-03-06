/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013-2016 Jean-Noël Rouvignac - initial API and implementation
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

import org.autorefactor.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

/** See {@link #getDescription()} method. */
public class AddBracketsToControlStatementCleanUp extends AbstractCleanUpRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return MultiFixMessages.CleanUpRefactoringWizard_AddBracketsToControlStatementCleanUp_name;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return MultiFixMessages.CleanUpRefactoringWizard_AddBracketsToControlStatementCleanUp_description;
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    public String getReason() {
        return MultiFixMessages.CleanUpRefactoringWizard_AddBracketsToControlStatementCleanUp_reason;
    }

    @Override
    public boolean visit(IfStatement node) {
        boolean result= maybeAddBrackets(node.getThenStatement());

        if (!(node.getElseStatement() instanceof IfStatement)) {
            result&= maybeAddBrackets(node.getElseStatement());
        }

        return result;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        return maybeAddBrackets(node.getBody());
    }

    @Override
    public boolean visit(ForStatement node) {
        return maybeAddBrackets(node.getBody());
    }

    @Override
    public boolean visit(WhileStatement node) {
        return maybeAddBrackets(node.getBody());
    }

    @Override
    public boolean visit(DoStatement node) {
        return maybeAddBrackets(node.getBody());
    }

    private boolean maybeAddBrackets(final Statement statement) {
        if (statement == null || statement instanceof Block) {
            return true;
        }

        final ASTNodeFactory b= this.ctx.getASTBuilder();
        final Block block= b.block(b.copy(statement));
        this.ctx.getRefactorings().replace(statement, block);
        return false;
    }
}
