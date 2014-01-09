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
import com.asakusafw.utils.java.model.syntax.InfixExpression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link InfixExpression}の実装。
 */
public final class InfixExpressionImpl extends ModelRoot implements InfixExpression {

    /**
     * 第一演算項。
     */
    private Expression leftOperand;

    /**
     * 二項演算子。
     */
    private InfixOperator operator;

    /**
     * 第二演算項。
     */
    private Expression rightOperand;

    @Override
    public Expression getLeftOperand() {
        return this.leftOperand;
    }

    /**
     * 第一演算項を設定する。
     * @param leftOperand
     *     第一演算項
     * @throws IllegalArgumentException
     *     {@code leftOperand}に{@code null}が指定された場合
     */
    public void setLeftOperand(Expression leftOperand) {
        Util.notNull(leftOperand, "leftOperand"); //$NON-NLS-1$
        this.leftOperand = leftOperand;
    }

    @Override
    public InfixOperator getOperator() {
        return this.operator;
    }

    /**
     * 二項演算子を設定する。
     * @param operator
     *     二項演算子
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     */
    public void setOperator(InfixOperator operator) {
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        this.operator = operator;
    }

    @Override
    public Expression getRightOperand() {
        return this.rightOperand;
    }

    /**
     * 第二演算項を設定する。
     * @param rightOperand
     *     第二演算項
     * @throws IllegalArgumentException
     *     {@code rightOperand}に{@code null}が指定された場合
     */
    public void setRightOperand(Expression rightOperand) {
        Util.notNull(rightOperand, "rightOperand"); //$NON-NLS-1$
        this.rightOperand = rightOperand;
    }

    /**
     * この要素の種類を表す{@link ModelKind#INFIX_EXPRESSION}を返す。
     * @return {@link ModelKind#INFIX_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.INFIX_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitInfixExpression(this, context);
    }
}
