/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * An implementation of {@link InfixExpression}.
 */
public final class InfixExpressionImpl extends ModelRoot implements InfixExpression {

    private Expression leftOperand;

    private InfixOperator operator;

    private Expression rightOperand;

    @Override
    public Expression getLeftOperand() {
        return this.leftOperand;
    }

    /**
     * Sets the left term.
     * @param leftOperand the left term
     * @throws IllegalArgumentException if {@code leftOperand} was {@code null}
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
     * Sets the infix operator.
     * @param operator the infix operator
     * @throws IllegalArgumentException if {@code operator} was {@code null}
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
     * Sets the right term.
     * @param rightOperand the right term
     * @throws IllegalArgumentException if {@code rightOperand} was {@code null}
     */
    public void setRightOperand(Expression rightOperand) {
        Util.notNull(rightOperand, "rightOperand"); //$NON-NLS-1$
        this.rightOperand = rightOperand;
    }

    /**
     * Returns {@link ModelKind#INFIX_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#INFIX_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.INFIX_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitInfixExpression(this, context);
    }
}
