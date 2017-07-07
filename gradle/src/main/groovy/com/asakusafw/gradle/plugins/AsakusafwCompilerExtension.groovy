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
package com.asakusafw.gradle.plugins

/**
 * An extension object for Asakusa DSL compiler framework.
 * @since 0.7.4
 * @version 0.8.1
 */
class AsakusafwCompilerExtension {

    /**
     * The base output directory for compilation results.
     * This path will be resolved using {@code project.file(...)}.
     */
    Object outputDirectory

    /**
     * The accepting batch class name pattern ({@code "*"} as a wildcard character).
     * This can be a list of class name patterns.
     */
    Object include

    /**
     * The ignoring batch class name pattern ({@code "*"} as a wildcard character).
     * This can be a list of class name patterns.
     */
    Object exclude

    /**
     * The custom runtime working directory URI.
     */
    String runtimeWorkingDirectory

    /**
     * The compiler properties.
     */
    Map<Object, Object> compilerProperties = [:]

    /**
     * The custom batch ID prefix for applications.
     */
    String batchIdPrefix

    /**
     * Whether fails on compilation errors or not.
     */
    boolean failOnError

    /**
     * Adds compiler properties.
     * @param additions the additional compiler properties
     * @since 0.8.1
     */
    void compilerProperties(Map<?, ?> additions) {
        additions.each { k, v ->
            compilerProperty(k, v)
        }
    }

    /**
     * Adds a compiler property.
     * @param additionalOptions the additional compiler properties
     * @since 0.8.1
     */
    void compilerProperty(Object key, Object value) {
        getCompilerProperties().put(key, value)
    }

    /**
     * Returns the compiler properties.
     * This is alias of {@link #compilerProperties}.
     * @return the current compiler properties
     * @since 0.8.1
     */
    Map<Object, Object> getOptions() {
        return getCompilerProperties()
    }

    /**
     * Sets the compiler properties.
     * This is alias of {@link #compilerProperties}.
     * @param options the map of compiler properties
     * @since 0.8.1
     */
    void setOptions(Map<?, ?> options) {
        setCompilerProperties(new LinkedHashMap<>(options))
    }

    /**
     * Adds compiler properties.
     * @param additions the additional compiler properties
     * @since 0.8.1
     */
    void options(Map<?, ?> additions) {
        compilerProperties(additions)
    }

    /**
     * Adds a compiler property.
     * @param additionalOptions the additional compiler properties
     * @since 0.8.1
     */
    void option(Object key, Object value) {
        compilerProperty(key, value)
    }
}
