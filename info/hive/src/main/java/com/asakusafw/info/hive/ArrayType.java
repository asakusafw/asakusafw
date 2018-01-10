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
package com.asakusafw.info.hive;

import java.text.MessageFormat;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive array type.
 * Each type category must be {@link com.asakusafw.info.hive.FieldType.Category#ARRAY}.
 * @since 0.8.1
 */
public class ArrayType implements FieldType {

    private static final String K_ELEMENT = "element";

    private final FieldType elementType;

    /**
     * Creates a new instance.
     * @param elementType the element type
     */
    public ArrayType(FieldType elementType) {
        this.elementType = elementType;
    }

    @JsonCreator
    ArrayType(
            @JsonProperty(value = "name", required = true) TypeName name,
            @JsonProperty(value = K_ELEMENT, required = true) FieldType elementType) {
        this(elementType);
        if (name != TypeName.ARRAY) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid array type name: {0}",
                    name));
        }
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return TypeName.ARRAY;
    }

    @Override
    public String getQualifiedName() {
        return String.format(
                "%s<%s>",
                getTypeName().name(),
                getElementType().getQualifiedName());
    }

    /**
     * Returns the element type.
     * @return the element type
     */
    @JsonProperty(K_ELEMENT)
    public FieldType getElementType() {
        return elementType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ArrayType other = (ArrayType) obj;
        if (!Objects.equals(elementType, other.elementType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
