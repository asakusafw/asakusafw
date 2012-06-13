/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.util;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Utilities for {@link Properties}.
 * @since 0.2.2
 */
public final class PropertiesUtil {

    /**
     * Removes entries which have the specified prefix in their key.
     * @param properties target properties
     * @param prefix target prefix
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public static void removeKeyPrefix(Properties properties, String prefix) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        for (Iterator<?> iter = properties.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            if ((key instanceof String) == false) {
                continue;
            }
            String name = (String) key;
            if (name.startsWith(prefix)) {
                iter.remove();
            }
        }
    }

    /**
     * Creates property pairs which has the specified prefix their key.
     * The created map is lexicographic ordered by each key name,
     * and trimmed the specified prefix.
     * @param properties target properties
     * @param prefix target prefix
     * @return the created map
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public static NavigableMap<String, String> createPrefixMap(Map<?, ?> properties, String prefix) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        NavigableMap<String, String> results = new TreeMap<String, String>();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if ((entry.getKey() instanceof String) == false || (entry.getValue() instanceof String) == false) {
                continue;
            }
            String name = (String) entry.getKey();
            if (name.startsWith(prefix) == false) {
                continue;
            }
            results.put(name.substring(prefix.length()), (String) entry.getValue());
        }
        return results;
    }

    /**
     * Checks whether the specified key is absent in the properties.
     * @param properties target properties
     * @param key target key
     * @throws IllegalArgumentException if the key exists in the properties, or any parameter is {@code null}
     */
    public static void checkAbsentKey(Properties properties, String key) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (properties.containsKey(key)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Target properties already contain \"{0}\"",
                    key));
        }
    }

    /**
     * Checks whether keys with the specified prefix are absent in the properties.
     * @param properties target properties
     * @param keyPrefix target key prefix
     * @throws IllegalArgumentException if the such key exists in the properties, or any parameter is {@code null}
     */
    public static void checkAbsentKeyPrefix(Properties properties, String keyPrefix) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (keyPrefix == null) {
            throw new IllegalArgumentException("keyPrefix must not be null"); //$NON-NLS-1$
        }
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(keyPrefix)) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Target properties already contain \"{0}\"",
                        key));
            }
        }
    }

    private PropertiesUtil() {
        return;
    }
}
