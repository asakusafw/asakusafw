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
package com.asakusafw.windgate.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The profile context.
 * @since 0.2.4
 * @version 0.9.1
 */
public class ProfileContext {

    private final ClassLoader classLoader;

    private final ParameterList contextParameters;

    private final Map<Class<?>, Object> resources;

    /**
     * Creates a new instance.
     * @param classLoader the class loader to load services
     * @param contextParameters the context parameters
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ProfileContext(ClassLoader classLoader, ParameterList contextParameters) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        if (contextParameters == null) {
            throw new IllegalArgumentException("contextParameters must not be null"); //$NON-NLS-1$
        }
        this.classLoader = classLoader;
        this.contextParameters = contextParameters;
        this.resources = Collections.emptyMap();
    }

    private ProfileContext(ClassLoader classLoader, ParameterList contextParameters, Map<Class<?>, Object> resources) {
        this.classLoader = classLoader;
        this.contextParameters = contextParameters;
        this.resources = resources;
    }

    /**
     * Creates a new context with system variables as context parameters.
     * @param classLoader current class loader
     * @return the created context object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ProfileContext system(ClassLoader classLoader) {
        return new ProfileContext(classLoader, new ParameterList(System.getenv()));
    }

    /**
     * Creates a new context with the additional resource.
     * @param <T> the resource type
     * @param type the resource type
     * @param object the resource
     * @return the created context
     * @since 0.9.1
     */
    public <T> ProfileContext withResource(Class<T> type, T object) {
        Map<Class<?>, Object> copy = new LinkedHashMap<>(resources);
        copy.put(type, object);
        return new ProfileContext(classLoader, contextParameters, copy);
    }

    /**
     * Returns a class loader to load services.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns context parameters (may be environment variables).
     * @return the context parameters
     */
    public ParameterList getContextParameters() {
        return contextParameters;
    }

    /**
     * Returns an optional resource.
     * @param <T> the resource type
     * @param type the resource type
     * @return the related resource
     * @since 0.9.1
     */
    public <T> Optional<T> findResource(Class<T> type) {
        return Optional.ofNullable(type.cast(resources.get(type)));
    }
}
