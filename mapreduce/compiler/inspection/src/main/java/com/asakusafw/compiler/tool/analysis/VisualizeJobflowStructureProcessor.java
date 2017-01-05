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
package com.asakusafw.compiler.tool.analysis;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.visualizer.VisualAnalyzer;
import com.asakusafw.compiler.flow.visualizer.VisualGraph;
import com.asakusafw.compiler.flow.visualizer.VisualGraphEmitter;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Visualizes each jobflow structure.
 * @since 0.2.6
 */
public class VisualizeJobflowStructureProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(VisualizeJobflowStructureProcessor.class);

    static final Charset ENCODING = StandardCharsets.UTF_8;

    private static final String PATH_FLOW_GRAPH = Constants.PATH_JOBFLOW + "{0}/flowgraph.dot"; //$NON-NLS-1$

    private static final String PATH_STAGE_GRAPH = Constants.PATH_JOBFLOW + "{0}/stagegraph.dot"; //$NON-NLS-1$

    private static final String PATH_STAGE_BLOCK = Constants.PATH_JOBFLOW + "{0}/stageblock-{1}.dot"; //$NON-NLS-1$

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        for (Workflow.Unit unit : workflow.getGraph().getNodeSet()) {
            processUnit(unit);
        }
    }

    private void processUnit(Workflow.Unit unit) throws IOException {
        assert unit != null;
        WorkDescription desc = unit.getDescription();
        if (desc instanceof JobFlowWorkDescription) {
            processDescription(
                    (JobFlowWorkDescription) desc,
                    (JobflowModel) unit.getProcessed());
        } else {
            throw new AssertionError(desc);
        }
    }

    private void processDescription(JobFlowWorkDescription desc, JobflowModel model) throws IOException {
        assert desc != null;
        assert model != null;
        StageGraph stageGraph = model.getStageGraph();
        processFlowGraph(model.getFlowId(), stageGraph.getInput().getSource().getOrigin());
        processStageGraph(model.getFlowId(), stageGraph);
        for (StageBlock stage : stageGraph.getStages()) {
            processStageBlock(model.getFlowId(), stage);
        }
    }

    private void processFlowGraph(String flowId, FlowGraph graph) throws IOException {
        assert flowId != null;
        assert graph != null;
        VisualGraph model = VisualAnalyzer.convertFlowGraph(graph);
        emit(MessageFormat.format(PATH_FLOW_GRAPH, flowId), false, model);
    }

    private void processStageGraph(String flowId, StageGraph graph) throws IOException {
        assert flowId != null;
        assert graph != null;
        VisualGraph model = VisualAnalyzer.convertStageGraph(graph);
        emit(MessageFormat.format(PATH_STAGE_GRAPH, flowId), false, model);
    }

    private void processStageBlock(String flowId, StageBlock stage) throws IOException {
        assert flowId != null;
        assert stage != null;
        VisualGraph model = VisualAnalyzer.convertStageBlock(stage);
        emit(MessageFormat.format(PATH_STAGE_BLOCK, flowId, String.valueOf(stage.getStageNumber())), true, model);
    }

    private void emit(String path, boolean partial, VisualGraph model) throws IOException {
        assert path != null;
        assert model != null;
        try (OutputStream output = getEnvironment().openResource(path)) {
            VisualGraphEmitter.emit(model, partial, output);
        }
    }
}
