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
package com.asakusafw.testdriver.compiler.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.compiler.PortMirror;
import com.asakusafw.testdriver.compiler.TaskMirror;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * A basic implementation of {@link JobflowMirror}.
 * @since 0.8.0
 */
public class BasicJobflowMirror extends AbstractGraphElement<JobflowMirror> implements JobflowMirror {

    private final String flowId;

    private final Map<String, PortMirror<? extends ImporterDescription>> inputs = new LinkedHashMap<>();

    private final Map<String, PortMirror<? extends ExporterDescription>> outputs = new LinkedHashMap<>();

    private final Map<TaskMirror.Phase, Set<TaskMirror>> tasks = new EnumMap<>(TaskMirror.Phase.class);
    {
        for (TaskMirror.Phase phase : TaskMirror.Phase.values()) {
            tasks.put(phase, new LinkedHashSet<TaskMirror>());
        }
    }

    /**
     * Creates a new instance.
     * @param flowId the flow ID
     */
    public BasicJobflowMirror(String flowId) {
        Objects.requireNonNull(flowId);
        this.flowId = flowId;
    }

    @Override
    public String getFlowId() {
        return flowId;
    }

    @Override
    public Collection<? extends PortMirror<? extends ImporterDescription>> getInputs() {
        return Collections.unmodifiableCollection(inputs.values());
    }

    @Override
    public Collection<? extends PortMirror<? extends ExporterDescription>> getOutputs() {
        return Collections.unmodifiableCollection(outputs.values());
    }

    @Override
    public Set<? extends TaskMirror> getTasks(TaskMirror.Phase phase) {
        Objects.requireNonNull(phase);
        return tasks.get(phase);
    }

    @Override
    public PortMirror<? extends ImporterDescription> findInput(String name) {
        Objects.requireNonNull(name);
        return inputs.get(name);
    }

    @Override
    public PortMirror<? extends ExporterDescription> findOutput(String name) {
        Objects.requireNonNull(name);
        return outputs.get(name);
    }

    /**
     * Adds external input ports.
     * @param ports the external port mirrors
     */
    public void addInputs(Collection<? extends PortMirror<? extends ImporterDescription>> ports) {
        Objects.requireNonNull(ports);
        for (PortMirror<? extends ImporterDescription> port : ports) {
            addInput(port);
        }
    }

    /**
     * Adds external output ports.
     * @param ports the external port mirrors
     */
    public void addOutputs(Collection<? extends PortMirror<? extends ExporterDescription>> ports) {
        Objects.requireNonNull(ports);
        for (PortMirror<? extends ExporterDescription> port : ports) {
            addOutput(port);
        }
    }

    /**
     * Adds an external input port.
     * @param port the external port mirror
     */
    public void addInput(PortMirror<? extends ImporterDescription> port) {
        Objects.requireNonNull(port);
        inputs.put(port.getName(), port);
    }

    /**
     * Adds an external output port.
     * @param port the external port mirror
     */
    public void addOutput(PortMirror<? extends ExporterDescription> port) {
        Objects.requireNonNull(port);
        outputs.put(port.getName(), port);
    }

    /**
     * Adds a task.
     * @param phase the target phase
     * @param task the task
     */
    public void addTask(TaskMirror.Phase phase, TaskMirror task) {
        Objects.requireNonNull(phase);
        Objects.requireNonNull(task);
        tasks.get(phase).add(task);
    }
}
