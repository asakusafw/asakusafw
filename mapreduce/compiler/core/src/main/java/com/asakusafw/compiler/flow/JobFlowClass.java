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
package com.asakusafw.compiler.flow;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Represents a model of jobflow class.
 */
public class JobFlowClass {

    private final JobFlow config;

    private final FlowGraph graph;

    /**
     * Creates a new instance.
     * @param config the configuration of this jobflow
     * @param graph a flow graph which represents the target jobflow
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public JobFlowClass(JobFlow config, FlowGraph graph) {
        Precondition.checkMustNotBeNull(config, "config"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        this.config = config;
        this.graph = graph;
    }

    /**
     * Returns the configuration of this jobflow.
     * @return the configuration of this jobflow
     */
    public JobFlow getConfig() {
        return config;
    }

    /**
     * Returns the flow graph of this jobflow.
     * @return the flow graph of this jobflow
     */
    public FlowGraph getGraph() {
        return this.graph;
    }
}
