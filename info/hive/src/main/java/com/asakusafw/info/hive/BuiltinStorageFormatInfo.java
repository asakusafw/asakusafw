/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a built-in storage format.
 * Format category must be {@link com.asakusafw.info.hive.StorageFormatInfo.Category#BUILTIN}.
 * @since 0.8.1
 */
public final class BuiltinStorageFormatInfo implements StorageFormatInfo {

    private static final Map<FormatKind, BuiltinStorageFormatInfo> ENTITY_MAP;
    static {
        Map<FormatKind, BuiltinStorageFormatInfo> map = new EnumMap<>(FormatKind.class);
        for (FormatKind kind : FormatKind.values()) {
            if (kind.getCategory() == Category.BUILTIN) {
                map.put(kind, new BuiltinStorageFormatInfo(kind));
            }
        }
        ENTITY_MAP = Collections.unmodifiableMap(map);
    }

    private BuiltinStorageFormatInfo(FormatKind kind) {
        this.formatKind = kind;
    }

    private final FormatKind formatKind;

    /**
     * Returns a built-in storage format that has the specified format kind.
     * @param kind the format kind
     * @return the corresponded format info
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static BuiltinStorageFormatInfo of(
            @JsonProperty(value = "kind", required = true) FormatKind kind) {
        if (kind.getCategory() != Category.BUILTIN) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported built-in storage format: {0}",
                    kind));
        }
        BuiltinStorageFormatInfo format = ENTITY_MAP.get(kind);
        assert format != null;
        return format;
    }

    @JsonProperty("kind")
    @Override
    public FormatKind getFormatKind() {
        return formatKind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(formatKind);
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
        BuiltinStorageFormatInfo other = (BuiltinStorageFormatInfo) obj;
        if (Objects.equals(formatKind, other.formatKind) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getFormatKind().toString();
    }
}
