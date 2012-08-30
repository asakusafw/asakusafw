/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.utils.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities about {@link Map}.
 */
public final class Maps {

    /**
     * Creates and returns an empty map.
     * The map is modifiable.
     * @param <K> the key type
     * @param <V> the value type
     * @return created map
     */
    public static <K, V> Map<K, V> create() {
        return new HashMap<K, V>();
    }

    /**
     * Returns a copy of the map.
     * The returned map is modifiable.
     * @param <K> the key type
     * @param <V> the value type
     * @param map target map
     * @return created map
     */
    public static <K, V> Map<K, V> from(Map<? extends K, ? extends V> map) {
        return new HashMap<K, V>(map);
    }

    /**
     * Returns a freezed map.
     * The returned map is not modifiable.
     * @param <K> the key type
     * @param <V> the value type
     * @param map target map
     * @return created map
     */
    public static <K, V> Map<K, V> freeze(Map<? extends K, ? extends V> map) {
        return Collections.unmodifiableMap(from(map));
    }

    /**
     * Returns a transposed map.
     * If the map contains duplicated value, the transposed map uses only one of them.
     * The map is modifiable.
     * @param <K> the key type
     * @param <V> the value type
     * @param map target map
     * @return transposed map
     */
    public static <K, V> Map<V, List<K>> transpose(Map<? extends K, ? extends V> map) {
        if (map == null) {
            throw new IllegalArgumentException("map must not be null"); //$NON-NLS-1$
        }
        Map<V, List<K>> results = create();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            addToList(results, entry.getValue(), entry.getKey());
        }
        return results;
    }

    /**
     * Returns a map whose key and value have same type.
     * The map is modifiable.
     * @param pairs array contains key and value alternatively
     * @param <T> the key and value type
     * @return created map
     */
    public static <T> Map<T, T> pairs(T... pairs) {
        if (pairs == null) {
            throw new IllegalArgumentException("pairs must not be null"); //$NON-NLS-1$
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("pairs must ");
        }
        Map<T, T> result = new HashMap<T, T>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(pairs[i + 0], pairs[i + 1]);
        }
        return result;
    }

    /**
     * Add the value to the list which is in the map with the key.
     * If the map does not contains the key, this first put a new list into the map.
     * @param <K> the key type
     * @param <V> the value type
     * @param map target map
     * @param key the key of target list
     * @param value the value to add to target list
     */
    public static <K, V> void addToList(Map<? super K, List<V>> map, K key, V value) {
        if (map == null) {
            throw new IllegalArgumentException("map must not be null"); //$NON-NLS-1$
        }
        List<V> list = map.get(key);
        if (list == null) {
            list = Lists.create();
            map.put(key, list);
        }
        list.add(value);
    }

    /**
     * Add the value to the set which is in the map with the key.
     * If the map does not contains the key, this first put a new set into the map.
     * @param <K> the key type
     * @param <V> the value type
     * @param map target map
     * @param key the key of target list
     * @param value the value to add to target list
     */
    public static <K, V> void addToSet(Map<? super K, Set<V>> map, K key, V value) {
        if (map == null) {
            throw new IllegalArgumentException("map must not be null"); //$NON-NLS-1$
        }
        Set<V> set = map.get(key);
        if (set == null) {
            set = new HashSet<V>();
            map.put(key, set);
        }
        set.add(value);
    }

    private Maps() {
        return;
    }
}
