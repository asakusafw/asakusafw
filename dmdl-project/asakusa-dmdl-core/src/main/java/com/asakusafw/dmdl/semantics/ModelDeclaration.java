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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.utils.collections.Lists;

/**
 * Declaration of data models.
 * @since 0.2.0
 * @version 0.9.2
 */
public class ModelDeclaration implements Declaration {

    private final DmdlSemantics owner;

    private final AstModelDefinition<?> originalAst;

    private final AstSimpleName name;

    private final AstDescription description;

    private final List<AstAttribute> attributes;

    private final Map<String, MemberDeclaration> members = new LinkedHashMap<>();

    private final TraitContainer traits = new TraitContainer();

    /**
     * Creates and returns a new instance.
     * @param owner the world contains this symbol
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param name the name of this model
     * @param description the description of this model, or {@code null} if unknown
     * @param attributes the attributes of this model
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
        this.attributes = Lists.freeze(attributes);
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
     * @param propertyAttributes the attributes of this property
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
        return doDeclareMember(new PropertyDeclaration(
                getSymbol(),
                propertyOriginalAst,
                propertyName,
                propertyType,
                propertyDescription,
                propertyAttributes));
    }

    /**
     * Declares a new property reference into this model.
     * @param memberOriginalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param memberName the name of this member
     * @param referentType the referent type
     * @param references the references
     * @param memberDescription the description of this member, or {@code null} if unknown
     * @param memberAttributes the attributes of this member
     * @return the declared member
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.2
     */
    public PropertyReferenceDeclaration declarePropertyReference(
            AstNode memberOriginalAst,
            AstSimpleName memberName,
            Type referentType,
            PropertyReferenceDeclaration.ReferenceContainer<?> references,
            AstDescription memberDescription,
            List<? extends AstAttribute> memberAttributes) {
        if (memberName == null) {
            throw new IllegalArgumentException("memberName must not be null"); //$NON-NLS-1$
        }
        if (referentType == null) {
            throw new IllegalArgumentException("referentType must not be null"); //$NON-NLS-1$
        }
        if (references == null) {
            throw new IllegalArgumentException("referents must not be null"); //$NON-NLS-1$
        }
        if (memberAttributes == null) {
            throw new IllegalArgumentException("memberAttributes must not be null"); //$NON-NLS-1$
        }
        for (PropertySymbol ref : references.getAllReferences()) {
            PropertyDeclaration decl = ref.findDeclaration();
            if (decl == null || Objects.equals(getSymbol(), ref.getOwner()) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "property \"{0}\" is not declared in the model \"{1}\"", //$NON-NLS-1$
                        ref,
                        getName()));
            }
            if (decl.getType().isSame(referentType) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "reference \"{0}.{1}\" has inconsistent type: {2}:{3}", //$NON-NLS-1$
                        getName(),
                        memberName,
                        ref,
                        decl.getType()));
            }
        }
        return doDeclareMember(new PropertyReferenceDeclaration(
                memberOriginalAst,
                getSymbol(),
                memberName,
                referentType,
                references,
                memberDescription,
                memberAttributes));
    }

    private <T extends MemberDeclaration> T doDeclareMember(T member) {
        if (members.putIfAbsent(member.getName().identifier, member) != null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Property \"{0}\" is already declared in the model \"{1}\"", //$NON-NLS-1$
                    member.getName(),
                    getName()));
        }
        return member;
    }

    /**
     * Returns a declared member in this model.
     * @param memberName the name of the member
     * @return a declared member with the name, or {@code null} if not declared
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.2
     */
    public MemberDeclaration findMemberDeclaration(String memberName) {
        if (memberName == null) {
            throw new IllegalArgumentException("memberName must not be null"); //$NON-NLS-1$
        }
        return members.get(memberName);
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
        return Optional.ofNullable(findMemberDeclaration(propertyName))
                .filter(member -> member instanceof PropertyDeclaration)
                .map(member -> (PropertyDeclaration) member)
                .orElse(null);
    }

    /**
     * Returns a declared property reference in this model.
     * @param propertyName the name of the property reference
     * @return a declared property with the name, or {@code null} if not declared
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.2
     */
    public PropertyReferenceDeclaration findPropertyReferenceDeclaration(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName must not be null"); //$NON-NLS-1$
        }
        return Optional.ofNullable(findMemberDeclaration(propertyName))
                .filter(member -> member instanceof PropertyReferenceDeclaration)
                .map(member -> (PropertyReferenceDeclaration) member)
                .orElse(null);
    }

    /**
     * Returns all declared members in this model.
     * @return all declared members, or an empty list if there are no declared members
     * @since 0.9.2
     */
    public List<MemberDeclaration> getDeclaredMembers() {
        return new ArrayList<>(members.values());
    }

    /**
     * Returns all declared properties in this model.
     * @return all declared properties, or an empty list if there are no declared properties
     */
    public List<PropertyDeclaration> getDeclaredProperties() {
        return members.values().stream()
                .sequential()
                .filter(member -> member instanceof PropertyDeclaration)
                .map(member -> (PropertyDeclaration) member)
                .collect(Collectors.toList());
    }

    /**
     * Returns all declared property references in this model.
     * @return all declared property references, or an empty list if there are no elements
     * @since 0.9.2
     */
    public List<PropertyReferenceDeclaration> getDeclaredPropertyReferences() {
        return members.values().stream()
                .sequential()
                .filter(member -> member instanceof PropertyReferenceDeclaration)
                .map(member -> (PropertyReferenceDeclaration) member)
                .collect(Collectors.toList());
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
        return traits.get(kind);
    }

    @Override
    public <T extends Trait<T>> void putTrait(Class<T> kind, T trait) {
        traits.put(kind, trait);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} {1}", //$NON-NLS-1$
                originalAst.kind,
                name);
    }
}
