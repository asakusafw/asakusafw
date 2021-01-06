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
package com.asakusafw.info.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a list of tasks in jobflow.
 * @since 0.9.2
 */
public class TaskListAttribute implements Attribute {

    static final String ID = "task-list";

    private static final String ID_TASKS = "tasks";

    @JsonProperty(ID_TASKS)
    @JsonInclude(Include.NON_ABSENT)
    private final List<TaskInfo> tasks;

    /**
     * Creates a new instance.
     * @param tasks list of tasks
     */
    public TaskListAttribute(Collection<? extends TaskInfo> tasks) {
        this.tasks = Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    @JsonCreator
    static TaskListAttribute restore(
            @JsonProperty("id") String id,
            @JsonProperty(ID_TASKS) Collection<? extends TaskInfo> tasks) {
        if (Objects.equals(id, ID) == false) {
            throw new IllegalArgumentException();
        }
        return new TaskListAttribute(Optional.ofNullable(tasks).orElse(Collections.emptyList()));
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * Returns the tasks.
     * @return the tasks
     */
    public List<TaskInfo> getTasks() {
        return tasks;
    }

    /**
     * Returns the available phases.
     * @return the available phases:
     *      each key is naturally ordered, and present only if there are any tasks in the phase
     */
    @JsonIgnore
    public Map<TaskInfo.Phase, List<TaskInfo>> getPhases() {
        Map<TaskInfo.Phase, List<TaskInfo>> results = new EnumMap<>(TaskInfo.Phase.class);
        tasks.forEach(it -> results.computeIfAbsent(it.getPhase(), k -> new ArrayList<>()).add(it));
        return results;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(tasks);
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
        return Objects.equals(tasks, ((TaskListAttribute) obj).tasks);
    }

    @Override
    public String toString() {
        return tasks.toString();
    }
}
