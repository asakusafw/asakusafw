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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ArrayInitializer}.
 */
public final class ArrayInitializerImpl extends ModelRoot implements ArrayInitializer {

    private List<? extends Expression> elements;

    @Override
    public List<? extends Expression> getElements() {
        return this.elements;
    }

    /**
     * Sets the element expressions.
     * @param elements the element expressions
     * @throws IllegalArgumentException if {@code elements} was {@code null}
     */
    public void setElements(List<? extends Expression> elements) {
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        Util.notContainNull(elements, "elements"); //$NON-NLS-1$
        this.elements = Util.freeze(elements);
    }

    /**
     * Returns {@link ModelKind#ARRAY_INITIALIZER} which represents this element kind.
     * @return {@link ModelKind#ARRAY_INITIALIZER}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.ARRAY_INITIALIZER;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitArrayInitializer(this, context);
    }
}
