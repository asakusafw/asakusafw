/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver.compiler.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.asakusafw.testdriver.compiler.CommandTaskMirror;
import com.asakusafw.testdriver.compiler.CommandToken;

/**
 * A basic implementation of {@link CommandTaskMirror}.
 * @since 0.8.0
 * @version 0.8.2
 */
public class BasicCommandTaskMirror extends BasicTaskMirror implements CommandTaskMirror {

    /**
     * A {@link ConfigurationResolver} which resolves nothing.
     */
    public static final ConfigurationResolver NULL_RESOLVER = new ConfigurationResolver() {
        @Override
        public List<CommandToken> apply(Map<String, String> configurations) {
            return Collections.emptyList();
        }
    };

    private final String moduleName;

    private final String profileName;

    private final String command;

    private final List<CommandToken> arguments;

    private final ConfigurationResolver configurationResolver;

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param profileName the profile name
     * @param command the command path
     * @param arguments the command arguments
     */
    public BasicCommandTaskMirror(
            String moduleName, String profileName,
            String command, List<CommandToken> arguments) {
        this(moduleName, profileName, command, arguments, NULL_RESOLVER);
    }

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param profileName the profile name
     * @param command the command path
     * @param arguments the command arguments
     * @param configurationResolver resolves extra configurations
     * @since 0.8.2
     */
    public BasicCommandTaskMirror(
            String moduleName, String profileName,
            String command, List<CommandToken> arguments,
            ConfigurationResolver configurationResolver) {
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(profileName);
        Objects.requireNonNull(command);
        Objects.requireNonNull(arguments);
        Objects.requireNonNull(configurationResolver);
        this.moduleName = moduleName;
        this.profileName = profileName;
        this.command = command;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        this.configurationResolver = configurationResolver;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getProfileName() {
        return profileName;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public List<CommandToken> getArguments() {
        return getArguments(Collections.<String, String>emptyMap());
    }

    @Override
    public List<CommandToken> getArguments(Map<String, String> extraConfigurations) {
        List<CommandToken> results = new ArrayList<>();
        results.addAll(arguments);
        results.addAll(configurationResolver.apply(extraConfigurations));
        return results;
    }

    /**
     * Resolves extra configurations.
     * @since 0.8.2
     * @see CommandTaskMirror#getArguments(Map)
     */
    public interface ConfigurationResolver {

        /**
         * Resolves configurations into additional command arguments.
         * @param configurations the extra configurations (treated as Hadoop configurations)
         * @return the additional command arguments
         */
        List<CommandToken> apply(Map<String, String> configurations);
    }
}
