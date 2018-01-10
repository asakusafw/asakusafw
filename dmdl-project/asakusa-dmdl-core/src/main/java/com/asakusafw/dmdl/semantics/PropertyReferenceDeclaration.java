/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.utils.collections.Lists;

/**
 * Represents a reference of properties.
 * @since 0.9.2
 */
public class PropertyReferenceDeclaration implements MemberDeclaration {

    private final AstNode originalAst;

    private final ModelSymbol owner;

    private final AstSimpleName name;

    private final Type referentType;

    private final ReferenceContainer<?> reference;

    private final AstDescription description;

    private final List<AstAttribute> attributes;

    private final TraitContainer traits = new TraitContainer();

    /**
     * Creates a new instance.
     * @param owner the owner symbol
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param name the name of this reference
     * @param referentType the type of referent properties
     * @param reference the reference container
     * @param description the description of this reference, or {@code null} if unknown
     * @param attributes the attributes of this reference
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PropertyReferenceDeclaration(
            AstNode originalAst,
            ModelSymbol owner,
            AstSimpleName name,
            Type referentType,
            ReferenceContainer<?> reference,
            AstDescription description,
            List<? extends AstAttribute> attributes) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (referentType == null) {
            throw new IllegalArgumentException("referentType must not be null"); //$NON-NLS-1$
        }
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.owner = owner;
        this.name = name;
        this.referentType = referentType;
        this.reference = reference;
        this.description = description;
        this.attributes = Lists.freeze(attributes);
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
     * Returns the type of each referent property.
     * @return the referent type
     */
    public Type getType() {
        return referentType;
    }

    /**
     * Returns the reference.
     * @return the reference
     */
    public ReferenceContainer<?> getReference() {
        return reference;
    }

    @Override
    public AstDescription getDescription() {
        return description;
    }

    @Override
    public List<AstAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public ModelSymbol getOwner() {
        return owner;
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
                "{0}.{1} : {2}", //$NON-NLS-1$
                owner,
                name,
                reference.getKind().getTypeName(referentType));
    }

    /**
     * Returns an instance for the target references.
     * @param symbols the references
     * @return the instance
     */
    public static ReferenceList container(List<? extends PropertySymbol> symbols) {
        return new ReferenceList(Collections.unmodifiableList(new ArrayList<>(symbols)));
    }

    /**
     * Returns an instance for the target references.
     * @param symbols the references
     * @return the instance
     */
    public static ReferenceMap container(Map<String, ? extends PropertySymbol> symbols) {
        return new ReferenceMap(Collections.unmodifiableMap(new LinkedHashMap<>(symbols)));
    }

    /**
     * Returns a stub instance.
     * @param kind the container kind
     * @return the instance
     */
    public static ReferenceContainer<?> stub(ReferenceKind kind) {
        switch (kind) {
        case LIST:
            return new ReferenceList(null);
        case MAP:
            return new ReferenceMap(null);
        default:
            throw new AssertionError(kind);
        }
    }

    /**
     * Represents a reference kind.
     * @since 0.9.2
     */
    public enum ReferenceKind {

        /**
         * Refers to list of properties.
         * @see ReferenceList
         */
        LIST("'{'{0}'}'"), //$NON-NLS-1$

        /**
         * Refers to association list of properties.
         * @see ReferenceMap
         */
        MAP("'{:'{0}'}'"), //$NON-NLS-1$
        ;
        private final String pattern;

        ReferenceKind(String pattern) {
            this.pattern = pattern;
        }

        String getTypeName(Type type) {
            return MessageFormat.format(pattern, type);
        }
    }

    /**
     * Represents a reference container.
     * @param <E> the entity type
     * @since 0.9.2
     */
    public interface ReferenceContainer<E> {

        /**
         * Returns the kind of this container.
         * @return the kind
         */
        ReferenceKind getKind();

        /**
         * Returns whether or not this container is a stub.
         * @return {@code true} if this is a stub, otherwise {@code false}
         */
        default boolean isStub() {
            return getEntity().isPresent() == false;
        }

        /**
         * Returns the container entity.
         * @return the container entity, or {@code empty} if this container is a stub
         */
        Optional<E> getEntity();

        /**
         * Returns the referred symbols.
         * @return the referred symbols
         */
        List<PropertySymbol> getAllReferences();

        /**
         * Returns the mapped container.
         * @param mapper the symbol mapper
         * @return the mapped container
         */
        ReferenceContainer<E> remap(UnaryOperator<PropertySymbol> mapper);

        /**
         * Returns the entity as {@link List}.
         * @return the symbol list
         * @throws IllegalStateException if this is not a list
         */
        default List<PropertySymbol> asList() {
            throw new IllegalStateException();
        }

        /**
         * Returns the entity as {@link Map}.
         * @return the symbol map
         * @throws IllegalStateException if this is not a map
         */
        default Map<String, PropertySymbol> asMap() {
            throw new IllegalStateException();
        }
    }

    private abstract static class AbstractReferenceContainer<E> implements ReferenceContainer<E> {

        private final ReferenceKind kind;

        private final E entity;

        AbstractReferenceContainer(ReferenceKind kind, E entity) {
            this.kind = kind;
            this.entity = entity;
        }

        @Override
        public ReferenceKind getKind() {
            return kind;
        }

        @Override
        public Optional<E> getEntity() {
            return Optional.ofNullable(entity);
        }

        @Override
        public String toString() {
            return String.valueOf(entity);
        }
    }

    /**
     * Represents a reference list.
     * @since 0.9.2
     */
    public static final class ReferenceList extends AbstractReferenceContainer<List<PropertySymbol>> {

        ReferenceList(List<PropertySymbol> entity) {
            super(ReferenceKind.LIST, entity);
        }

        @Override
        public List<PropertySymbol> getAllReferences() {
            return getEntity().orElse(Collections.emptyList());
        }

        @Override
        public ReferenceList remap(UnaryOperator<PropertySymbol> mapper) {
            return new ReferenceList(getEntity()
                    .map(it -> it.stream()
                        .map(mapper)
                        .collect(Collectors.toList()))
                    .map(Collections::unmodifiableList)
                    .orElse(null));
        }

        @Override
        public List<PropertySymbol> asList() {
            return getEntity().orElseThrow(IllegalStateException::new);
        }
    }

    /**
     * Represents a reference association list.
     * @since 0.9.2
     */
    public static final class ReferenceMap
            extends AbstractReferenceContainer<Map<String, PropertySymbol>> {

        ReferenceMap(Map<String, PropertySymbol> entity) {
            super(ReferenceKind.MAP, entity);
        }

        @Override
        public List<PropertySymbol> getAllReferences() {
            return getEntity()
                    .map(m -> (List<PropertySymbol>) new ArrayList<>(m.values()))
                    .orElse(Collections.emptyList());
        }

        @Override
        public ReferenceMap remap(UnaryOperator<PropertySymbol> mapper) {
            return new ReferenceMap(getEntity()
                    .map(it -> {
                        Map<String, PropertySymbol> results = new LinkedHashMap<>();
                        it.forEach((k, v) -> results.put(k, mapper.apply(v)));
                        return results;
                    })
                    .map(Collections::unmodifiableMap)
                    .orElse(null));
        }

        @Override
        public Map<String, PropertySymbol> asMap() {
            return getEntity().orElseThrow(IllegalStateException::new);
        }
    }
}
