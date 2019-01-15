/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SynchronizedStatement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link SynchronizedStatement}.
 */
public final class SynchronizedStatementImpl extends ModelRoot implements SynchronizedStatement {

    private Expression expression;

    private Block body;

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * Sets the monitor object.
     * @param expression the monitor object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    @Override
    public Block getBody() {
        return this.body;
    }

    /**
     * Sets the body block.
     * @param body the body block
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    public void setBody(Block body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#SYNCHRONIZED_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#SYNCHRONIZED_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SYNCHRONIZED_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSynchronizedStatement(this, context);
    }
}
