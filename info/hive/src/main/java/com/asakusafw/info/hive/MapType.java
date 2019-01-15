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
package com.asakusafw.info.hive;

import java.text.MessageFormat;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive map type.
 * Each type category must be {@link com.asakusafw.info.hive.FieldType.Category#MAP}.
 * @since 0.8.1
 */
public class MapType implements FieldType {

    private static final String K_KEY = "key";

    private static final String K_VALUE = "value";

    private final FieldType keyType;

    private final FieldType valueType;

    /**
     * Creates a new instance.
     * @param keyType the key type
     * @param valueType the value type
     */
    public MapType(FieldType keyType, FieldType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @JsonCreator
    MapType(
            @JsonProperty(value = "name", required = true) TypeName name,
            @JsonProperty(value = K_KEY, required = true) FieldType keyType,
            @JsonProperty(value = K_VALUE, required = true) FieldType valueType) {
        this(keyType, valueType);
        if (name != TypeName.MAP) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid map type name: {0}",
                    name));
        }
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return TypeName.MAP;
    }

    @Override
    public String getQualifiedName() {
        return String.format(
                "%s<%s, %s>",
                getTypeName().name(),
                getKeyType().getQualifiedName(),
                getValueType().getQualifiedName());
    }

    /**
     * Returns the key type.
     * @return the key type
     */
    @JsonProperty(K_KEY)
    public FieldType getKeyType() {
        return keyType;
    }

    /**
     * Returns the value type.
     * @return the value type
     */
    @JsonProperty(K_VALUE)
    public FieldType getValueType() {
        return valueType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(keyType);
        result = prime * result + Objects.hashCode(valueType);
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
        MapType other = (MapType) obj;
        if (!Objects.equals(keyType, other.keyType)) {
            return false;
        }
        if (!Objects.equals(valueType, other.valueType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
