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
package com.asakusafw.workflow.executor.basic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.asakusafw.workflow.executor.ExecutionContext;

/**
 * A basic implementation of {@link ExecutionContext}.
 * @since 0.10.0
 */
public class BasicExecutionContext implements ExecutionContext {

    private ClassLoader classLoader = getClass().getClassLoader();

    private final Map<String, String> configurations = new LinkedHashMap<>();

    private final Map<String, String> environmentVariables = new LinkedHashMap<>();

    private final Map<Class<?>, Object> resources = new LinkedHashMap<>();

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
