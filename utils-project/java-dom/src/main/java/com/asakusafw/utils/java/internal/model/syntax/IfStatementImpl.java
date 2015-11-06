/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.IfStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link IfStatement}.
 */
public final class IfStatementImpl extends ModelRoot implements IfStatement {

    private Expression condition;

    private Statement thenStatement;

    private Statement elseStatement;

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * Sets the condition expression.
     * @param condition the condition expression
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     */
    public void setCondition(Expression condition) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        this.condition = condition;
    }

    @Override
    public Statement getThenStatement() {
        return this.thenStatement;
    }

    /**
     * Sets the truth statement.
     * @param thenStatement the truth statement
     * @throws IllegalArgumentException if {@code thenStatement} was {@code null}
     */
    public void setThenStatement(Statement thenStatement) {
        Util.notNull(thenStatement, "thenStatement"); //$NON-NLS-1$
        this.thenStatement = thenStatement;
    }

    @Override
    public Statement getElseStatement() {
        return this.elseStatement;
    }

    /**
     * Sets the false statement.
     * @param elseStatement the false statement, or {@code null} if it is not specified
     */
    public void setElseStatement(Statement elseStatement) {
        this.elseStatement = elseStatement;
    }

    /**
     * Returns {@link ModelKind#IF_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#IF_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.IF_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitIfStatement(this, context);
    }
}
