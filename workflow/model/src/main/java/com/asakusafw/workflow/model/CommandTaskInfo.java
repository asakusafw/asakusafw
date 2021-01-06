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
package com.asakusafw.workflow.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.asakusafw.workflow.model.basic.BasicCommandTaskInfo;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represents a command task.
 * @since 0.10.0
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class",
        visible = false
)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        scope = BatchInfo.class,
        property = "oid"
)
public interface CommandTaskInfo extends TaskInfo {

    @Override
    String getModuleName();

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
     * Returns the raw command arguments.
     * @return the command arguments
     */
    List<CommandToken> getArguments();

    /**
     * Returns the command arguments.
     * @param extraConfigurations the extra configurations (treated as Hadoop configurations)
     * @return the command arguments
     */
    default List<CommandToken> getArguments(Map<String, String> extraConfigurations) {
        return getArguments();
    }

    /**
     * Resolves extra configurations.
     * @since 0.10.0
     * @see BasicCommandTaskInfo#getArguments(Map)
     */
    @FunctionalInterface
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.MINIMAL_CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "class",
            visible = false
    )
    public interface ConfigurationResolver extends Function<Map<String, String>, List<CommandToken>> {

        /**
         * Resolves configurations into additional command arguments.
         * @param configurations the extra configurations (treated as Hadoop configurations)
         * @return the additional command arguments
         */
        @Override
        List<CommandToken> apply(Map<String, String> configurations);
    }
}
