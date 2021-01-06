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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.ConstructorReferenceExpression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link ConstructorReferenceExpression}.
 * @since 0.9.1
 */
public final class ConstructorReferenceExpressionImpl extends ModelRoot implements ConstructorReferenceExpression {

    private Type qualifier;

    private List<? extends Type> typeArguments;

    @Override
    public Type getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets the owner type.
     * @param qualifier the owner type
     * @throws IllegalArgumentException if {@code qualifier} was {@code null}
     */
    public void setQualifier(Type qualifier) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        this.qualifier = qualifier;
    }

    @Override
    public List<? extends Type> getTypeArguments() {
        return this.typeArguments;
    }

    /**
     * Sets the type arguments.
     * @param typeArguments the type arguments
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     */
    public void setTypeArguments(List<? extends Type> typeArguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        this.typeArguments = Util.freeze(typeArguments);
    }

    /**
     * Returns {@link ModelKind#CONSTRUCTOR_REFERENCE_EXPRESSION} which represents this element kind.
     * @return {@link ModelKind#CONSTRUCTOR_REFERENCE_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CONSTRUCTOR_REFERENCE_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitConstructorReferenceExpression(this, context);
    }
}
