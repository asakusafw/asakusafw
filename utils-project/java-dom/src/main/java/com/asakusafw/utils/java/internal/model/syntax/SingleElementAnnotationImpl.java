/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SingleElementAnnotation;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link SingleElementAnnotation}.
 */
public final class SingleElementAnnotationImpl extends ModelRoot implements SingleElementAnnotation {

    private NamedType type;

    private Expression expression;

    @Override
    public NamedType getType() {
        return this.type;
    }

    /**
     * Sets the annotation type.
     * @param type the annotation type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(NamedType type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * Sets the value for {@code value} element.
     * @param expression the value for {@code value} element
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    public void setExpression(Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        this.expression = expression;
    }

    /**
     * Returns {@link ModelKind#SINGLE_ELEMENT_ANNOTATION} which represents this element kind.
     * @return {@link ModelKind#SINGLE_ELEMENT_ANNOTATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SINGLE_ELEMENT_ANNOTATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSingleElementAnnotation(this, context);
    }
}
