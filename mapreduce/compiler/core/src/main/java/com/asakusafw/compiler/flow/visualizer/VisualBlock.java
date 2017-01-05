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
package com.asakusafw.compiler.flow.visualizer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.utils.collections.Sets;

/**
 * A visual model for flow blocks.
 */
public class VisualBlock implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private final String label;

    private final Set<FlowBlock.Input> inputs;

    private final Set<FlowBlock.Output> outputs;

    private final Set<VisualNode> nodes;

    /**
     * Creates a new instance.
     * @param label the label of this element (nullable)
     * @param inputs the input ports
     * @param outputs the output ports
     * @param nodes the element nodes
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public VisualBlock(
            String label,
            Set<FlowBlock.Input> inputs,
            Set<FlowBlock.Output> outputs,
            Set<? extends VisualNode> nodes) {
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(nodes, "nodes"); //$NON-NLS-1$
        this.label = label;
        this.inputs = Sets.from(inputs);
        this.outputs = Sets.from(outputs);
        this.nodes = Sets.from(nodes);
    }

    /**
     * Returns the label of this block.
     * @return the block label, or {@code null} if it was not specified
     */
    public String getLabel() {
        return label;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Returns the input ports of this block.
     * @return the input ports of this block
     */
    public Set<FlowBlock.Input> getInputs() {
        return inputs;
    }

    /**
     * Returns the output ports of this block.
     * @return the output ports
     */
    public Set<FlowBlock.Output> getOutputs() {
        return outputs;
    }

    /**
     * Returns the element nodes of this block.
     * @return the element nodes
     */
    public Set<VisualNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public Kind getKind() {
        return Kind.BLOCK;
    }

    @Override
    public <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E {
        Precondition.checkMustNotBeNull(visitor, "visitor"); //$NON-NLS-1$
        R result = visitor.visitBlock(context, this);
        return result;
    }
}
