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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive struct type.
 * Each type category must be {@link com.asakusafw.directio.hive.info.FieldType.Category#STRUCT}.
 * @since 0.8.1
 */
public class StructType implements FieldType {

    private static final String K_MEMBERS = "members";

    private final List<ColumnInfo> members;

    /**
     * Creates a new instance.
     * @param members the struct type members
     */
    public StructType(List<? extends ColumnInfo> members) {
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
    }

    /**
     * Creates a new instance.
     * @param members the struct type members
     */
    public StructType(ColumnInfo... members) {
        this(Arrays.asList(members));
    }

    @JsonCreator
    StructType(
            @JsonProperty(value = "name", required = true) TypeName name,
            @JsonProperty(value = K_MEMBERS, required = true) List<? extends ColumnInfo> members) {
        this(members);
        if (name != TypeName.STRUCT) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "invalid struct type name: {0}",
                    name));
        }
    }

    @JsonProperty("name")
    @Override
    public TypeName getTypeName() {
        return TypeName.STRUCT;
    }

    @Override
    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder();
        buf.append(getTypeName().name());
        buf.append('<');
        for (int i = 0, n = members.size(); i < n; i++) {
            if (i != 0) {
                buf.append(',').append(' ');
            }
            ColumnInfo member = members.get(i);
            buf.append(member.getName());
            buf.append(':');
            buf.append(member.getType().getQualifiedName());
        }
        buf.append('>');
        return buf.toString();
    }

    /**
     * Returns the members.
     * @return the members
     */
    public List<ColumnInfo> getMembers() {
        return members;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(members);
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
        StructType other = (StructType) obj;
        if (!Objects.equals(members, other.members)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
