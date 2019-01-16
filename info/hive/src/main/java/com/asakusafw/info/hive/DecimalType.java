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
 * Represents a Hive decimal type.
 * Each type category must be {@link com.asakusafw.info.hive.FieldType.Category#DECIMAL}.
 * @since 0.8.1
 */
public class DecimalType implements FieldType {

    private static final String K_PRECISION = "precision";

    private static final String K_SCALE = "scale";

    private final TypeName typeName;

    private final int precision;

    private final int scale;

    /**
     * Creates a new instance.
     * @param name the type name
     * @param precision the decimal precision
     * @param scale the decimal scale
     */
    @JsonCreator
    public DecimalType(
            @JsonProperty(value = "name", required = true) TypeName name,
            @JsonProperty(value = K_PRECISION, required = true) int precision,
            @JsonProperty(value = K_SCALE, required = true) int scale) {
        if (name.getCategory() != Category.DECIMAL) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported decimal type: {0}",
                    name));
        }
        this.typeName = name;
        this.precision = precision;
        this.scale = scale;
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    @Override
    public String getQualifiedName() {
        return String.format(
                "%s(%d, %d)", //$NON-NLS-1$
                getTypeName(),
                getPrecision(),
                getScale());
    }

    /**
     * Returns the precision.
     * @return the precision
     */
    @JsonProperty(K_PRECISION)
    public int getPrecision() {
        return precision;
    }

    /**
     * Returns the scale.
     * @return the scale
     */
    @JsonProperty(K_SCALE)
    public int getScale() {
        return scale;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(typeName);
        result = prime * result + precision;
        result = prime * result + scale;
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
        DecimalType other = (DecimalType) obj;
        if (Objects.equals(typeName, other.typeName) == false) {
            return false;
        }
        if (precision != other.precision) {
            return false;
        }
        if (scale != other.scale) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
