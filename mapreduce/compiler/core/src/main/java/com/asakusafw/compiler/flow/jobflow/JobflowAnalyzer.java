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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Delivery;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Process;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Reduce;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.SideData;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Source;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Target;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.CompiledReduce;
import com.asakusafw.compiler.flow.stage.CompiledShuffle;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Analyzes the structure of jobflows.
 */
public class JobflowAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(JobflowAnalyzer.class);

    private final FlowCompilingEnvironment environment;

    private boolean sawError;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JobflowAnalyzer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Returns whether this analysis result contains any erroneous information or not.
     * @return {@code true} if this contains any erroneous information, otherwise {@code false}
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * Resets the current errors.
     * @see #hasError()
     */
    public void clearError() {
        sawError = false;
    }

    /**
     * Analyzes the target stage graph and returns the corresponding jobflow model object.
     * @param graph the target jobflow object
     * @param stageModels the stage model objects
     * @return the analyzed jobflow model, or {@code null} if the target jobflow is not valid
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @see #hasError()
     */
    public JobflowModel analyze(StageGraph graph, Collection<StageModel> stageModels) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageModels, "stageModels"); //$NON-NLS-1$
        LOG.debug("analyzing stage structure: {}", //$NON-NLS-1$
                graph.getInput().getSource().getDescription().getName());

        List<Import> imports = analyzeImports(graph, stageModels);
        List<Export> exports = analyzeExports(graph, stageModels);
        List<Stage> stages = analyzeStages(stageModels);
        if (hasError()) {
            return null;
        }

        resolve(imports, exports, stages);
        if (hasError()) {
            return null;
        }

        return new JobflowModel(
                graph,
                environment.getBatchId(),
                environment.getFlowId(),
                imports,
                exports,
                stages);
    }

    private List<Import> analyzeImports(StageGraph graph, Collection<StageModel> stageModels) {
        assert graph != null;
        assert stageModels != null;
        LOG.debug("analyzing jobflow inputs: {}", graph.getInput()); //$NON-NLS-1$

        Set<InputDescription> saw = new HashSet<>();
        List<Import> results = new ArrayList<>();
        for (FlowBlock.Output source : graph.getInput().getBlockOutputs()) {
            FlowElement element = source.getElementPort().getOwner();
            FlowElementDescription desc = element.getDescription();
            if (desc.getKind() != FlowElementKind.INPUT) {
                error(Messages.getString("JobflowAnalyzer.errorInvalidInput"), desc); //$NON-NLS-1$
                continue;
            }
            InputDescription description = (InputDescription) desc;
            saw.add(description);
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(description);
            if (proc == null) {
                error(Messages.getString("JobflowAnalyzer.errorMissingImporterProcessor"), desc); //$NON-NLS-1$
                continue;
            }
            Import input = new Import(source, description, proc);
            LOG.debug("found jobflow input: {}", input); //$NON-NLS-1$
            results.add(input);
        }
        Set<InputDescription> sideData = new HashSet<>();
        for (StageModel stage : stageModels) {
            sideData.addAll(stage.getSideDataInputs());
        }
        sideData.removeAll(saw);
        for (InputDescription description : sideData) {
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(description);
            if (proc == null) {
                error(Messages.getString("JobflowAnalyzer.errorMissingImporterProcessor"), description); //$NON-NLS-1$
                continue;
            }
            Import input = new Import(description, proc);
            LOG.debug("found side-data: {}", input); //$NON-NLS-1$
            results.add(input);
        }
        return results;
    }

    private List<Export> analyzeExports(StageGraph graph, Collection<StageModel> stageModels) {
        assert graph != null;
        assert stageModels != null;
        LOG.debug("analyzing jobflow outputs: {}", graph.getOutput()); //$NON-NLS-1$

        List<Export> results = new ArrayList<>();
        for (FlowBlock.Input target : graph.getOutput().getBlockInputs()) {
            FlowElement element = target.getElementPort().getOwner();
            FlowElementDescription desc = element.getDescription();
            if (desc.getKind() != FlowElementKind.OUTPUT) {
                error(Messages.getString("JobflowAnalyzer.errorInvalidOutput"), desc); //$NON-NLS-1$
                continue;
            }
            OutputDescription description = (OutputDescription) desc;
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(description);
            if (proc == null) {
                error(Messages.getString("JobflowAnalyzer.errorMissingExporterProcessor"), desc); //$NON-NLS-1$
                continue;
            }
            Export epilogue = new Export(
                    Collections.singletonList(target),
                    description,
                    proc);
            results.add(epilogue);
            LOG.debug("found jobflow output: {}", epilogue); //$NON-NLS-1$
        }
        return results;
    }

    private List<Stage> analyzeStages(Collection<StageModel> stageModels) {
        assert stageModels != null;
        List<Stage> results = new ArrayList<>();
        for (StageModel model : sort(stageModels)) {
            results.add(analyzeStage(model));
        }
        return results;
    }

    private Stage analyzeStage(StageModel model) {
        assert model != null;
        LOG.debug("analyzing jobflow stage: {}", model); //$NON-NLS-1$
        List<Process> processes = analyzeProcesses(model);
        List<Delivery> deliveries = analyzeDeliveries(model);
        Set<SideData> sideData = analyzeSideData(model);
        Reduce reduce = analyzeReduce(model);
        Stage stage = new Stage(
                model,
                processes,
                deliveries,
                reduce,
                sideData);
        LOG.debug("found jobflow stage: {}", model); //$NON-NLS-1$
        return stage;
    }

    private Reduce analyzeReduce(StageModel model) {
        if (model.getShuffleModel() == null) {
            assert model.getReduceUnits().isEmpty();
            return null;
        }
        assert model.getReduceUnits().isEmpty() == false;
        CompiledShuffle shuffle = model.getShuffleModel().getCompiled();
        CompiledReduce reducer = model.getReduceUnits().get(0).getCompiled();
        return new Reduce(
                reducer.getReducerType().getQualifiedName(),
                reducer.getCombinerTypeOrNull() == null
                    ? null
                    : reducer.getCombinerTypeOrNull().getQualifiedName(),
                shuffle.getKeyTypeName(),
                shuffle.getValueTypeName(),
                shuffle.getGroupComparatorTypeName(),
                shuffle.getSortComparatorTypeName(),
                shuffle.getPartitionerTypeName());
    }

    private List<Delivery> analyzeDeliveries(StageModel model) {
        assert model != null;
        Location base = environment.getStageLocation(model.getStageBlock().getStageNumber());
        List<Delivery> deliveries = new ArrayList<>();
        for (StageModel.Sink sink : model.getStageResults()) {
            Location location = base.append(sink.getName()).asPrefix();
            deliveries.add(new Delivery(sink.getOutputs(), Collections.singleton(location)));
        }
        return deliveries;
    }

    private List<Process> analyzeProcesses(StageModel model) {
        List<Process> processes = new ArrayList<>();
        for (StageModel.MapUnit unit : model.getMapUnits()) {
            processes.add(new Process(
                    unit.getInputs(),
                    unit.getCompiled().getQualifiedName()));
        }
        return processes;
    }

    private Set<SideData> analyzeSideData(StageModel model) {
        assert model != null;
        Set<SideData> results = new HashSet<>();
        for (InputDescription input : model.getSideDataInputs()) {
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(input);
            if (proc == null) {
                error(Messages.getString("JobflowAnalyzer.errorMissingImporterProcessor"), input); //$NON-NLS-1$
                continue;
            }
            Set<Location> locations = proc.getInputInfo(input).getLocations();
            results.add(new SideData(locations, input.getName()));
        }
        return results;
    }

    private List<StageModel> sort(Collection<StageModel> stageModels) {
        List<StageModel> models = Lists.from(stageModels);
        Collections.sort(models, (o1, o2) -> Integer.compare(
                o1.getStageBlock().getStageNumber(),
                o2.getStageBlock().getStageNumber()));
        return models;
    }

    private void resolve(List<Import> imports, List<Export> exports, List<Stage> stages) {
        assert imports != null;
        assert exports != null;
        assert stages != null;
        Map<FlowBlock.Output, Source> sources = createOutputMap(imports, stages);
        for (Target target : exports) {
            resolveTarget(target, sources);
        }
        for (Stage stage : stages) {
            for (Target target : stage.getProcesses()) {
                resolveTarget(target, sources);
            }
        }
    }

    private void resolveTarget(Target target, Map<FlowBlock.Output, Source> sources) {
        assert target != null;
        assert sources != null;
        Set<Source> opposites = new HashSet<>();
        for (FlowBlock.Input input : target.getInputs()) {
            for (FlowBlock.Connection conn : input.getConnections()) {
                FlowBlock.Output upstream = conn.getUpstream();
                Source source = sources.get(upstream);
                assert source != null;
                opposites.add(source);
            }
        }
        target.resolveSources(opposites);
    }

    private Map<FlowBlock.Output, Source> createOutputMap(
            List<Import> imports,
            List<Stage> stages) {
        assert imports != null;
        assert stages != null;
        Map<FlowBlock.Output, Source> sources = new HashMap<>();
        for (Source source : imports) {
            for (FlowBlock.Output output : source.getOutputs()) {
                sources.put(output, source);
            }
        }
        for (Stage stage : stages) {
            for (Source source : stage.getDeliveries()) {
                for (FlowBlock.Output output : source.getOutputs()) {
                    sources.put(output, source);
                }
            }
        }
        return sources;
    }

    private void error(String format, Object...args) {
        environment.error(format, args);
        sawError = true;
    }
}
