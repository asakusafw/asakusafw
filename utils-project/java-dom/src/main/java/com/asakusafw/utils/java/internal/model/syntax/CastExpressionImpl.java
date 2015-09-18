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

import com.asakusafw.utils.java.model.syntax.CastExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link CastExpression}.
 */
public final class CastExpressionImpl extends ModelRoot implements CastExpression {

    private Type type;

    private Expression expression;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the target type.
     * @param type the target type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * Sets the target term.
     * @param expression the target term
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    /**
     * Returns {@link ModelKind#CAST_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#CAST_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CAST_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitCastExpression(this, context);
    }
}
