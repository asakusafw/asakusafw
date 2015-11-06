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

import com.asakusafw.utils.java.model.syntax.ArrayAccessExpression;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ArrayAccessExpression}.
 */
public final class ArrayAccessExpressionImpl extends ModelRoot implements ArrayAccessExpression {

    private Expression array;

    private Expression index;

    @Override
    public Expression getArray() {
        return this.array;
    }

    /**
     * Sets the array expression.
     * @param array the array expression
     * @throws IllegalArgumentException if {@code array} was {@code null}
     */
    public void setArray(Expression array) {
        Util.notNull(array, "array"); //$NON-NLS-1$
        this.array = array;
    }

    @Override
    public Expression getIndex() {
        return this.index;
    }

    /**
     * Sets the index expression.
     * @param index the index expression
     * @throws IllegalArgumentException if {@code index} was {@code null}
     */
    public void setIndex(Expression index) {
        Util.notNull(index, "index"); //$NON-NLS-1$
        this.index = index;
    }

    /**
     * Returns {@link ModelKind#ARRAY_ACCESS_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#ARRAY_ACCESS_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ARRAY_ACCESS_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitArrayAccessExpression(this, context);
    }
}
