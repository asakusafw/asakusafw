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
package com.asakusafw.info.hive;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Hive SERDE row format.
 * @since 0.8.1
 */
public class SerdeRowFormatInfo implements RowFormatInfo {

    private final String name;

    private final Map<String, String> properties;

    /**
     * Creates a new instance.
     * @param name the serde name
     * @param properties the serde properties (nullable)
     */
    @JsonCreator
    public SerdeRowFormatInfo(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "properties", required = false) Map<String, String> properties) {
        this.name = name;
        this.properties = properties == null || properties.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    /**
     * Creates a new instance.
     * @param name the serde name
     */
    public SerdeRowFormatInfo(String name) {
        this(name, null);
    }

    @JsonProperty("kind")
    @Override
    public FormatKind getFormatKind() {
        return FormatKind.SERDE;
    }

    /**
     * Returns the serde name.
     * @return the serde name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the serde properties.
     * @return the serde properties, or {@code null} if the no properties are defined
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(properties);
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
        SerdeRowFormatInfo other = (SerdeRowFormatInfo) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(properties, other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(name=\"%s\", properties=%s)", //$NON-NLS-1$
                getFormatKind().toString(),
                getName(),
                getProperties());
    }
}
