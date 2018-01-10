/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An abstract super interface of Direct I/O Hive ports.
 * @since 0.10.0
 */
public interface HivePortInfo extends TableInfo.Provider {

    /**
     * The property key of port name.
     */
    String ID_NAME = "name";

    /**
     * The property key of port description class.
     */
    String ID_DESCRIPTION = "description";

    /**
     * The property key of location information.
     */
    String ID_LOCATION = "location";

    /**
     * The property key of schema information.
     */
    String ID_SCHEMA = "schema";

    /**
     * Returns the port name.
     * @return the port name
     */
    @JsonProperty(ID_NAME)
    String getName();

    /**
     * Returns the description class name.
     * @return the description class name
     */
    @JsonProperty(ID_DESCRIPTION)
    String getDescriptionClass();

    /**
     * Returns the location information.
     * @return the location information
     */
    @JsonProperty(ID_LOCATION)
    LocationInfo getLocation();

    /**
     * Returns the schema information.
     * @return the schema information
     */
    @Override
    @JsonProperty(ID_SCHEMA)
    TableInfo getSchema();
}
