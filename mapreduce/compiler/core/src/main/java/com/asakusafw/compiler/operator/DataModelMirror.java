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
package com.asakusafw.compiler.operator;

import javax.lang.model.type.TypeMirror;

/**
 * Data model representation.
 * @since 0.2.0
 */
public interface DataModelMirror {

    /**
     * Returns the kind of this data model.
     * @return the kind
     */
    Kind getKind();

    /**
     * Returns {@code true} iff this data model and the specified data model are same.
     * @param other the other data model type
     * @return {@code true} iff both are same
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean isSame(DataModelMirror other);

    /**
     * Returns {@code true} iff this data model can apply the method parameter
     * whose type is the specified data model type.
     * That is:
     * <ul>
     * <li> this is a subtype of the other, </li>
     * <li> or this is a subtype of the other's upperbounds. </li>
     * </ul>
     * @param other the other data model type
     * @return {@code true} iff can invoke method
     *     using this data model as parameter of the specified parameter,
     *     {@code false} otherwise
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean canInvoke(DataModelMirror other);

    /**
     * Returns {@code true} iff this data model can contain the other data model.
     * That is:
     * <ul>
     * <li> this is a same type as the other, </li>
     * <li> or this is a subtype of the other's upperbounds
     *     and supertype of the other's lowerbounds. </li>
     * </ul>
     * @param other the other data model type
     * @return {@code true} iff this data model can contain the other data model,
     *     {@code false} otherwise
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean canContain(DataModelMirror other);

    /**
     * Returns a property with the specified name.
     * @param name target name
     * @return the property, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    PropertyMirror findProperty(String name);

    /**
     * Kinds of data models.
     * @since 0.2.0
     */
    enum Kind {

        /**
         * A concrete data model.
         */
        CONCRETE,

        /**
         * A part of other concrete data models.
         */
        PARTIAL,
    }

    /**
     * Property in data models.
     * @since 0.2.0
     */
    interface PropertyMirror {

        /**
         * Returns the name of this property.
         * @return the name of this property
         */
        String getName();

        /**
         * Returns the type of this property.
         * @return the type of this property
         */
        TypeMirror getType();
    }
}
