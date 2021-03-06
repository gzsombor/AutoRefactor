/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2017-2019 Fabrice Tiercelin - Initial API and implementation
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

import java.util.ArrayList;
import java.util.List;

import org.autorefactor.jdt.internal.corext.dom.ASTNodeFactory;
import org.autorefactor.jdt.internal.corext.dom.ASTNodes;
import org.autorefactor.jdt.internal.corext.dom.BlockSubVisitor;
import org.autorefactor.jdt.internal.corext.dom.Refactorings;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;

/** See {@link #getDescription()} method. */
public class OneCodeThatFallsThroughRatherThanRedundantBlocksCleanUp extends AbstractCleanUpRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return MultiFixMessages.CleanUpRefactoringWizard_OneCodeThatFallsThroughRatherThanRedundantBlocksCleanUp_name;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return MultiFixMessages.CleanUpRefactoringWizard_OneCodeThatFallsThroughRatherThanRedundantBlocksCleanUp_description;
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    public String getReason() {
        return MultiFixMessages.CleanUpRefactoringWizard_OneCodeThatFallsThroughRatherThanRedundantBlocksCleanUp_reason;
    }

    @Override
    public boolean visit(Block node) {
        final CatchesAndFollowingCodeVisitor catchesAndFollowingCodeVisitor= new CatchesAndFollowingCodeVisitor(ctx,
                node);
        node.accept(catchesAndFollowingCodeVisitor);
        return catchesAndFollowingCodeVisitor.getResult();
    }

    private static final class CatchesAndFollowingCodeVisitor extends BlockSubVisitor {
        public CatchesAndFollowingCodeVisitor(final RefactoringContext ctx, final Block startNode) {
            super(ctx, startNode);
        }

        @Override
        public boolean visit(TryStatement node) {
            return visitStatement(node);
        }

        @Override
        public boolean visit(IfStatement node) {
            return visitStatement(node);
        }

        private boolean visitStatement(Statement node) {
            if (!getResult()) {
                return true;
            }

            final List<Statement> redundantStatements= new ArrayList<>();
            collectStatements(node, redundantStatements);
            return maybeRemoveRedundantCode(node, redundantStatements);
        }

        @SuppressWarnings("unchecked")
        private void collectStatements(Statement node, final List<Statement> redundantStatements) {
            if (node == null) {
                return;
            }

            TryStatement ts= ASTNodes.as(node, TryStatement.class);
            IfStatement is= ASTNodes.as(node, IfStatement.class);

            if (ts != null && ts.getFinally() == null) {
                for (CatchClause catchClause : (List<CatchClause>) ts.catchClauses()) {
                    doCollectStatements(catchClause.getBody(), redundantStatements);
                }
            } else if (is != null) {
                doCollectStatements(is.getThenStatement(), redundantStatements);
                doCollectStatements(is.getElseStatement(), redundantStatements);
            }
        }

        private void doCollectStatements(Statement node, final List<Statement> redundantStatements) {
            if (node == null) {
                return;
            }

            redundantStatements.add(node);
            List<Statement> statements= ASTNodes.asList(node);

            if (statements == null || statements.isEmpty()) {
                return;
            }

            node= statements.get(statements.size() - 1);
            collectStatements(node, redundantStatements);
        }

        private boolean maybeRemoveRedundantCode(final Statement node, final List<Statement> redundantStatements) {
            if (redundantStatements.isEmpty()) {
                return true;
            }

            final List<Statement> referenceStatements= new ArrayList<>();

            Statement nextSibling= ASTNodes.getNextSibling(node);
            while (nextSibling != null && !ASTNodes.fallsThrough(nextSibling)) {
                referenceStatements.add(nextSibling);
                nextSibling= ASTNodes.getNextSibling(nextSibling);
            }

            if (nextSibling != null) {
                referenceStatements.add(nextSibling);
                ASTNodeFactory b= ctx.getASTBuilder();

                for (Statement redundantStatement : redundantStatements) {
                    List<Statement> stmtsToCompare= ASTNodes.asList(redundantStatement);

                    if (stmtsToCompare.size() > referenceStatements.size()) {
                        stmtsToCompare= stmtsToCompare.subList(stmtsToCompare.size() - referenceStatements.size(),
                                stmtsToCompare.size());
                    }

                    if (ASTNodes.match(referenceStatements, stmtsToCompare)) {
                        Refactorings r= ctx.getRefactorings();

                        if (redundantStatement instanceof Block) {
                            r.remove(stmtsToCompare);
                        } else {
                            r.replace(redundantStatement, b.block());
                        }

                        setResult(false);
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
