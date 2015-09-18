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
 * An implementation of {@link ParenthesizedExpression}.
 */
public final class ParenthesizedExpressionImpl extends ModelRoot implements ParenthesizedExpression {

    private Expression expression;

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * Sets the element expression.
     * @param expression the element expression
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    /**
     * Returns {@link ModelKind#PARENTHESIZED_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#PARENTHESIZED_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.PARENTHESIZED_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitParenthesizedExpression(this, context);
    }
}
