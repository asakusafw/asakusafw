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

import com.asakusafw.utils.java.model.syntax.AssignmentExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link AssignmentExpression}の実装。
 */
public final class AssignmentExpressionImpl extends ModelRoot implements AssignmentExpression {

    /**
     * 左辺式。
     */
    private Expression leftHandSide;

    /**
     * 単純代入演算子、または複合する演算子。
     */
    private InfixOperator operator;

    /**
     * 右辺式。
     */
    private Expression rightHandSide;

    @Override
    public Expression getLeftHandSide() {
        return this.leftHandSide;
    }

    /**
     * 左辺式を設定する。
     * @param leftHandSide
     *     左辺式
     * @throws IllegalArgumentException
     *     {@code leftHandSide}に{@code null}が指定された場合
     */
    public void setLeftHandSide(Expression leftHandSide) {
        Util.notNull(leftHandSide, "leftHandSide"); //$NON-NLS-1$
        this.leftHandSide = leftHandSide;
    }

    @Override
    public InfixOperator getOperator() {
        return this.operator;
    }

    /**
     * 単純代入演算子、または複合する演算子を設定する。
     * @param operator
     *     単純代入演算子、または複合する演算子
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     */
    public void setOperator(InfixOperator operator) {
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        this.operator = operator;
    }

    @Override
    public Expression getRightHandSide() {
        return this.rightHandSide;
    }

    /**
     * 右辺式を設定する。
     * @param rightHandSide
     *     右辺式
     * @throws IllegalArgumentException
     *     {@code rightHandSide}に{@code null}が指定された場合
     */
    public void setRightHandSide(Expression rightHandSide) {
        Util.notNull(rightHandSide, "rightHandSide"); //$NON-NLS-1$
        this.rightHandSide = rightHandSide;
    }

    /**
     * この要素の種類を表す{@link ModelKind#ASSIGNMENT_EXPRESSION}を返す。
     * @return {@link ModelKind#ASSIGNMENT_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ASSIGNMENT_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitAssignmentExpression(this, context);
    }
}
