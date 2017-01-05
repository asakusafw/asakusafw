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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents {@code ROW FORMAT} in Hive.
 * @since 0.8.1
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = false // all sub-types are identical with 'kind'
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DelimitedRowFormatInfo.class, name = "DELIMITED"),
    @JsonSubTypes.Type(value = SerdeRowFormatInfo.class, name = "SERDE"),
})
public interface RowFormatInfo {

    /**
     * Returns the row format kind.
     * @return the format kind
     */
    @JsonProperty("kind")
    FormatKind getFormatKind();

    /**
     * Represents a row format kind.
     * @since 0.8.1
     */
    enum FormatKind {

        /**
         * The delimited rows.
         */
        DELIMITED,

        /**
         * Using user defined serialization facilities.
         */
        SERDE,
    }
}
