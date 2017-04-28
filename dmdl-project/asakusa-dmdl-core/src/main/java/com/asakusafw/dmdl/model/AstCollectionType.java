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
package com.asakusafw.dmdl.model;

import java.util.Objects;

import com.asakusafw.dmdl.Region;

/**
 * Represents a collection type of property.
 * @since 0.9.2
 */
public class AstCollectionType extends AbstractAstNode implements AstType {

    private final Region region;

    /**
     * The kind of collection.
     */
    public final CollectionKind kind;

    /**
     * The element type.
     */
    public final AstType elementType;

    /**
     * Creates a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param kind the collection kind
     * @param elementType the element type
     */
    public AstCollectionType(Region region, CollectionKind kind, AstType elementType) {
        this.region = region;
        this.kind = kind;
        this.elementType = elementType;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public <C, R> R accept(C context, Visitor<C, R> visitor) {
        Objects.requireNonNull(visitor);
        R result = visitor.visitCollectionType(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(kind);
        result = prime * result + Objects.hashCode(elementType);
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
        AstCollectionType other = (AstCollectionType) obj;
        return Objects.equals(kind, other.kind)
                && Objects.equals(elementType, other.elementType);
    }

    @Override
    public String toString() {
        switch (kind) {
        case LIST:
            return String.format("{%s}", elementType); //$NON-NLS-1$
        case MAP:
            return String.format("{:%s}", elementType); //$NON-NLS-1$
        default:
            return String.format("UnknwonCollection(kind=%s, element=%s)", kind, elementType); //$NON-NLS-1$
        }
    }

    /**
     * Represents a kind of collection.
     * @since 0.9.2
     */
    public enum CollectionKind {

        /**
         * List type.
         */
        LIST,

        /**
         * Map type.
         */
        MAP,
    }
}
