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
package com.asakusafw.testdriver.compiler;

import java.util.Collection;
import java.util.Set;

import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Represents a jobflow.
 * @since 0.8.0
 */
public interface JobflowMirror extends GraphElement<JobflowMirror> {

    /**
     * The flow ID of this jobflow.
     * @return the flow ID
     */
    String getFlowId();

    /**
     * Returns the input ports of the jobflow.
     * @return the input ports
     */
    Collection<? extends PortMirror<? extends ImporterDescription>> getInputs();

    /**
     * Returns the output ports of the jobflow.
     * @return the output ports
     */
    Collection<? extends PortMirror<? extends ExporterDescription>> getOutputs();

    /**
     * Returns tasks of this jobflow.
     * @param phase the target phase
     * @return the tasks, or an empty set if it is not found
     */
    Set<? extends TaskMirror> getTasks(TaskMirror.Phase phase);

    /**
     * Returns an input of this jobflow.
     * @param name the input name
     * @return the input port, or {@code null} if it is not found
     */
    PortMirror<? extends ImporterDescription> findInput(String name);

    /**
     * Returns an output of this jobflow.
     * @param name the output name
     * @return the output port, or {@code null} if it is not found
     */
    PortMirror<? extends ExporterDescription> findOutput(String name);
}
