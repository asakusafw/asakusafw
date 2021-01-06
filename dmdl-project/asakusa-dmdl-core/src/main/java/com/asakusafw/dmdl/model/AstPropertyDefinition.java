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
package com.asakusafw.dmdl.model;

import java.util.List;
import java.util.Objects;

import com.asakusafw.dmdl.Region;
import com.asakusafw.utils.collections.Lists;

/**
 * Represents a definition of each property.
 * @since 0.2.0
 * @version 0.9.2
 */
public class AstPropertyDefinition extends AbstractAstNode {

    private final Region region;

    /**
     * The description of the defining property, or {@code null} if is omitted.
     */
    public final AstDescription description;

    /**
     * The attributes of the defining property.
     */
    public final List<AstAttribute> attributes;

    /**
     * The name of the defining property.
     */
    public final AstSimpleName name;

    /**
     * The type of the defining property.
     * @since 0.9.2
     */
    public final AstType type;

    /**
     * The value of the defining property.
     */
    public final AstAttributeValue expression;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param description the description of this property, or {@code null} if is omitted
     * @param attributes the attributes of this property
     * @param name the name
     * @param type the type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstPropertyDefinition(
            Region region,
            AstDescription description,
            List<AstAttribute> attributes,
            AstSimpleName name,
            AstType type) {
        this(region, description, attributes, name, type, null);
    }

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param description the description of this property, or {@code null} if is omitted
     * @param attributes the attributes of this property
     * @param name the name
     * @param type the type, or {@code null} if it is not defined
     * @param expression the property expression, or {@code null} if it is not defined
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.2
     */
    public AstPropertyDefinition(
            Region region,
            AstDescription description,
            List<AstAttribute> attributes,
            AstSimpleName name,
            AstType type,
            AstAttributeValue expression) {
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.description = description;
        this.attributes = Lists.freeze(attributes);
        this.name = name;
        this.type = type;
        this.expression = expression;
    }

    /**
     * Returns the inferred kind of this property definition.
     * @return the property kind
     * @since 0.9.2
     */
    public PropertyKind getPropertyKind() {
        if (expression != null || type instanceof AstCollectionType) {
            return PropertyKind.REFERENCE;
        } else if (type != null) {
            return PropertyKind.NORMAL;
        } else {
            return PropertyKind.INVALID;
        }
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitPropertyDefinition(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(attributes);
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(type);
        result = prime * result + Objects.hashCode(expression);
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
        AstPropertyDefinition other = (AstPropertyDefinition) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(type, other.type)
                && Objects.equals(expression, other.expression)
                && Objects.equals(attributes, other.attributes)
                && Objects.equals(type, other.type);
    }

    /**
     * Represents a kind of property.
     * @since 0.9.2
     */
    public enum PropertyKind {

        /**
         * A normal property definition.
         */
        NORMAL,

        /**
         * A property reference definition.
         */
        REFERENCE,

        /**
         * An invalid definition.
         */
        INVALID,
    }
}
