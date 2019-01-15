/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * Represents a value.
 * @see Descriptions
 */
public interface ValueDescription extends Description {

    /**
     * Returns the value kind.
     * @return the value kind
     */
    ValueKind getValueKind();

    /**
     * Returns the value type.
     * @return the value type
     */
    TypeDescription getValueType();

    /**
     * Represents a kind of {@link ValueDescription}.
     */
    enum ValueKind {

        /**
         * immediate values.
         * @see ImmediateDescription
         */
        IMMEDIATE,

        /**
         * {@code enum} constants.
         * @see EnumConstantDescription
         */
        ENUM_CONSTANT,

        /**
         * types.
         * @see ReifiableTypeDescription
         */
        TYPE,

        /**
         * annotations.
         * @see AnnotationDescription
         */
        ANNOTATION,

        /**
         * array of other values.
         * @see ArrayDescription
         */
        ARRAY,

        /**
         * instantiated objects.
         *
         */
        OBJECT,

        /**
         * unknown values.
         * @see UnknownValueDescription
         */
        UNKNOWN,
    }
}
