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
package com.asakusafw.vocabulary.batch;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * A description of Unit-of-Work using custom scripts.
 * @deprecated does not supported
 */
@Deprecated
public class ScriptWorkDescription extends WorkDescription {

    /**
     * The key name of the command name.
     */
    public static final String K_NAME = "name"; //$NON-NLS-1$

    /**
     * The key name of command line.
     */
    public static final String K_COMMAND = "command"; //$NON-NLS-1$

    /**
     * The key name of profile name.
     */
    public static final String K_PROFILE = "profile"; //$NON-NLS-1$

    /**
     * The key prefix of environment variables.
     */
    public static final String K_ENVIRONMENT_PREFIX = "env."; //$NON-NLS-1$

    private final String name;

    private final String command;

    private final String profileName;

    private final Map<String, String> variables;

    /**
     * Creates a new instance.
     * @param name the identifier of this work
     * @param command the command line
     * @param profileName the profile name
     * @param variables the environment variables
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public ScriptWorkDescription(String name, String command, String profileName, Map<String, String> variables) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        if (profileName == null) {
            throw new IllegalArgumentException("profileName must not be null"); //$NON-NLS-1$
        }
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("ScriptWorkDescription.errorInvalidId"), //$NON-NLS-1$
                    name,
                    command));
        }
        this.name = name;
        this.command = command;
        this.profileName = profileName;
        this.variables = Collections.unmodifiableSortedMap(new TreeMap<>(variables));
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the command line.
     * @return the command line
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the profile name.
     * @return the profile name
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the environment variables.
     * @return the environment variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + command.hashCode();
        result = prime * result + profileName.hashCode();
        result = prime * result + variables.hashCode();
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
        ScriptWorkDescription other = (ScriptWorkDescription) obj;
        if (name.equals(other.name) == false) {
            return false;
        }
        if (command.equals(other.command) == false) {
            return false;
        }
        if (profileName.equals(other.profileName) == false) {
            return false;
        }
        if (variables.equals(other.variables) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Script({0})@{1}", getCommand(), getProfileName()); //$NON-NLS-1$
    }
}
