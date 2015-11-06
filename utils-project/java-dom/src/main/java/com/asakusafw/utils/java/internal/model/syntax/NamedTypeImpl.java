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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link NamedType}.
 */
public final class NamedTypeImpl extends ModelRoot implements NamedType {

    private Name name;

    @Override
    public Name getName() {
        return this.name;
    }

    /**
     * Sets the type name.
     * @param name the type name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(Name name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * Returns {@link ModelKind#NAMED_TYPE} which represents this element kind.
     * @return {@link ModelKind#NAMED_TYPE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.NAMED_TYPE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitNamedType(this, context);
    }
}
