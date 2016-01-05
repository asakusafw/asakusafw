/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.SwitchStatement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link SwitchStatement}.
 */
public final class SwitchStatementImpl extends ModelRoot implements SwitchStatement {

    private Expression expression;

    private List<? extends Statement> statements;

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * Sets the {@code switch} selector expression.
     * @param expression the {@code switch} selector expression
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    @Override
    public List<? extends Statement> getStatements() {
        return this.statements;
    }

    /**
     * Sets the {@code switch} body statements.
     * @param statements the {@code switch} body statements
     * @throws IllegalArgumentException if {@code statements} was {@code null}
     */
    public void setStatements(List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        Util.notContainNull(statements, "statements"); //$NON-NLS-1$
        this.statements = Util.freeze(statements);
    }

    /**
     * Returns {@link ModelKind#SWITCH_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#SWITCH_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SWITCH_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSwitchStatement(this, context);
    }
}
