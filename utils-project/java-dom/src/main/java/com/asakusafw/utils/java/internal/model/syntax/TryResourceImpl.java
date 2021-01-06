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
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.TryResource;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link TryResource}.
 */
public final class TryResourceImpl extends ModelRoot implements TryResource {

    private FormalParameterDeclaration parameter;

    private Expression initializer;

    @Override
    public FormalParameterDeclaration getParameter() {
        return this.parameter;
    }

    /**
     * Sets the resource variable declaration.
     * @param parameter the resource variable declaration
     * @throws IllegalArgumentException if {@code parameter} was {@code null}
     */
    public void setParameter(FormalParameterDeclaration parameter) {
        Util.notNull(parameter, "parameter"); //$NON-NLS-1$
        this.parameter = parameter;
    }

    @Override
    public Expression getInitializer() {
        return this.initializer;
    }

    /**
     * Sets the resource expression.
     * @param initializer the resource expression
     * @throws IllegalArgumentException if {@code initializer} was {@code null}
     */
    public void setInitializer(Expression initializer) {
        Util.notNull(initializer, "initializer"); //$NON-NLS-1$
        this.initializer = initializer;
    }

    /**
     * Returns {@link ModelKind#TRY_RESOURCE} which represents this element kind.
     * @return {@link ModelKind#TRY_RESOURCE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.TRY_RESOURCE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitTryResource(this, context);
    }
}
