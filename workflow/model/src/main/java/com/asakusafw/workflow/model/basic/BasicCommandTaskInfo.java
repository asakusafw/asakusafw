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
package com.asakusafw.workflow.model.basic;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.TaskInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a command task.
 * @since 0.10.0
 */
public class BasicCommandTaskInfo extends BasicTaskInfo implements CommandTaskInfo {

    @JsonProperty("module")
    private final String moduleName;

    @JsonProperty("profile")
    private final String profileName;

    @JsonProperty("command")
    private final String command;

    @JsonProperty("arguments")
    private final List<CommandToken> arguments;

    @JsonProperty("resolver")
    @JsonInclude(Include.NON_NULL)
    private final ConfigurationResolver resolver;

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param profileName the profile name
     * @param command the command path
     * @param arguments the command arguments
     */
    public BasicCommandTaskInfo(
            String moduleName, String profileName,
            String command, List<CommandToken> arguments) {
        this(moduleName, profileName, command, arguments, null);
    }

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param profileName the profile name
     * @param command the command path
     * @param arguments the command arguments
     * @param resolver resolves extra configurations (nullable)
     */
    public BasicCommandTaskInfo(
            String moduleName, String profileName,
            String command, List<CommandToken> arguments,
            ConfigurationResolver resolver) {
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(profileName);
        Objects.requireNonNull(command);
        Objects.requireNonNull(arguments);
        this.moduleName = moduleName;
        this.profileName = profileName;
        this.command = command;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        this.resolver = resolver;
    }

    @JsonCreator
    static BasicCommandTaskInfo restore(
            @JsonProperty("module") String moduleName,
            @JsonProperty("profile") String profileName,
            @JsonProperty("command") String command,
            @JsonProperty("arguments") List<CommandToken> arguments,
            @JsonProperty("resolver") ConfigurationResolver configurationResolver,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes,
            @JsonProperty("blockers") Collection<? extends TaskInfo> blockers) {
        BasicCommandTaskInfo result = new BasicCommandTaskInfo(
                moduleName, profileName,
                command, arguments,
                configurationResolver);
        result.setAttributes(attributes);
        result.setBlockers(blockers);
        return result;
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
        return arguments;
    }

    @Override
    public List<CommandToken> getArguments(Map<String, String> extraConfigurations) {
        List<CommandToken> results = new ArrayList<>();
        results.addAll(arguments);
        if (resolver != null) {
            results.addAll(resolver.apply(extraConfigurations));
        }
        return results;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Command(module={0}, profile={1}, command={2})",
                getModuleName(),
                getProfileName(),
                getCommand());
    }
}
