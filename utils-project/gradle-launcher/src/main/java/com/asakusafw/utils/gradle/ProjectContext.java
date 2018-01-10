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
package com.asakusafw.utils.gradle;

import java.util.Map;

/**
 * A context of project.
 * @since 0.9.2
 */
public interface ProjectContext {

    /**
     * Returns the environment variables.
     * @return the environment variables
     */
    Map<String, String> environment();

    /**
     * Returns the environment variable.
     * @param name the variable name
     * @return the variable value, or {@code null} if it is not defined
     */
    default String environment(String name) {
        return environment().get(name);
    }

    /**
     * Returns the system properties.
     * @return the system properties
     */
    Map<String, String> properties();

    /**
     * Returns the system property.
     * @param key the property key
     * @return the property value, or {@code null} if it is not defined
     */
    default String property(String key) {
        return properties().get(key);
    }

    /**
     * Returns a command launcher of this context.
     * @return a command launcher
     */
    CommandLauncher getCommandLauncher();
}
