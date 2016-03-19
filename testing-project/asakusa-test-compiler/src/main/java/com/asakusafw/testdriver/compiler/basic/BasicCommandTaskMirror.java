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
import java.util.Objects;

import com.asakusafw.testdriver.compiler.CommandTaskMirror;
import com.asakusafw.testdriver.compiler.CommandToken;

/**
 * A basic implementation of {@link CommandTaskMirror}.
 * @since 0.8.0
 */
public class BasicCommandTaskMirror extends BasicTaskMirror implements CommandTaskMirror {

    private final String moduleName;

    private final String profileName;

    private final String command;

    private final List<CommandToken> arguments;

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
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(profileName);
        Objects.requireNonNull(command);
        Objects.requireNonNull(arguments);
        this.moduleName = moduleName;
        this.profileName = profileName;
        this.command = command;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
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
}
