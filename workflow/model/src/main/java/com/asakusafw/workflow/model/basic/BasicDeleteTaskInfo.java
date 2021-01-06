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
package com.asakusafw.workflow.model.basic;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;

import com.asakusafw.workflow.model.DeleteTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A basic implementation of {@link DeleteTaskInfo}.
 * @since 0.10.0
 */
public class BasicDeleteTaskInfo extends BasicTaskInfo implements DeleteTaskInfo {

    @JsonProperty("module")
    private final String moduleName;

    @JsonProperty("kind")
    private final PathKind pathKind;

    @JsonProperty("path")
    private final String path;

    /**
     * Creates a new instance.
     * @param pathKind the path kind
     * @param path the path expression
     */
    public BasicDeleteTaskInfo(PathKind pathKind, String path) {
        this(DEFAULT_MODULE_NAME, pathKind, path);
    }

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param pathKind the path kind
     * @param path the path expression
     */
    public BasicDeleteTaskInfo(String moduleName, PathKind pathKind, String path) {
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(pathKind);
        Objects.requireNonNull(path);
        this.moduleName = moduleName;
        this.pathKind = pathKind;
        this.path = path;
    }

    @JsonCreator
    static BasicDeleteTaskInfo restore(
            @JsonProperty("module") String moduleName,
            @JsonProperty("kind") PathKind pathKind,
            @JsonProperty("path") String path,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes,
            @JsonProperty("blockers") Collection<? extends TaskInfo> blockers) {
        BasicDeleteTaskInfo result = new BasicDeleteTaskInfo(moduleName, pathKind, path);
        result.setAttributes(attributes);
        result.setBlockers(blockers);
        return result;
    }


    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public PathKind getPathKind() {
        return pathKind;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Delete(module={0}, kind={1}, path={2})",
                getModuleName(),
                getPathKind(),
                getPath());
    }
}
