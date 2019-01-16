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
package com.asakusafw.dmdl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.Region;

/**
 * Represents a map of string to attribute values.
 * @since 0.9.1
 */
public class AstAttributeValueMap extends AbstractAstNode implements AstAttributeValue {

    private final Region region;

    /**
     * Entries in this map.
     */
    public final List<Entry> entries;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param entries the entries in this map
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstAttributeValueMap(Region region, List<? extends Entry> entries) {
        this.region = region;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    @Override
    public Region getRegion() {
        return region;
    }

    /**
     * Returns entries as map object.
     * @return a related map
     */
    public Map<Object, AstAttributeValue> asMap() {
        Map<Object, AstAttributeValue> results = new LinkedHashMap<>();
        for (Entry entry : entries) {
            results.put(entry.key.toValue(), entry.value);
        }
        return results;

    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitAttributeValueMap(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(entries);
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
        AstAttributeValueMap other = (AstAttributeValueMap) obj;
        if (!Objects.equals(entries, other.entries)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return entries.stream()
                .map(Entry::toString)
                .collect(Collectors.joining("{", ",", "}")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * An entry of {@link AstAttributeValueMap}.
     * @since 0.9.1
     */
    public static class Entry {

        /**
         * The entry key.
         */
        public final AstLiteral key;

        /**
         * The entry value.
         */
        public final AstAttributeValue value;

        /**
         * Creates a new instance.
         * @param key the entry key
         * @param value the entry value
         */
        public Entry(AstLiteral key, AstAttributeValue value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(key);
            result = prime * result + Objects.hashCode(value);
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
            Entry other = (Entry) obj;
            if (!Objects.equals(key, other.key)) {
                return false;
            }
            if (!Objects.equals(value, other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.format("%s : %s", key, value); //$NON-NLS-1$
        }
    }
}
