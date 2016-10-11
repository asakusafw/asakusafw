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
package com.asakusafw.testdriver.compiler;

import java.util.List;
import java.util.Map;

/**
 * Represents a command task.
 * @since 0.8.0
 * @version 0.8.2
 */
public interface CommandTaskMirror extends TaskMirror {

    /**
     * Returns the target profile name.
     * @return the target profile name
     */
    String getProfileName();

    /**
     * Returns the command location.
     * @return the command location (relative from {@code ASAKUSA_HOME})
     */
    String getCommand();

    /**
     * Returns the command arguments.
     * @return the command arguments
     * @deprecated Use {@link #getArguments()} instead
     */
    @Deprecated
    List<CommandToken> getArguments();

    /**
     * Returns the command arguments.
     * @param extraConfigurations the extra configurations (treated as Hadoop configurations)
     * @return the command arguments
     */
    List<CommandToken> getArguments(Map<String, String> extraConfigurations);
}
