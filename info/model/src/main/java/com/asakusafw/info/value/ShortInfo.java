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
package com.asakusafw.info.value;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a {@code short} value.
 * @since 0.9.2
 */
public final class ShortInfo implements NumberInfo {

    static final String KIND = "short"; //$NON-NLS-1$

    @JsonProperty(Constants.ID_VALUE)
    private final short value;

    private ShortInfo(short value) {
        this.value = value;
    }

    /**
     * Returns an instance for the value.
     * @param value the value
     * @return the instance
     */
    @JsonCreator
    public static ShortInfo of(@JsonProperty(Constants.ID_VALUE) short value) {
        return new ShortInfo(value);
    }

    @Override
    public Kind getKind() {
        return Kind.SHORT;
    }

    @Override
    public Short getObject() {
        return getValue();
    }

    /**
     * Returns the value.
     * @return the value
     */
    public short getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getKind());
        result = prime * result + Short.hashCode(value);
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
        if (!(obj instanceof ShortInfo)) {
            return false;
        }
        ShortInfo other = (ShortInfo) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getKind(), getObject());
    }
}
