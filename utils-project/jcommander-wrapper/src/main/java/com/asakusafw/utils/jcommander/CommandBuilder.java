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
package com.asakusafw.utils.jcommander;

import java.util.function.Consumer;

/**
 * An abstract super interface of command builders.
 * @param <T> the command object type
 * @since 0.10.0
 */
public interface CommandBuilder<T> {

    /**
     * Configures this object.
     * @param configurator the configurator
     * @return this
     */
    default CommandBuilder<T> configure(Consumer<? super CommandBuilder<T>> configurator) {
        configurator.accept(this);
        return this;
    }

    /**
     * Adds a leaf command.
     * @param command the command object
     * @return this
     */
    default CommandBuilder<T> addCommand(T command) {
        return addCommand(Util.getCommandName(command), command);
    }

    /**
     * Adds a leaf command with explicit name.
     * @param name the command name
     * @param command the command object
     * @return this
     */
    CommandBuilder<T> addCommand(String name, T command);

    /**
     * Adds a group.
     * @param command the group command
     * @param configurator the group configurator
     * @return this
     */
    default CommandBuilder<T> addGroup(T command, Consumer<? super CommandBuilder<T>> configurator) {
        return addGroup(Util.getCommandName(command), command, configurator);
    }

    /**
     * Adds a group with explicit name.
     * @param name the group name
     * @param command the group command
     * @param configurator the group configurator
     * @return this
     */
    CommandBuilder<T> addGroup(String name, T command, Consumer<? super CommandBuilder<T>> configurator);
}
