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

import com.asakusafw.utils.java.model.syntax.DoStatement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link DoStatement}.
 */
public final class DoStatementImpl extends ModelRoot implements DoStatement {

    private Statement body;

    private Expression condition;

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

    /**
     * Returns {@link ModelKind#DO_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#DO_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DO_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDoStatement(this, context);
    }
}
