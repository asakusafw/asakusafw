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
package com.asakusafw.info.hive;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive plain type.
 * Each type category must be {@link com.asakusafw.info.hive.FieldType.Category#PLAIN}.
 * @since 0.8.1
 */
public final class PlainType implements FieldType {

    private static final Map<TypeName, PlainType> ENTITY_MAP;
    static {
        Map<TypeName, PlainType> map = new EnumMap<>(TypeName.class);
        for (TypeName name : TypeName.values()) {
            if (name.getCategory() == Category.PLAIN) {
                map.put(name, new PlainType(name));
            }
        }
        ENTITY_MAP = Collections.unmodifiableMap(map);
    }

    private final TypeName typeName;

    private PlainType(TypeName name) {
        this.typeName = name;
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    @Override
    public String getQualifiedName() {
        return getTypeName().name();
    }

    /**
     * Returns a primitive type that has the specified type name.
     * @param name the type name
     * @return the corresponded type
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static PlainType of(
            @JsonProperty(value = "name", required = true) TypeName name) {
        if (name.getCategory() != Category.PLAIN) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported plain type: {0}",
                    name));
        }
        PlainType type = ENTITY_MAP.get(name);
        assert type != null;
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(typeName);
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
        PlainType other = (PlainType) obj;
        if (Objects.equals(typeName, other.typeName) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getTypeName().toString();
    }
}
