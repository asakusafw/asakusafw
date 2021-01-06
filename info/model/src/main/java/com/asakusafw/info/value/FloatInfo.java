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
package com.asakusafw.info.value;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a {@code float} value.
 * @since 0.9.2
 */
public final class FloatInfo implements NumberInfo {

    static final String KIND = "float"; //$NON-NLS-1$

    @JsonProperty(Constants.ID_VALUE)
    private final float value;

    private FloatInfo(float value) {
        this.value = value;
    }

    /**
     * Returns an instance for the value.
     * @param value the value
     * @return the instance
     */
    @JsonCreator
    public static FloatInfo of(@JsonProperty(Constants.ID_VALUE) float value) {
        return new FloatInfo(value);
    }

    @Override
    public Kind getKind() {
        return Kind.FLOAT;
    }

    @Override
    public Float getObject() {
        return getValue();
    }

    /**
     * Returns the value.
     * @return the value
     */
    public float getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getKind());
        result = prime * result + Float.hashCode(value);
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
        if (!(obj instanceof FloatInfo)) {
            return false;
        }
        FloatInfo other = (FloatInfo) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getKind(), getObject());
    }
}
