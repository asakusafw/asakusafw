/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * An implementation of {@link AssignmentExpression}.
 */
public final class AssignmentExpressionImpl extends ModelRoot implements AssignmentExpression {

    private Expression leftHandSide;

    private InfixOperator operator;

    private Expression rightHandSide;

    @Override
    public Expression getLeftHandSide() {
        return this.leftHandSide;
    }

    /**
     * Sets the left hand side term.
     * @param leftHandSide the left hand side term
     * @throws IllegalArgumentException if {@code leftHandSide} was {@code null}
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
     * Sets the simple assignment operator, or an infix operator for compound assignment expression.
     * @param operator the assignment or infix operator
     * @throws IllegalArgumentException if {@code operator} was {@code null}
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
     * Sets the right hand side term.
     * @param rightHandSide the right hand side term
     * @throws IllegalArgumentException if {@code rightHandSide} was {@code null}
     */
    public void setRightHandSide(Expression rightHandSide) {
        Util.notNull(rightHandSide, "rightHandSide"); //$NON-NLS-1$
        this.rightHandSide = rightHandSide;
    }

    /**
     * Returns {@link ModelKind#ASSIGNMENT_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#ASSIGNMENT_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ASSIGNMENT_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitAssignmentExpression(this, context);
    }
}
