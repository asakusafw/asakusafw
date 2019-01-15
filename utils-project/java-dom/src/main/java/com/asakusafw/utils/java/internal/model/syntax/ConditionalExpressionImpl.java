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

import com.asakusafw.utils.java.model.syntax.ConditionalExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ConditionalExpression}.
 */
public final class ConditionalExpressionImpl extends ModelRoot implements ConditionalExpression {

    private Expression condition;

    private Expression thenExpression;

    private Expression elseExpression;

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * Sets the condition term.
     * @param condition the condition term
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     */
    public void setCondition(Expression condition) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        this.condition = condition;
    }

    @Override
    public Expression getThenExpression() {
        return this.thenExpression;
    }

    /**
     * Sets the truth term.
     * @param thenExpression the truth term
     * @throws IllegalArgumentException if {@code thenExpression} was {@code null}
     */
    public void setThenExpression(Expression thenExpression) {
        Util.notNull(thenExpression, "thenExpression"); //$NON-NLS-1$
        this.thenExpression = thenExpression;
    }

    @Override
    public Expression getElseExpression() {
        return this.elseExpression;
    }

    /**
     * Sets the false term.
     * @param elseExpression the false term
     * @throws IllegalArgumentException if {@code elseExpression} was {@code null}
     */
    public void setElseExpression(Expression elseExpression) {
        Util.notNull(elseExpression, "elseExpression"); //$NON-NLS-1$
        this.elseExpression = elseExpression;
    }

    /**
     * Returns {@link ModelKind#CONDITIONAL_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#CONDITIONAL_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CONDITIONAL_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitConditionalExpression(this, context);
    }
}
