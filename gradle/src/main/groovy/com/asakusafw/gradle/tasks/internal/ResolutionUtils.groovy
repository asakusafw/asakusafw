/*
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
package com.asakusafw.gradle.tasks.internal

import java.util.concurrent.Callable

/**
 * Utilities for resolving values.
 * @since 0.6.1
 */
final class ResolutionUtils {

    /**
     * Resolves a value to a string.
     * @param value the target value
     * @return the resolved string
     */
    static String resolveToString(Object value) {
        if (value == null) {
            return null
        } else if (value instanceof String) {
            return value
        } else if (value instanceof Closure<?>) {
            Closure<?> c = value
            return resolveToString(c.call())
        } else if (value instanceof Callable<?>) {
            Callable<?> c = value
            return resolveToString(c.call())
        } else {
            return String.valueOf(value)
        }
    }

    /**
     * Resolves values to a string list.
     * @param value the target values
     * @return the resolved string list
     */
    static List<String> resolveToStringList(Iterable<?> values) {
        List<String> results = []
        for (Object arg in values) {
            resolveInto(arg, results)
        }
        return results
    }

    /**
     * Resolves value map into a map which strings maps to strings.
     * If key or value is null in original map, the resulting map does not contain it.
     * @param value the target values
     * @return the resolved string list
     */
    static Map<String, String> resolveToStringMap(Map<?, ?> values) {
        Map<String, String> results = [:]
        for (Map.Entry<?, ?> entry in values.entrySet()) {
            String key = resolveToString(entry.key)
            String value = resolveToString(entry.value)
            if (key != null && value != null) {
                results.put key, value
            }
        }
        return results
    }

    private static void resolveInto(Object arg, List<String> results) {
        if (arg == null) {
            // skip this entry
        } else if (arg instanceof String) {
            results.add(arg)
        } else if (arg instanceof Closure<?>) {
            Closure<?> c = arg
            resolveInto(c.call(), results)
        } else if (arg instanceof Callable<?>) {
            Callable<?> c = arg
            resolveInto(c.call(), results)
        } else if (arg instanceof Iterable<?>) {
            for (Object element in (Iterable<?>) arg) {
                resolveInto(element, results)
            }
        } else {
            resolveInto(String.valueOf(arg), results)
        }
    }

    private ResolutionUtils() {
    }
}
