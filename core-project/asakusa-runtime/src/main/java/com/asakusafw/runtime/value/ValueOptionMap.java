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
package com.asakusafw.runtime.value;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * An abstract implementation of map of references to other properties.
 * @param <K> the key type
 * @param <V> the value type
 * @since 0.9.2
 */
public abstract class ValueOptionMap<K, V extends ValueOption<V>> extends AbstractMap<K, V> {

    private EntrySet<K, V> entries;

    /**
     * Builds a key set.
     * @param <T> the key type
     * @param keys the sequence of keys
     * @return the built key set
     */
    @SafeVarargs
    public static <T> Set<T> keys(T... keys) {
        if (keys.length == 0) {
            return Collections.emptySet();
        } else if (keys.length == 1) {
            return Collections.singleton(keys[0]);
        } else {
            Set<T> results = new LinkedHashSet<>(keys.length * 2);
            Collections.addAll(results, keys);
            return Collections.unmodifiableSet(results);
        }
    }

    @Override
    public abstract V get(Object key);

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the given key is not in this map
     */
    @Override
    public V put(K key, V value) {
        V property = get(key);
        if (property == null) {
            throw new IllegalArgumentException(String.valueOf(key));
        }
        return update(property, value);
    }

    @Override
    public abstract Set<K> keySet();

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return entrySet0();
    }

    private EntrySet<K, V> entrySet0() {
        if (entries == null) {
            Set<K> keys = keySet();
            @SuppressWarnings("unchecked")
            Map.Entry<K, V>[] array = (Map.Entry<K, V>[]) new Map.Entry<?, ?>[keys.size()];
            int index = 0;
            for (K key : keys) {
                V value = get(key);
                assert value != null;
                array[index++] = new Entry<>(key, value);
            }
            entries = new EntrySet<>(array);
        }
        return entries;
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @SuppressWarnings("deprecation")
    static <V extends ValueOption<V>> V update(V property, V newValue) {
        property.copyFrom(newValue);
        return property;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        entrySet0().forEach(action);
    }

    private static final class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

        private final Map.Entry<K, V>[] entries;

        EntrySet(Map.Entry<K, V>[] entries) {
            this.entries = entries;
        }

        void forEach(BiConsumer<? super K, ? super V> action) {
            for (Map.Entry<K, V> e : entries) {
                action.accept(e.getKey(), e.getValue());
            }
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            Map.Entry<K, V>[] es = entries;
            return new Iterator<Map.Entry<K, V>>() {
                private int index = 0;
                @Override
                public boolean hasNext() {
                    return index < es.length;
                }
                @Override
                public Map.Entry<K, V> next() {
                    if (index >= es.length) {
                        throw new NoSuchElementException();
                    }
                    return es[index++];
                }
            };
        }

        @Override
        public int size() {
            return entries.length;
        }
    }

    private static final class Entry<K, V extends ValueOption<V>> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        Entry(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V newValue) {
            return ValueOptionMap.update(getValue(), newValue);
        }
    }
}
