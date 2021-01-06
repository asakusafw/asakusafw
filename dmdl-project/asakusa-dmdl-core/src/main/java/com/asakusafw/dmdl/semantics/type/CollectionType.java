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
package com.asakusafw.dmdl.semantics.type;

import com.asakusafw.dmdl.model.AstCollectionType;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.Type;

/**
 * Collection type of properties.
 * @since 0.9.2
 */
public class CollectionType implements Type {

    private final AstCollectionType originalAst;

    private final CollectionKind kind;

    private final Type elementType;

    /**
     * Creates a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param kind the kind of this type
     * @param elementType the element type
     */
    public CollectionType(AstCollectionType originalAst, CollectionKind kind, Type elementType) {
        this.originalAst = originalAst;
        this.kind = kind;
        this.elementType = elementType;
    }

    @Override
    public AstCollectionType getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the collection kind of this type.
     * @return the kind
     */
    public CollectionKind getKind() {
        return kind;
    }

    /**
     * Returns the element type.
     * @return the element type
     */
    public Type getElementType() {
        return elementType;
    }

    @Override
    public Type map(PropertyMappingKind mapping) {
        return null;
    }

    @Override
    public boolean isSame(Type o) {
        if ((o instanceof CollectionType) == false) {
            return false;
        }
        CollectionType other = (CollectionType) o;
        return kind == other.kind && elementType.isSame(other.elementType);
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
     * Returns the kind of this type.
     * @since 0.9.2
     */
    public enum CollectionKind {

        /**
         * List of types.
         */
        LIST,

        /**
         * Association list of types.
         */
        MAP,
    }
}
