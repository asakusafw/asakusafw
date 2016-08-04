/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.operator.description;

/**
 * Represents a type.
 */
public interface TypeDescription {

    /**
     * Returns the type kind.
     * @return the type kind
     */
    TypeKind getTypeKind();

    /**
     * Returns the erasure of this type.
     * @return the erasure type
     */
    ReifiableTypeDescription getErasure();

    /**
     * Represents a kind of {@link TypeDescription}.
     */
    enum TypeKind {

        /**
         * basic types.
         * @see BasicTypeDescription
         */
        BASIC,

        /**
         * array type.
         * @see ArrayTypeDescription
         */
        ARRAY,

        /**
         * class or interface types.
         * @see ClassDescription
         */
        CLASS,
    }
}
