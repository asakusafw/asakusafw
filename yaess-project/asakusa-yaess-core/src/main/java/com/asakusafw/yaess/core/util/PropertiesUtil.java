/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.yaess.core.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Utilities for {@link Properties}.
 * @since 0.2.3
 */
public final class PropertiesUtil {

    /**
     * Collect the {@code child keys} from the properties.
     * If the parent prefix is {@code "parent."} and delimiter is {@code "."},
     * then the child keys are in form of {@code "parent.<child-name>"}.
     * @param properties target properties
     * @param parentPrefix parent key prefix (may end with {@code delimiter})
     * @param delimitier a generation delimiter token (ordinally, {@code "."})
     * @return the child keys (includes parent key in head)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Set<String> getChildKeys(Map<?, ?> properties, String parentPrefix, String delimitier) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (parentPrefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (delimitier == null) {
            throw new IllegalArgumentException("delimitier must not be null"); //$NON-NLS-1$
        }
        int parentLength = parentPrefix.length();
        Set<String> results = new TreeSet<String>();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if ((entry.getKey() instanceof String) == false || (entry.getValue() instanceof String) == false) {
                continue;
            }
            String name = (String) entry.getKey();
            if (name.startsWith(parentPrefix) == false) {
                continue;
            }
            int index = name.indexOf(delimitier, parentLength);
            if (index < 0) {
                results.add(name);
            } else {
                results.add(name.substring(0, index));
            }
        }
        return results;
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

    private PropertiesUtil() {
        return;
    }
}
