/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.PostfixExpression;
import com.asakusafw.utils.java.model.syntax.PostfixOperator;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link PostfixExpression}.
 */
public final class PostfixExpressionImpl extends ModelRoot implements PostfixExpression {

    private Expression operand;

    private PostfixOperator operator;

    @Override
    public Expression getOperand() {
        return this.operand;
    }

    /**
     * Sets the operand term.
     * @param operand the operand term
     * @throws IllegalArgumentException if {@code operand} was {@code null}
     */
    public void setOperand(Expression operand) {
        Util.notNull(operand, "operand"); //$NON-NLS-1$
        this.operand = operand;
    }

    @Override
    public PostfixOperator getOperator() {
        return this.operator;
    }

    /**
     * Sets the postfix operator.
     * @param operator the postfix operator
     * @throws IllegalArgumentException if {@code operator} was {@code null}
     */
    public void setOperator(PostfixOperator operator) {
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        this.operator = operator;
    }

    /**
     * Returns {@link ModelKind#POSTFIX_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#POSTFIX_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.POSTFIX_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitPostfixExpression(this, context);
    }
}
