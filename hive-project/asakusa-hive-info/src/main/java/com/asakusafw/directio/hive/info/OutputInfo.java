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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Direct I/O Hive output.
 * @since 0.8.1
 */
public class OutputInfo {

    private static final String K_LOCATION = "location";

    private static final String K_TABLE = "table";

    private final LocationInfo location;

    private final TableInfo table;

    /**
     * Creates a new instance.
     * @param location the output location
     * @param table the table structure
     */
    public OutputInfo(
            @JsonProperty(value = K_LOCATION, required = true) LocationInfo location,
            @JsonProperty(value = K_TABLE, required = true) TableInfo table) {
        this.location = location;
        this.table = table;
    }

    /**
     * Returns the location.
     * @return the location
     */
    @JsonProperty(K_LOCATION)
    public LocationInfo getLocation() {
        return location;
    }

    /**
     * Returns the table.
     * @return the table
     */
    @JsonProperty(K_TABLE)
    public TableInfo getTable() {
        return table;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(location);
        result = prime * result + Objects.hashCode(table);
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
        OutputInfo other = (OutputInfo) obj;
        if (!Objects.equals(location, other.location)) {
            return false;
        }
        if (!Objects.equals(table, other.table)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Outut(%s@%s)",
                getTable(),
                getLocation());
    }
}
