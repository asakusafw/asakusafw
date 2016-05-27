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
package com.asakusafw.directio.hive.info;

import java.text.MessageFormat;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive sequence type.
 * Each type category must be {@link com.asakusafw.directio.hive.info.FieldType.Category#SEQUENCE}.
 * @since 0.8.1
 */
public class SequenceType implements FieldType {

    private static final String K_LENGTH = "length";

    private final TypeName typeName;

    private final int length;

    /**
     * Creates a new instance.
     * @param name the type name
     * @param length the element length
     */
    @JsonCreator
    public SequenceType(
            @JsonProperty(value = "name", required = true) TypeName name,
            @JsonProperty(value = K_LENGTH, required = true) int length) {
        if (name.getCategory() != Category.SEQUENCE) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported sequence type: {0}",
                    name));
        }
        this.typeName = name;
        this.length = length;
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    @Override
    public String getQualifiedName() {
        return String.format(
                "%s(%d)", //$NON-NLS-1$
                getTypeName(),
                getLength());
    }

    /**
     * Returns the element length.
     * @return the element length
     */
    @JsonProperty(K_LENGTH)
    public int getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(typeName);
        result = prime * result + length;
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
        SequenceType other = (SequenceType) obj;
        if (Objects.equals(typeName, other.typeName) == false) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
