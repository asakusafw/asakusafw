/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.dmdl.semantics;

import java.text.MessageFormat;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;

/**
 * Symbol of declared models.
 */
public class ModelSymbol implements Symbol<ModelDeclaration> {

    private final DmdlSemantics owner;

    private final AstSimpleName name;

    /**
     * Creates and returns a new instance.
     * @param owner the world that contains this model
     * @param name the name of this
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected ModelSymbol(DmdlSemantics owner, AstSimpleName name) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.owner = owner;
        this.name = name;
    }

    @Override
    public AstNode getOriginalAst() {
        return name;
    }

    @Override
    public AstSimpleName getName() {
        return name;
    }

    /**
     * Creates and returns a new property symbol in this model.
     * @param propertyName the name of target property
     * @return the created symbol
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertySymbol createPropertySymbol(AstSimpleName propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName must not be null"); //$NON-NLS-1$
        }
        return new PropertySymbol(this, propertyName);
    }

    @Override
    public ModelDeclaration findDeclaration() {
        return owner.findModelDeclaration(getName().identifier);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + owner.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModelSymbol other = (ModelSymbol) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        if (!owner.equals(other.owner)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}", //$NON-NLS-1$
                name);
    }
}
