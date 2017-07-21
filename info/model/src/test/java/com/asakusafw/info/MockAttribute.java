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
package com.asakusafw.info;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mock {@link Attribute}.
 */
public class MockAttribute implements Attribute {

    /**
     * The attribute ID.
     */
    public static final String ID = "mock";

    private final String value;

    /**
     * Creates a new instance.
     * @param value the value
     */
    public MockAttribute(String value) {
        this.value = value;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    static MockAttribute of(
            @JsonProperty("id") String id,
            @JsonProperty("value") String value) {
        if (Objects.equals(id, ID) == false) {
            throw new IllegalArgumentException();
        }
        return new MockAttribute(value);
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
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
        MockAttribute other = (MockAttribute) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return String.format("Mock(%s)", value); //$NON-NLS-1$
    }
}
