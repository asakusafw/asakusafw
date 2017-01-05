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
package com.asakusafw.compiler.flow.plan;

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;

/**
 * Represents a graph of {@link StageBlock}s.
 */
public class StageGraph {

    private final FlowBlock input;

    private final FlowBlock output;

    private final List<StageBlock> stages;

    /**
     * Creates a new instance.
     * @param input the flow input block
     * @param output the flow output block
     * @param stages the stage blocks
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public StageGraph(FlowBlock input, FlowBlock output, List<StageBlock> stages) {
        Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stages, "stages"); //$NON-NLS-1$
        this.input = input;
        this.output = output;
        this.stages = Lists.from(stages);
    }

    /**
     * Returns the block which contains flow inputs of the graph.
     * @return the input block
     */
    public FlowBlock getInput() {
        return input;
    }

    /**
     * Returns the block which contains flow outputs of the graph.
     * @return the output block
     */
    public FlowBlock getOutput() {
        return output;
    }

    /**
     * Returns the stage blocks.
     * @return the stage blocks
     */
    public List<StageBlock> getStages() {
        return stages;
    }
}
