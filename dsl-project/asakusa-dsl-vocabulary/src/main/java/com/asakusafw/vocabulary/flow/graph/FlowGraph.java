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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Represents a flow graph.
 * @since 0.1.0
 * @version 0.2.6
 */
public class FlowGraph {

    private final Class<? extends FlowDescription> description;

    private final List<FlowIn<?>> flowInputs;

    private final List<FlowOut<?>> flowOutputs;

    private FlowGraph origin;

    /**
     * Creates a new instance.
     * @param description the description of this flow
     * @param flowInputs input elements in this flow
     * @param flowOutputs output elements in this flow
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public FlowGraph(
            Class<? extends FlowDescription> description,
            List<? extends FlowIn<?>> flowInputs,
            List<? extends FlowOut<?>> flowOutputs) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (flowInputs == null) {
            throw new IllegalArgumentException("flowInputs must not be null"); //$NON-NLS-1$
        }
        if (flowOutputs == null) {
            throw new IllegalArgumentException("flowOutputs must not be null"); //$NON-NLS-1$
        }
        this.description = description;
        this.flowInputs = Collections.unmodifiableList(new ArrayList<>(flowInputs));
        this.flowOutputs = Collections.unmodifiableList(new ArrayList<>(flowOutputs));
        this.origin = this;
    }

    /**
     * Sets the original flow graph object.
     * @param origin the original flow graph
     * @since 0.2.6
     * @see #getOrigin()
     */
    public void setOrigin(FlowGraph origin) {
        if (origin == null) {
            this.origin = this;
        } else {
            this.origin = origin;
        }
    }

    /**
     * Returns the original flow graph object.
     * @return the original flow graph
     * @since 0.2.6
     */
    public FlowGraph getOrigin() {
        return origin;
    }

    /**
     * Returns the description class.
     * @return the description class
     */
    public Class<? extends FlowDescription> getDescription() {
        return description;
    }

    /**
     * Returns input port elements which represent inputs of this flow graph.
     * @return input port elements
     */
    public List<FlowIn<?>> getFlowInputs() {
        return flowInputs;
    }

    /**
     * Returns output port elements which represent outputs of this flow graph.
     * @return output port elements
     */
    public List<FlowOut<?>> getFlowOutputs() {
        return flowOutputs;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "FlowGraph({0})", //$NON-NLS-1$
                getDescription().getName(),
                getFlowInputs(),
                getFlowOutputs());
    }
}
