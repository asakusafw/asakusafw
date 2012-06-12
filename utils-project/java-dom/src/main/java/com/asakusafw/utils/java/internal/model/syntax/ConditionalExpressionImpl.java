/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
 * {@link ConditionalExpression}の実装。
 */
public final class ConditionalExpressionImpl extends ModelRoot implements ConditionalExpression {

    /**
     * 条件式。
     */
    private Expression condition;

    /**
     * 条件成立時に評価される式。
     */
    private Expression thenExpression;

    /**
     * 条件不成立時に評価される式。
     */
    private Expression elseExpression;

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * 条件式を設定する。
     * @param condition
     *     条件式
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
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
     * 条件成立時に評価される式を設定する。
     * @param thenExpression
     *     条件成立時に評価される式
     * @throws IllegalArgumentException
     *     {@code thenExpression}に{@code null}が指定された場合
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
     * 条件不成立時に評価される式を設定する。
     * @param elseExpression
     *     条件不成立時に評価される式
     * @throws IllegalArgumentException
     *     {@code elseExpression}に{@code null}が指定された場合
     */
    public void setElseExpression(Expression elseExpression) {
        Util.notNull(elseExpression, "elseExpression"); //$NON-NLS-1$
        this.elseExpression = elseExpression;
    }

    /**
     * この要素の種類を表す{@link ModelKind#CONDITIONAL_EXPRESSION}を返す。
     * @return {@link ModelKind#CONDITIONAL_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CONDITIONAL_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitConditionalExpression(this, context);
    }
}
