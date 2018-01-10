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
package com.asakusafw.yaess.core;

/**
 * The profile context for YAESS.
 * @since 0.2.4
 */
public class ProfileContext {

    private final ClassLoader classLoader;

    private final VariableResolver contextParameters;

    /**
     * Creates a new instance.
     * @param classLoader the class loader to load services
     * @param contextParameters the context parameters
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ProfileContext(ClassLoader classLoader, VariableResolver contextParameters) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        if (contextParameters == null) {
            throw new IllegalArgumentException("contextParameters must not be null"); //$NON-NLS-1$
        }
        this.classLoader = classLoader;
        this.contextParameters = contextParameters;
    }

    /**
     * Creates a new context with system variables as context parameters.
     * @param classLoader current class loader
     * @return the created context object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ProfileContext system(ClassLoader classLoader) {
        return new ProfileContext(classLoader, new VariableResolver(System.getenv()));
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
    public VariableResolver getContextParameters() {
        return contextParameters;
    }
}
