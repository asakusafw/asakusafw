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
package com.asakusafw.workflow.hadoop.bootstrap;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents a set of environment variables.
 * @since 0.10.0
 */
public class Environment {

    static final boolean CASE_SENSITIVE = isCaseSensitiveEnvironmentVariables();

    private final Map<String, String> entries = CASE_SENSITIVE
            ? new LinkedHashMap<>()
            : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private static boolean isCaseSensitiveEnvironmentVariables() {
        Map<String, String> env = System.getenv();
        if (env instanceof SortedMap<?, ?>) {
            Comparator<? super String> comparator = ((SortedMap<String, String>) env).comparator();
            return comparator == null || comparator.compare("a", "A") != 0;
        }
        if (env.isEmpty() == false) {
            for (String name : env.keySet()) {
                String upper = name.toUpperCase(Locale.ENGLISH);
                String lower = name.toLowerCase(Locale.ENGLISH);
                if (upper.equals(lower) == false) {
                    String value = System.getenv(name);
                    String upperValue = System.getenv(upper);
                    String lowerValue = System.getenv(lower);
                    return Objects.equals(value, upperValue) == false || Objects.equals(value, lowerValue) == false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a new system environment variables.
     * @return a copy of system environment variables
     */
    public static Environment system() {
        Environment result = new Environment();
        result.entries.putAll(System.getenv());
        return result;
    }

    /**
     * Returns a value of environment variable.
     * @param key key of the target environment variable
     * @return the corresponded environmet variable value, or {@code empty} if it is not defined
     */
    public Optional<String> find(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    /**
     * Returns the view of environment variables.
     * @return the environment variables
     */
    public Map<String, String> entries() {
        return entries;
    }
}
