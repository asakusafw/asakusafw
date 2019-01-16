/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.asakusafw.workflow.model.JobflowInfo;
import com.asakusafw.workflow.model.TaskInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a jobflow.
 * @since 0.10.0
 */
public class BasicJobflowInfo extends AbstractGraphElement<JobflowInfo> implements JobflowInfo {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("tasks")
    private final Map<TaskInfo.Phase, List<TaskInfo>> tasks = new EnumMap<>(TaskInfo.Phase.class);

    /**
     * Creates a new instance.
     * @param id the flow ID
     */
    public BasicJobflowInfo(String id) {
        this.id = id;
    }

    @JsonCreator
    static BasicJobflowInfo restore(
            @JsonProperty("id") String id,
            @JsonProperty("tasks") Map<TaskInfo.Phase, ? extends Collection<? extends TaskInfo>> tasks,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes,
            @JsonProperty("blockers") Collection<? extends JobflowInfo> blockers) {
        BasicJobflowInfo result = new BasicJobflowInfo(id);
        tasks.forEach((k, v) -> {
            if (v != null && v.isEmpty() == false) {
                result.tasks.put(k, new ArrayList<>(v));
            }
        });
        result.setAttributes(attributes);
        result.setBlockers(blockers);
        return result;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Adds a task.
     * @param phase the target phase
     * @param task the task
     */
    public void addTask(TaskInfo.Phase phase, TaskInfo task) {
        Objects.requireNonNull(phase);
        Objects.requireNonNull(task);
        tasks.computeIfAbsent(phase, k -> new ArrayList<>()).add(task);
    }

    @Override
    public Collection<? extends TaskInfo> getTasks(TaskInfo.Phase phase) {
        return tasks.getOrDefault(phase, Collections.emptyList());
    }
}
