/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.workflow.executor;

import java.util.Map;
import java.util.Optional;

/**
 * A base context of {@link TaskExecutor}.
 * @since 0.10.0
 */
public interface ExecutionContext {

    /**
     * Returns the class loader to load testing peripherals.
     * @return the class loader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the platform configurations.
     * @return the platform configurations
     */
    Map<String, String> getConfigurations();

    /**
     * Returns the environment variables.
     * @return the environment variables
     */
    Map<String, String> getEnvironmentVariables();

    /**
     * Returns a resource for the type.
     * @param <T> the provider type
     * @param type the provider type
     * @return a resource for the type, or {@code empty} if it is not defined
     */
    <T> Optional<T> findResource(Class<T> type);
}