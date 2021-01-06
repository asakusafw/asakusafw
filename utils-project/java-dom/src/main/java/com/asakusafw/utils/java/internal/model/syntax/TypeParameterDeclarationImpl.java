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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link TypeParameterDeclaration}.
 */
public final class TypeParameterDeclarationImpl extends ModelRoot implements TypeParameterDeclaration {

    private SimpleName name;

    private List<? extends Type> typeBounds;

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the type variable name.
     * @param name the type variable name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends Type> getTypeBounds() {
        return this.typeBounds;
    }

    /**
     * Sets the bound types.
     * @param typeBounds the bound types
     * @throws IllegalArgumentException if {@code typeBounds} was {@code null}
     */
    public void setTypeBounds(List<? extends Type> typeBounds) {
        Util.notNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        Util.notContainNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        this.typeBounds = Util.freeze(typeBounds);
    }

    /**
     * Returns {@link ModelKind#TYPE_PARAMETER_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#TYPE_PARAMETER_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.TYPE_PARAMETER_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitTypeParameterDeclaration(this, context);
    }
}
