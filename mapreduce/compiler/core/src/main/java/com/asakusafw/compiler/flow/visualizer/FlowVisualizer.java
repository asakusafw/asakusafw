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

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Visualizes flow graphs.
 * @since 0.1.0
 * @version 0.4.0
 */
public class FlowVisualizer {

    private static final String PATH_FLOW_GRAPH = "META-INF/visualize/flowgraph.dot"; //$NON-NLS-1$

    private static final String PATH_STAGE_GRAPH = "META-INF/visualize/stagegraph.dot"; //$NON-NLS-1$

    private static final String PATH_STAGE_BLOCK = "META-INF/visualize/stageblock-{0}.dot"; //$NON-NLS-1$

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowVisualizer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Visualizes the stage graph and write it to the current context.
     * @param graph the target graph
     * @throws IOException if error occurred while visualizing the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void visualize(StageGraph graph) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        VisualGraph model = VisualAnalyzer.convertStageGraph(graph);
        emit(PATH_STAGE_GRAPH, false, model);
    }

    /**
     * Visualizes the stage block and write it to the current context.
     * @param block the target block
     * @throws IOException if error occurred while visualizing the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void visualize(StageBlock block) throws IOException {
        Precondition.checkMustNotBeNull(block, "block"); //$NON-NLS-1$
        VisualGraph model = VisualAnalyzer.convertStageBlock(block);
        emit(MessageFormat.format(PATH_STAGE_BLOCK, String.valueOf(block.getStageNumber())), true, model);
    }

    /**
     * Visualizes the flow graph and write it to the current context.
     * @param graph the target graph
     * @throws IOException if error occurred while visualizing the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void visualize(FlowGraph graph) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        VisualGraph model = VisualAnalyzer.convertFlowGraph(graph);
        emit(PATH_FLOW_GRAPH, false, model);
    }

    private void emit(String path, boolean partial, VisualGraph model) throws IOException {
        assert path != null;
        assert model != null;
        try (OutputStream output = environment.openResource(null, path)) {
            VisualGraphEmitter.emit(model, partial, output);
        }
    }
}
