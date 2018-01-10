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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive column.
 * @since 0.8.1
 */
public class ColumnInfo {

    private static final String K_NAME = "name";

    private static final String K_TYPE = "type";

    private static final String K_COMMENT = "comment";

    private final String name;

    private final FieldType type;

    private final String comment;

    /**
     * Creates a new instance.
     * @param name the column name
     * @param type the column type
     * @param comment the column comment (nullable)
     */
    @JsonCreator
    public ColumnInfo(
            @JsonProperty(value = K_NAME, required = true) String name,
            @JsonProperty(value = K_TYPE, required = true) FieldType type,
            @JsonProperty(value = K_COMMENT, required = false) String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    /**
     * Creates a new instance.
     * @param name the column name
     * @param type the column type
     */
    public ColumnInfo(String name, FieldType type) {
        this(name, type, null);
    }

    /**
     * Returns the column name.
     * @return the column name
     */
    @JsonProperty(K_NAME)
    public String getName() {
        return name;
    }

    /**
     * Returns the column type.
     * @return the column type
     */
    @JsonProperty(K_TYPE)
    public FieldType getType() {
        return type;
    }

    /**
     * Returns the column comment.
     * @return the column comment, or {@code null} if it is not defined
     */
    @JsonProperty(K_COMMENT)
    public String getComment() {
        return comment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(type);
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
        ColumnInfo other = (ColumnInfo) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(type, other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "%s:%s", //$NON-NLS-1$
                getName(),
                getType());
    }
}
