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

import java.util.Collection;
import java.util.Objects;

import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A basic implementation of {@link HadoopTaskInfo}.
 * @since 0.10.0
 */
public class BasicHadoopTaskInfo extends BasicTaskInfo implements HadoopTaskInfo {

    @JsonProperty("module")
    private final String moduleName;

    @JsonProperty("application")
    private final String className;

    /**
     * Creates a new instance.
     * @param className the class name
     */
    public BasicHadoopTaskInfo(String className) {
        this(DEFAULT_MODULE_NAME, className);
    }

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param className the class name
     */
    public BasicHadoopTaskInfo(String moduleName, String className) {
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(className);
        this.moduleName = moduleName;
        this.className = className;
    }

    @JsonCreator
    static BasicHadoopTaskInfo restore(
            @JsonProperty("module") String moduleName,
            @JsonProperty("application") String className,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes,
            @JsonProperty("blockers") Collection<? extends TaskInfo> blockers) {
        BasicHadoopTaskInfo result = new BasicHadoopTaskInfo(moduleName, className);
        result.setAttributes(attributes);
        result.setBlockers(blockers);
        return result;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
