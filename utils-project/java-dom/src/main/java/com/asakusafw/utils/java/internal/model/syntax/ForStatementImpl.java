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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ForInitializer;
import com.asakusafw.utils.java.model.syntax.ForStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.StatementExpressionList;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ForStatement}.
 */
public final class ForStatementImpl extends ModelRoot implements ForStatement {

    private ForInitializer initialization;

    private Expression condition;

    private StatementExpressionList update;

    private Statement body;

    @Override
    public ForInitializer getInitialization() {
        return this.initialization;
    }

    /**
     * Sets the loop initialization part.
     * @param initialization the loop initialization part, or {@code null} if it is not specified
     */
    public void setInitialization(ForInitializer initialization) {
        this.initialization = initialization;
    }

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * Sets the loop condition expression.
     * @param condition the loop condition expression, or {@code null} if it is not specified
     */
    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public StatementExpressionList getUpdate() {
        return this.update;
    }

    /**
     * Sets the loop update part.
     * @param update the loop update part, or {@code null} if it is not specified
     */
    public void setUpdate(StatementExpressionList update) {
        this.update = update;
    }

    @Override
    public Statement getBody() {
        return this.body;
    }

    /**
     * Sets the loop body.
     * @param body the loop body
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    public void setBody(Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#FOR_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#FOR_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FOR_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitForStatement(this, context);
    }
}
