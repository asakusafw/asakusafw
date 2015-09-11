/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.core;

/**
 * An abstract super interface of configuration information for runtime resources.
 */
public interface ResourceConfiguration {

    /**
     * Returns a configuration value for the specified key.
     * @param keyName the key name
     * @param defaultValue the default value (nullable)
     * @return the configuration value for the specified key, or the {@code defaultValue} if it is not defined
     * @throws IllegalArgumentException if {@code keyName} is {@code null}
     */
    String get(String keyName, String defaultValue);

    /**
     * Sets a configuration value for the specified key.
     * @param keyName the key name
     * @param value the configuration value, or {@code null} to delete its property
     * @throws IllegalArgumentException if {@code keyName} is {@code null}
     */
    void set(String keyName, String value);

    /**
     * Returns a class loader for this configuration.
     * @return class loader
     */
    ClassLoader getClassLoader();
}