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
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.ParenthesizedExpression;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ParenthesizedExpression}の実装。
 */
public final class ParenthesizedExpressionImpl extends ModelRoot implements ParenthesizedExpression {

    /**
     * 内包する式。
     */
    private Expression expression;

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * 内包する式を設定する。
     * @param expression
     *     内包する式
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    /**
     * この要素の種類を表す{@link ModelKind#PARENTHESIZED_EXPRESSION}を返す。
     * @return {@link ModelKind#PARENTHESIZED_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.PARENTHESIZED_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitParenthesizedExpression(this, context);
    }
}
