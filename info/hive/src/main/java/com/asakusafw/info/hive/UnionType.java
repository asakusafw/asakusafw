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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive union type.
 * Type category must be {@link com.asakusafw.info.hive.FieldType.Category#UNION}.
 * @since 0.8.1
 */
public class UnionType implements FieldType {

    private static final String K_ELEMENTS = "elements";

    @JsonProperty(K_ELEMENTS)
    private final List<FieldType> elementTypes;

    /**
     * Creates a new instance.
     * @param elementTypes the element types
     */
    public UnionType(List<? extends FieldType> elementTypes) {
        this.elementTypes = Collections.unmodifiableList(new ArrayList<>(elementTypes));
    }

    /**
     * Creates a new instance.
     * @param elementTypes the element types
     */
    public UnionType(FieldType... elementTypes) {
        this(Arrays.asList(elementTypes));
    }

    @JsonCreator
    UnionType(
            @JsonProperty("name") TypeName name,
            @JsonProperty(K_ELEMENTS) List<? extends FieldType> elementTypes) {
        this(elementTypes);
        if (name != TypeName.UNION) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid union type name: {0}",
                    name));
        }
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return TypeName.UNION;
    }

    @Override
    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder();
        buf.append(getTypeName().name());
        buf.append('<');
        for (int i = 0, n = elementTypes.size(); i < n; i++) {
            if (i != 0) {
                buf.append(',').append(' ');
            }
            buf.append(elementTypes.get(i).getQualifiedName());
        }
        buf.append('>');
        return buf.toString();
    }

    /**
     * Returns the element types.
     * @return the element types
     */
    public List<FieldType> getElementTypes() {
        return elementTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(elementTypes);
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
        UnionType other = (UnionType) obj;
        if (!Objects.equals(elementTypes, other.elementTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
