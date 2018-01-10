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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.ArrayCreationExpression;
import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ArrayCreationExpression}.
 */
public final class ArrayCreationExpressionImpl extends ModelRoot implements ArrayCreationExpression {

    private ArrayType type;

    private List<? extends Expression> dimensionExpressions;

    private ArrayInitializer arrayInitializer;

    @Override
    public ArrayType getType() {
        return this.type;
    }

    /**
     * Sets the target array type.
     * @param type the target array type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(ArrayType type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public List<? extends Expression> getDimensionExpressions() {
        return this.dimensionExpressions;
    }

    /**
     * Sets expressions which represent the number of dimensions for the creating array.
     * @param dimensionExpressions the dimension expression
     * @throws IllegalArgumentException if {@code dimensionExpressions} was {@code null}
     */
    public void setDimensionExpressions(List<? extends Expression> dimensionExpressions) {
        Util.notNull(dimensionExpressions, "dimensionExpressions"); //$NON-NLS-1$
        Util.notContainNull(dimensionExpressions, "dimensionExpressions"); //$NON-NLS-1$
        this.dimensionExpressions = Util.freeze(dimensionExpressions);
    }

    @Override
    public ArrayInitializer getArrayInitializer() {
        return this.arrayInitializer;
    }

    /**
     * Sets the array initializer.
     * @param arrayInitializer the array initializer, or {@code null} if it is not specified
     */
    public void setArrayInitializer(ArrayInitializer arrayInitializer) {
        this.arrayInitializer = arrayInitializer;
    }

    /**
     * Returns {@link ModelKind#ARRAY_CREATION_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#ARRAY_CREATION_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ARRAY_CREATION_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitArrayCreationExpression(this, context);
    }
}
