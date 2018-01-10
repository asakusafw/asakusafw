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

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Configures environment variables.
 * @since 0.9.2
 */
public class EnvironmentConfigurator implements Consumer<BaseProject<?>> {

    private final Map<String, String> edit;

    /**
     * Creates a new instance.
     * @param edit edit
     */
    public EnvironmentConfigurator(Map<String, String> edit) {
        this.edit = new LinkedHashMap<>(edit);
    }

    /**
     * Returns a NO-OP configurator.
     * @return the created configurator
     */
    public static EnvironmentConfigurator nothing() {
        return of(Collections.emptyMap());
    }

    /**
     * Returns a system {@link EnvironmentConfigurator}.
     * @return a system {@link EnvironmentConfigurator}
     */
    public static EnvironmentConfigurator system() {
        return of(System.getenv());
    }

    /**
     * Returns a configurator which edits the given environment variable.
     * @param name the variable name
     * @param value the variable value
     * @return the created configurator
     */
    public static EnvironmentConfigurator of(String name, String value) {
        return of(Collections.singletonMap(name, value));
    }

    /**
     * Returns a configurator which edits the given environment variable.
     * @param name the variable name
     * @param path the target path (nullable)
     * @return the configurator
     */
    public static EnvironmentConfigurator of(String name, Path path) {
        return of(name, Optional.ofNullable(path).map(Path::toAbsolutePath).map(Path::toString).orElse(null));
    }

    /**
     * Returns a configurator which edits the given environment variables.
     * @param variables the variables
     * @return the created configurator
     */
    public static EnvironmentConfigurator of(Map<String, String> variables) {
        return new EnvironmentConfigurator(variables);
    }

    @Override
    public void accept(BaseProject<?> project) {
        project.withEnvironment(m -> m.putAll(edit));
    }
}
