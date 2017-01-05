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
package com.asakusafw.compiler.flow.jobflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.Input;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.IoContext;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.Output;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Delivery;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Process;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Processible;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Reduce;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.SideData;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Source;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * Compiles jobflow described in the flow DSL.
 * @since 0.1.0
 * @version 0.2.6
 */
public class JobflowCompiler {

    static final Logger LOG = LoggerFactory.getLogger(JobflowCompiler.class);

    @SuppressWarnings("unused")
    private final FlowCompilingEnvironment environment;

    private final JobflowAnalyzer analyzer;

    private final StageClientEmitter stageClientEmitter;

    private final CleanupStageClientEmitter cleanupStageClientEmitter;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JobflowCompiler(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
        this.analyzer = new JobflowAnalyzer(environment);
        this.stageClientEmitter = new StageClientEmitter(environment);
        this.cleanupStageClientEmitter = new CleanupStageClientEmitter(environment);
    }

    /**
     * Compiles the target stage graph.
     * @param graph the target stage graph
     * @param stageModels the stage models
     * @return the corresponded jobflow model object
     * @throws IOException if failed to compile
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public JobflowModel compile(
            StageGraph graph,
            Collection<StageModel> stageModels) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageModels, "stageModels"); //$NON-NLS-1$
        LOG.debug("analyzing jobflow: {}", graph.getInput().getSource().getDescription().getName()); //$NON-NLS-1$
        JobflowModel jobflow = analyze(graph, stageModels);
        compileClients(jobflow);
        CompiledJobflow compiled = emit(jobflow);
        jobflow.setCompiled(compiled);
        reportSummary(jobflow);
        return jobflow;
    }

    private JobflowModel analyze(
            StageGraph graph,
            Collection<StageModel> stageModels) throws IOException {
        assert graph != null;
        assert stageModels != null;
        JobflowModel jobflow = analyzer.analyze(graph, stageModels);
        if (analyzer.hasError()) {
            analyzer.clearError();
            throw new IOException(Messages.getString("JobflowCompiler.errorFailedToAnalyze")); //$NON-NLS-1$
        }
        return jobflow;
    }

    private CompiledJobflow emit(JobflowModel model) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        LOG.debug("generating external I/O tasks: {}.{}", model.getBatchId(), model.getFlowId()); //$NON-NLS-1$
        Map<ExternalIoDescriptionProcessor, List<Import>> imports = group(model.getImports());
        Map<ExternalIoDescriptionProcessor, List<Export>> exports = group(model.getExports());
        fillEmptyList(imports, exports.keySet());
        fillEmptyList(exports, imports.keySet());

        List<ExternalIoCommandProvider> commands = new ArrayList<>();
        List<ExternalIoStage> prologues = new ArrayList<>();
        List<ExternalIoStage> epilogues = new ArrayList<>();
        for (Map.Entry<ExternalIoDescriptionProcessor, List<Import>> entry : imports.entrySet()) {
            ExternalIoDescriptionProcessor proc = entry.getKey();
            List<Import> importGroup = entry.getValue();
            List<Export> exportGroup = exports.get(proc);
            assert exportGroup != null;
            assert importGroup.isEmpty() == false || exportGroup.isEmpty() == false;

            IoContext context = createEmitContext(proc, importGroup, exportGroup);

            LOG.debug("generating external I/O descriptions: {}", proc.getClass().getName()); //$NON-NLS-1$
            proc.emitPackage(context);

            LOG.debug("generating prologue stages: {}", proc.getClass().getName()); //$NON-NLS-1$
            prologues.addAll(proc.emitPrologue(context));

            LOG.debug("generating epilogue stages: {}", proc.getClass().getName()); //$NON-NLS-1$
            epilogues.addAll(proc.emitEpilogue(context));

            commands.add(proc.createCommandProvider(context));
        }

        return new CompiledJobflow(commands, prologues, epilogues);
    }

    private IoContext createEmitContext(
            ExternalIoDescriptionProcessor processor,
            List<Import> importGroup,
            List<Export> exportGroup) {
        assert processor != null;
        assert importGroup != null;
        assert exportGroup != null;
        List<Input> inputs = new ArrayList<>();
        for (Import model : importGroup) {
            inputs.add(new Input(model.getDescription(), model.getOutputFormatType()));
        }
        List<Output> outputs = new ArrayList<>();
        for (Export model : exportGroup) {
            List<SourceInfo> sources = new ArrayList<>();
            for (Source source : model.getResolvedSources()) {
                sources.add(source.getInputInfo());
            }
            outputs.add(new Output(model.getDescription(), sources));
        }
        IoContext context = new IoContext(inputs, outputs);
        return context;
    }

    private void reportSummary(JobflowModel jobflow) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Compilation Report: {} - {}", jobflow.getBatchId(), jobflow.getFlowId()); //$NON-NLS-1$
            LOG.debug("Imports: {}", jobflow.getImports().size()); //$NON-NLS-1$
            LOG.debug("Exports: {}", jobflow.getExports().size()); //$NON-NLS-1$
            LOG.debug("Stages : {}", jobflow.getStages().size()); //$NON-NLS-1$
            LOG.debug("Details:"); //$NON-NLS-1$
            for (Import stage : jobflow.getImports()) {
                LOG.debug("===="); //$NON-NLS-1$
                LOG.debug("Import: {}", stage.getId()); //$NON-NLS-1$
                LOG.debug("Description: {}", //$NON-NLS-1$
                        stage.getDescription().getImporterDescription().getClass().getName());
                LOG.debug("Target: {}", stage.getInputInfo().getLocations()); //$NON-NLS-1$
                LOG.debug("Format: {}", stage.getInputInfo().getFormat().getName()); //$NON-NLS-1$
            }
            for (CompiledStage stage : jobflow.getCompiled().getPrologueStages()) {
                LOG.debug("===="); //$NON-NLS-1$
                LOG.debug("Prologue: {}", stage.getStageId()); //$NON-NLS-1$
                LOG.debug("Client: {}", stage.getQualifiedName().toNameString()); //$NON-NLS-1$
            }
            Graph<Stage> graph = jobflow.getDependencyGraph();
            Graph<Stage> tgraph = Graphs.transpose(graph);
            for (Stage stage : jobflow.getStages()) {
                LOG.debug("===="); //$NON-NLS-1$
                LOG.debug("Stage: {}", stage.getCompiled().getStageId()); //$NON-NLS-1$
                LOG.debug("Client: {}", stage.getCompiled().getQualifiedName().toNameString()); //$NON-NLS-1$
                for (Process unit : stage.getProcesses()) {
                    LOG.debug("Input: {} ({})", unit.getResolvedLocations(), unit.getDataType()); //$NON-NLS-1$
                }
                for (Delivery unit : stage.getDeliveries()) {
                    LOG.debug("Output: {} ({})", unit.getInputInfo().getLocations(), unit.getDataType()); //$NON-NLS-1$
                }
                Reduce reducer = stage.getReduceOrNull();
                if (reducer != null) {
                    LOG.debug("ShuffleKey: {}", reducer.getKeyTypeName().toNameString()); //$NON-NLS-1$
                    LOG.debug("ShuffleValue: {}", reducer.getValueTypeName().toNameString()); //$NON-NLS-1$
                    LOG.debug("Partitioner: {}", reducer.getPartitionerTypeName().toNameString()); //$NON-NLS-1$
                    LOG.debug("Grouping: {}", reducer.getGroupingComparatorTypeName().toNameString()); //$NON-NLS-1$
                    LOG.debug("Sort: {}", reducer.getSortComparatorTypeName().toNameString()); //$NON-NLS-1$
                    LOG.debug("Combiner: {}", reducer.getCombinerTypeNameOrNull() == null //$NON-NLS-1$
                            ? "N/A" : reducer.getCombinerTypeNameOrNull().toNameString()); //$NON-NLS-1$
                    LOG.debug("Reducer: {}", reducer.getReducerTypeName().toNameString()); //$NON-NLS-1$
                }
                for (SideData data : stage.getSideData()) {
                    LOG.debug("SideData: {} ({})", data.getLocalName(), data.getClusterPaths()); //$NON-NLS-1$
                }
                LOG.debug("Upstreams: {}", getStageIds(graph.getConnected(stage))); //$NON-NLS-1$
                LOG.debug("Downstreams: {}", getStageIds(tgraph.getConnected(stage))); //$NON-NLS-1$
            }
            for (CompiledStage stage : jobflow.getCompiled().getEpilogueStages()) {
                LOG.debug("===="); //$NON-NLS-1$
                LOG.debug("Epilogue: {}", stage.getStageId()); //$NON-NLS-1$
                LOG.debug("Client: {}", stage.getQualifiedName().toNameString()); //$NON-NLS-1$
            }
            for (Export stage : jobflow.getExports()) {
                LOG.debug("===="); //$NON-NLS-1$
                LOG.debug("Export: {}", stage.getId()); //$NON-NLS-1$
                LOG.debug("Description: {}", //$NON-NLS-1$
                        stage.getDescription().getExporterDescription().getClass().getName());
                LOG.debug("Source: {}", stage.getResolvedLocations()); //$NON-NLS-1$
            }
            LOG.debug("===="); //$NON-NLS-1$
        }
    }

    private List<String> getStageIds(Collection<Stage> stages) {
        assert stages != null;
        List<String> results = new ArrayList<>();
        for (Stage stage : stages) {
            results.add(stage.getCompiled().getStageId());
        }
        Collections.sort(results);
        return results;
    }

    private <K, V> void fillEmptyList(Map<K, List<V>> map, Set<K> samples) {
        assert map != null;
        assert samples != null;
        for (K sample : samples) {
            if (map.containsKey(sample) == false) {
                map.put(sample, Collections.emptyList());
            }
        }
    }

    private <T extends Processible> Map<ExternalIoDescriptionProcessor, List<T>> group(List<T> targets) {
        assert targets != null;
        Map<ExternalIoDescriptionProcessor, List<T>> results = new HashMap<>();
        for (T processible : targets) {
            ExternalIoDescriptionProcessor proc = processible.getProcessor();
            Maps.addToList(results, proc, processible);
        }
        return results;
    }

    private void compileClients(JobflowModel jobflow) throws IOException {
        assert jobflow != null;
        for (Stage stage : jobflow.getStages()) {
            CompiledStage client = stageClientEmitter.emit(stage);
            stage.setCompiled(client);
        }
        cleanupStageClientEmitter.emit();
    }
}
