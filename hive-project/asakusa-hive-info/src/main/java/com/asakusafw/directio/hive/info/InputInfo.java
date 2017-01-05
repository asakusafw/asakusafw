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
package com.asakusafw.directio.hive.info;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Direct I/O Hive input.
 * @since 0.8.1
 */
public class InputInfo implements TableInfo.Provider {

    private static final String K_LOCATION = "location";

    private static final String K_SCHEMA = "schema";

    private final LocationInfo location;

    private final TableInfo schema;

    /**
     * Creates a new instance.
     * @param location the input location
     * @param schema the table structure
     */
    public InputInfo(
            @JsonProperty(value = K_LOCATION, required = true) LocationInfo location,
            @JsonProperty(value = K_SCHEMA, required = true) TableInfo schema) {
        this.location = location;
        this.schema = schema;
    }

    /**
     * Returns the location.
     * @return the location
     */
    @JsonProperty(K_LOCATION)
    public LocationInfo getLocation() {
        return location;
    }

    @Override
    @JsonProperty(K_SCHEMA)
    public TableInfo getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(location);
        result = prime * result + Objects.hashCode(schema);
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
        InputInfo other = (InputInfo) obj;
        if (!Objects.equals(location, other.location)) {
            return false;
        }
        if (!Objects.equals(schema, other.schema)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Input(%s@%s)",
                getSchema(),
                getLocation());
    }
}
