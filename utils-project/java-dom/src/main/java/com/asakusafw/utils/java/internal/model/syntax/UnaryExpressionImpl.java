/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.UnaryExpression;
import com.asakusafw.utils.java.model.syntax.UnaryOperator;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link UnaryExpression}の実装。
 */
public final class UnaryExpressionImpl extends ModelRoot implements UnaryExpression {

    /**
     * 単項演算子。
     */
    private UnaryOperator operator;

    /**
     * 演算項。
     */
    private Expression operand;

    @Override
    public UnaryOperator getOperator() {
        return this.operator;
    }

    /**
     * 単項演算子を設定する。
     * @param operator
     *     単項演算子
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     */
    public void setOperator(UnaryOperator operator) {
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        this.operator = operator;
    }

    @Override
    public Expression getOperand() {
        return this.operand;
    }

    /**
     * 演算項を設定する。
     * @param operand
     *     演算項
     * @throws IllegalArgumentException
     *     {@code operand}に{@code null}が指定された場合
     */
    public void setOperand(Expression operand) {
        Util.notNull(operand, "operand"); //$NON-NLS-1$
        this.operand = operand;
    }

    /**
     * この要素の種類を表す{@link ModelKind#UNARY_EXPRESSION}を返す。
     * @return {@link ModelKind#UNARY_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.UNARY_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitUnaryExpression(this, context);
    }
}
