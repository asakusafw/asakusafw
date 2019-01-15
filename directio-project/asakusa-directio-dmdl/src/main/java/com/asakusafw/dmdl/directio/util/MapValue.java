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
package com.asakusafw.dmdl.directio.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstAttributeValueMap;

/**
 * Represents a map.
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.9.1
 */
public final class MapValue<K, V> implements Iterable<MapValue.Entry<K, V>> {

    private final AstAttributeElement declaration;

    private final List<Entry<K, V>> entries = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param declaration the original declaration
     */
    public MapValue(AstAttributeElement declaration) {
        this.declaration = declaration;
    }

    /**
     * Adds an entry.
     * @param decl the entry declaration
     * @param key the key
     * @param value the value
     */
    public void add(AstAttributeValueMap.Entry decl, K key, V value) {
        entries.add(new Entry<>(decl, key, value));
    }

    /**
     * Returns the declaration.
     * @return the declaration
     */
    public AstAttributeElement getDeclaration() {
        return declaration;
    }

    /**
     * Returns the entries.
     * @return the entries
     */
    public List<Entry<K, V>> getEntries() {
        return entries;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entries.iterator();
    }

    @Override
    public String toString() {
        return entries.stream()
                .map(Entry::toString)
                .collect(Collectors.joining(", ", "{", "}")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * An entry of {@link MapValue}.
     * @since 0.9.1
     * @param <K> the key type
     * @param <V> the value type
     */
    public static final class Entry<K, V> {

        private final AstAttributeValueMap.Entry declaration;

        private final K key;

        private final V value;

        Entry(com.asakusafw.dmdl.model.AstAttributeValueMap.Entry declaration, K key, V value) {
            this.declaration = declaration;
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the declaration.
         * @return the declaration
         */
        public AstAttributeValueMap.Entry getDeclaration() {
            return declaration;
        }

        /**
         * Returns the key.
         * @return the key
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value.
         * @return the value
         */
        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", key, value); //$NON-NLS-1$
        }
    }
}
