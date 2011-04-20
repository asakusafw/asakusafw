/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.util.Util;

/**
 * Declaration of data models.
 */
public class ModelDeclaration implements Declaration {

    private final DmdlSemantics owner;

    private final AstModelDefinition<?> originalAst;

    private final AstSimpleName name;

    private final AstDescription description;

    private final List<AstAttribute> attributes;

    private final List<PropertyDeclaration> declaredProperties;

    private final Map<Class<? extends Trait<?>>, Trait<?>> traits;

    /**
     * Creates and returns a new instance.
     * @param owner the world contains this symbol
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param name the name of this model
     * @param description the description of this model, or {@code null} if unknown
     * @param attributes the attribtues of this model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected ModelDeclaration(
            DmdlSemantics owner,
            AstModelDefinition<?> originalAst,
            AstSimpleName name,
            AstDescription description,
            List<? extends AstAttribute> attributes) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.owner = owner;
        this.originalAst = originalAst;
        this.name = name;
        this.description = description;
        this.attributes = Util.freeze(attributes);
        this.declaredProperties = new ArrayList<PropertyDeclaration>();
        this.traits = new HashMap<Class<? extends Trait<?>>, Trait<?>>();
    }

    /**
     * Returns the symbol refer to this declaration.
     * @return the symbol refer to this declaration
     */
    public ModelSymbol getSymbol() {
        return new ModelSymbol(owner, name);
    }

    @Override
    public AstModelDefinition<?> getOriginalAst() {
        return originalAst;
    }

    @Override
    public AstSimpleName getName() {
        return name;
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
     * Declares a new property into this model.
     * @param propertyOriginalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param propertyName the name of this property
     * @param propertyType the type of this property
     * @param propertyDescription the description of this property, or {@code null} if unknown
     * @param propertyAttributes the attribtues of this property
     * @return the declared property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertyDeclaration declareProperty(
            AstNode propertyOriginalAst,
            AstSimpleName propertyName,
            Type propertyType,
            AstDescription propertyDescription,
            List<? extends AstAttribute> propertyAttributes) {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName must not be null"); //$NON-NLS-1$
        }
        if (propertyType == null) {
            throw new IllegalArgumentException("propertyType must not be null"); //$NON-NLS-1$
        }
        if (propertyAttributes == null) {
            throw new IllegalArgumentException("propertyAttributes must not be null"); //$NON-NLS-1$
        }
        if (findPropertyDeclaration(propertyName.identifier) != null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Property \"{0}\" is already declared in the model \"{1}\"",
                    propertyName,
                    getName()));
        }
        PropertyDeclaration property = new PropertyDeclaration(
                getSymbol(),
                propertyOriginalAst,
                propertyName,
                propertyType,
                propertyDescription,
                propertyAttributes);
        declaredProperties.add(property);
        return property;
    }

    /**
     * Returns a declared property in this model.
     * @param propertyName the name of the property
     * @return a declared property with the name, or {@code null} if not declared
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertyDeclaration findPropertyDeclaration(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName must not be null"); //$NON-NLS-1$
        }
        for (PropertyDeclaration property : declaredProperties) {
            if (property.getName().identifier.equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Returns all declared properties in this model.
     * <p>
     * This returns an empty list if no properties are declared.
     * </p>
     * @return all declared properties
     */
    public List<PropertyDeclaration> getDeclaredProperties() {
        return declaredProperties;
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
        return new PropertySymbol(getSymbol(), propertyName);
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
        }
        else {
            traits.put(kind, trait);
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} {1}",
                originalAst.kind,
                name);
    }
}
