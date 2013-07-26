/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * Symbol of declared properties.
 */
public class PropertySymbol implements Symbol<PropertyDeclaration> {

    private final ModelSymbol owner;

    private final AstSimpleName name;

    /**
     * Creates and returns a new instance.
     * @param owner the owner of this propery
     * @param name the name of this property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected PropertySymbol(ModelSymbol owner, AstSimpleName name) {
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

    /**
     * Returns owner of this property.
     * @return the owner of this property
     */
    public ModelSymbol getOwner() {
        return owner;
    }

    @Override
    public AstSimpleName getName() {
        return name;
    }

    @Override
    public PropertyDeclaration findDeclaration() {
        ModelDeclaration ownerDecl = getOwner().findDeclaration();
        if (ownerDecl == null) {
            return null;
        }
        return ownerDecl.findPropertyDeclaration(name.identifier);
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
        PropertySymbol other = (PropertySymbol) obj;
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
                "{0}.{1}", //$NON-NLS-1$
                owner,
                name);
    }
}
