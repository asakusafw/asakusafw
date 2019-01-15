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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Hive input port information.
 * @since 0.10.0
 */
public class HiveInputInfo implements HivePortInfo {

    private final String name;

    private final String descriptionClass;

    private final LocationInfo location;

    private final TableInfo schema;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param descriptionClass the description class (nullable)
     * @param location the location information
     * @param schema the schema information
     */
    public HiveInputInfo(String name, String descriptionClass, LocationInfo location, TableInfo schema) {
        this.name = name;
        this.descriptionClass = descriptionClass;
        this.location = location;
        this.schema = schema;
    }

    @JsonCreator
    static HiveInputInfo restore(
            @JsonProperty(ID_NAME) String name,
            @JsonProperty(ID_DESCRIPTION) String descriptionClass,
            @JsonProperty(ID_LOCATION) LocationInfo location,
            @JsonProperty(ID_SCHEMA) TableInfo schema) {
        return new HiveInputInfo(name, descriptionClass, location, schema);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptionClass() {
        return descriptionClass;
    }

    @Override
    public LocationInfo getLocation() {
        return location;
    }

    @Override
    public TableInfo getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getClass());
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(descriptionClass);
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
        HiveInputInfo other = (HiveInputInfo) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(descriptionClass, other.descriptionClass)
                && Objects.equals(location, other.location)
                && Objects.equals(schema, other.schema);
    }

    @Override
    public String toString() {
        return String.format(
                "HiveInput(%s@%s)",
                getSchema(),
                getLocation());
    }
}
