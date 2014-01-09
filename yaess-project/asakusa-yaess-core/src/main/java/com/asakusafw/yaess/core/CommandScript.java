/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A script that describes process command using operating system shell.
 * @since 0.2.3
 */
public class CommandScript implements ExecutionScript {

    static final Logger LOG = LoggerFactory.getLogger(CommandScript.class);

    /**
     * Default profile name (empty string).
     */
    public static final String DEFAULT_PROFILE_NAME = "";

    private final String id;

    private final Set<String> blockerIds;

    private final String profileName;

    private final List<String> command;

    private final String moduleName;

    private final Map<String, String> environmentVariables;

    private final boolean resolved;

    /**
     * Creates a new instance.
     * Note that this creates an <em>UNRESOLVED</em> instance.
     * To create a resolved instance, please use {@link #resolve(ExecutionContext, ExecutionScriptHandler)}.
     * @param id the script ID
     * @param blockerIds other script IDs blocking this script execution
     * @param profileName the profile name to execute this command, empty string means use default profile
     * @param moduleName module name of target command
     * @param command sequence of command tokens
     * @param environmentVariables the extra environment variables
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #DEFAULT_PROFILE_NAME
     * @see #resolve(ExecutionContext, ExecutionScriptHandler)
     */
    public CommandScript(
            String id,
            Set<String> blockerIds,
            String profileName,
            String moduleName,
            List<String> command,
            Map<String, String> environmentVariables) {
        this(id, blockerIds, profileName, moduleName, command, environmentVariables, false);
    }

    private CommandScript(
            String id,
            Set<String> blockerIds,
            String profileName,
            String moduleName,
            List<String> command,
            Map<String, String> environmentVariables,
            boolean resolved) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (blockerIds == null) {
            throw new IllegalArgumentException("blockerIds must not be null"); //$NON-NLS-1$
        }
        if (profileName == null) {
            throw new IllegalArgumentException("profileName must not be null"); //$NON-NLS-1$
        }
        if (moduleName == null) {
            throw new IllegalArgumentException("moduleName must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        if (command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty"); //$NON-NLS-1$
        }
        if (environmentVariables == null) {
            throw new IllegalArgumentException("environmentVariables must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.blockerIds = Collections.unmodifiableSet(new TreeSet<String>(blockerIds));
        this.profileName = profileName;
        this.command = Collections.unmodifiableList(new ArrayList<String>(command));
        this.moduleName = moduleName;
        this.environmentVariables = Collections.unmodifiableMap(
                new LinkedHashMap<String, String>(environmentVariables));
        this.resolved = resolved;
    }

    @Override
    public Kind getKind() {
        return Kind.COMMAND;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> getBlockerIds() {
        return blockerIds;
    }

    /**
     * Returns the profile name to execute this command.
     * @return the profile name, or an empty string to profile is unspecified
     * @see #DEFAULT_PROFILE_NAME
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns a module name of target command.
     * @return the module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns a sequence of command line tokens.
     * If this script is not resolved, returning tokens may include some placeholders.
     * @return the command line tokens
     */
    public List<String> getCommandLineTokens() {
        return command;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    @Override
    public CommandScript resolve(
            ExecutionContext context,
            ExecutionScriptHandler<?> handler) throws InterruptedException, IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null"); //$NON-NLS-1$
        }
        if (isResolved()) {
            return this;
        }
        LOG.debug("Resolving {}", this);

        PlaceholderResolver resolver = new PlaceholderResolver(this, context, handler);
        List<String> resolvedCommands = new ArrayList<String>();
        for (String token : getCommandLineTokens()) {
            resolvedCommands.add(resolver.resolve(token));
        }
        LOG.debug("Resolved command line tokens: {}", resolvedCommands);

        Map<String, String> resolvedEnvironments = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : getEnvironmentVariables().entrySet()) {
            resolvedEnvironments.put(entry.getKey(), resolver.resolve(entry.getValue()));
        }
        LOG.debug("Resolved environment variables: {}", resolvedEnvironments);

        return new CommandScript(
                getId(),
                getBlockerIds(),
                getProfileName(),
                getModuleName(),
                resolvedCommands,
                resolvedEnvironments,
                true);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Command'{'id={0}, blockers={1}, profile={2}, module={3}, command={4}, environment={5}'}'",
                getId(),
                getBlockerIds(),
                getProfileName(),
                getModuleName(),
                getCommandLineTokens(),
                getEnvironmentVariables());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + blockerIds.hashCode();
        result = prime * result + profileName.hashCode();
        result = prime * result + moduleName.hashCode();
        result = prime * result + command.hashCode();
        result = prime * result + environmentVariables.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CommandScript other = (CommandScript) obj;
        if (!id.equals(other.id)) {
            return false;
        }
        if (!blockerIds.equals(other.blockerIds)) {
            return false;
        }
        if (!profileName.equals(other.profileName)) {
            return false;
        }
        if (!moduleName.equals(other.moduleName)) {
            return false;
        }
        if (!command.equals(other.command)) {
            return false;
        }
        if (!environmentVariables.equals(other.environmentVariables)) {
            return false;
        }
        return true;
    }
}
