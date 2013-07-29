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
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;

/**
 * Declaration of properties.
 */
public class PropertyDeclaration implements Declaration {

    private final AstNode originalAst;

    private final ModelSymbol owner;

    private final AstSimpleName name;

    private final Type type;

    private final AstDescription description;

    private final List<AstAttribute> attributes;

    private final Map<Class<? extends Trait<?>>, Trait<?>> traits;

    /**
     * Creates and returns a new instance.
     * @param owner the owner symbol
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param name the name of this property
     * @param type the type of this property
     * @param description the description of this property, or {@code null} if unknown
     * @param attributes the attribtues of this property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected PropertyDeclaration(
            ModelSymbol owner,
            AstNode originalAst,
            AstSimpleName name,
            Type type,
            AstDescription description,
            List<? extends AstAttribute> attributes) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.description = description;
        this.attributes = Lists.freeze(attributes);
        this.traits = Maps.create();
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    @Override
    public AstSimpleName getName() {
        return name;
    }

    /**
     * Returns the type of this property.
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public AstDescription getDescription() {
        return description;
    }

    @Override
    public List<AstAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns owner of this property.
     * @return the owner of this property
     */
    public ModelSymbol getOwner() {
        return owner;
    }

    /**
     * Returns the symbol refer to this declaration.
     * @return the symbol refer to this declaration
     */
    public PropertySymbol getSymbol() {
        return new PropertySymbol(owner, name);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}.{1} : {2}", //$NON-NLS-1$
                owner,
                name,
                type);
    }

    @Override
    public <T extends Trait<T>> T getTrait(Class<T> kind) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        return kind.cast(traits.get(kind));
    }

    @Override
    public <T extends Trait<T>> void putTrait(Class<T> kind, T trait) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (trait == null) {
            traits.remove(kind);
        } else {
            traits.put(kind, trait);
        }
    }
}
