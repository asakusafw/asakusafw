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
package com.asakusafw.workflow.executor.basic;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import com.asakusafw.workflow.executor.ExecutionContext;

/**
 * A basic implementation of {@link ExecutionContext}.
 * @since 0.10.0
 */
public class BasicExecutionContext implements ExecutionContext {

    static final boolean CASE_SENSITIVE = isCaseSensitiveEnvironmentVariables();

    private ClassLoader classLoader = getClass().getClassLoader();

    private final Map<String, String> configurations = new LinkedHashMap<>();

    private final Map<String, String> environmentVariables = CASE_SENSITIVE
            ? new LinkedHashMap<>()
            : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final Map<Class<?>, Object> resources = new LinkedHashMap<>();

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

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Configures the class loader.
     * @param newValue the new class loader
     * @return this
     */
    public BasicExecutionContext withClassLoader(ClassLoader newValue) {
        this.classLoader = newValue;
        return this;
    }

    @Override
    public Map<String, String> getConfigurations() {
        return configurations;
    }

    /**
     * Configure configurations.
     * @param configurator the configurator
     * @return this
     */
    public BasicExecutionContext withConfigurations(Consumer<Map<String, String>> configurator) {
        configurator.accept(configurations);
        return this;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Configures environment variables.
     * @param configurator the configurator
     * @return this
     */
    public BasicExecutionContext withEnvironmentVariables(Consumer<Map<String, String>> configurator) {
        configurator.accept(environmentVariables);
        return this;
    }

    @Override
    public <T> Optional<T> findResource(Class<T> type) {
        return Optional.ofNullable(resources.get(type))
                .map(type::cast);
    }

    /**
     * Configures resources.
     * @param <T> the resource type
     * @param type the resource type
     * @param value the resource value
     * @return this
     */
    public <T> BasicExecutionContext withResource(Class<T> type, T value) {
        resources.put(type, value);
        return this;
    }
}
