/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.Comparator;
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
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * ジョブフローの構造を解析する。
 */
public class JobflowAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(JobflowAnalyzer.class);

    private final FlowCompilingEnvironment environment;

    private boolean sawError;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public JobflowAnalyzer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 現在までにエラーが発生していた場合に{@code true}を返す。
     * @return 現在までにエラーが発生していた場合に{@code true}
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * 現在までに発生したエラーの情報をクリアする。
     * @see #hasError()
     */
    public void clearError() {
        sawError = false;
    }

    /**
     * 指定のステージグラフを解析し、ジョブフロー全体のステージ構造に関する情報を返す。
     * @param graph ステージグラフ
     * @param stageModels 各ステージの情報
     * @return ジョブフロー全体のステージ構造に関する情報、解析に失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public JobflowModel analyze(StageGraph graph, Collection<StageModel> stageModels) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageModels, "stageModels"); //$NON-NLS-1$
        LOG.debug("{}のステージ構造を分析します",
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
        LOG.debug("入力を解析しています({})", graph.getInput());

        Set<InputDescription> saw = new HashSet<InputDescription>();
        List<Import> results = new ArrayList<JobflowModel.Import>();
        for (FlowBlock.Output source : graph.getInput().getBlockOutputs()) {
            FlowElement element = source.getElementPort().getOwner();
            FlowElementDescription desc = element.getDescription();
            if (desc.getKind() != FlowElementKind.INPUT) {
                error("{0}は入力を表現していません", desc);
                continue;
            }
            InputDescription description = (InputDescription) desc;
            saw.add(description);
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(description);
            if (proc == null) {
                error("{0}は不明なインポーターを利用しています", desc);
                continue;
            }
            Import prologue = new Import(source, description, proc);
            LOG.debug("入力{}が追加されます", prologue);
            results.add(prologue);
        }
        Set<InputDescription> sideData = new HashSet<InputDescription>();
        for (StageModel stage : stageModels) {
            sideData.addAll(stage.getSideDataInputs());
        }
        sideData.removeAll(saw);
        for (InputDescription input : sideData) {
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(input);
            if (proc == null) {
                error("{0}は不明なインポーターを利用しています", input);
                continue;
            }
            Import prologue = new Import(input, proc);
            LOG.debug("サイドデータ入力{}が追加されます", prologue);
            results.add(prologue);
        }
        return results;
    }

    private List<Export> analyzeExports(StageGraph graph, Collection<StageModel> stageModels) {
        assert graph != null;
        assert stageModels != null;
        LOG.debug("出力を解析しています({})", graph.getOutput());

        List<Export> results = new ArrayList<JobflowModel.Export>();
        for (FlowBlock.Input target : graph.getOutput().getBlockInputs()) {
            FlowElement element = target.getElementPort().getOwner();
            FlowElementDescription desc = element.getDescription();
            if (desc.getKind() != FlowElementKind.OUTPUT) {
                error("{0}は出力を表現していません", desc);
                continue;
            }
            OutputDescription description = (OutputDescription) desc;
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(description);
            if (proc == null) {
                error("{0}は不明なエクスポーターを利用しています", desc);
                continue;
            }
            Export epilogue = new Export(
                    Collections.singletonList(target),
                    description,
                    proc);
            results.add(epilogue);
            LOG.debug("出力{}が追加されます", epilogue);
        }
        return results;
    }

    private List<Stage> analyzeStages(Collection<StageModel> stageModels) {
        assert stageModels != null;
        List<Stage> results = new ArrayList<JobflowModel.Stage>();
        for (StageModel model : sort(stageModels)) {
            results.add(analyzeStage(model));
        }
        return results;
    }

    private Stage analyzeStage(StageModel model) {
        assert model != null;
        LOG.debug("{}を解析しています", model);
        List<Process> processes = analyzeProcesses(model);
        List<Delivery> deliveries = analyzeDeliveries(model);
        Set<SideData> sideData = analyzeSideData(model);
        Reduce reduce = analyzeReduce(model);
        Stage stage = new Stage(
                model.getStageBlock().getStageNumber(),
                processes,
                deliveries,
                reduce,
                sideData);
        LOG.debug("{}が追加されます", model);
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
        List<Delivery> deliveries = new ArrayList<JobflowModel.Delivery>();
        for (StageModel.Sink sink : model.getStageResults()) {
            Location location = base.append(sink.getName()).asPrefix();
            deliveries.add(new Delivery(sink.getOutputs(), Collections.singleton(location)));
        }
        return deliveries;
    }

    private List<Process> analyzeProcesses(StageModel model) {
        List<Process> processes = new ArrayList<JobflowModel.Process>();
        for (StageModel.MapUnit unit : model.getMapUnits()) {
            processes.add(new Process(
                    unit.getInputs(),
                    unit.getCompiled().getQualifiedName()));
        }
        return processes;
    }

    private Set<SideData> analyzeSideData(StageModel model) {
        assert model != null;
        Set<SideData> results = new HashSet<SideData>();
        for (InputDescription input : model.getSideDataInputs()) {
            ExternalIoDescriptionProcessor proc = environment.getExternals().findProcessor(input);
            if (proc == null) {
                error("{0}は不明なインポーターを利用しています", input);
                continue;
            }
            Set<Location> locations = proc.getInputInfo(input).getLocations();
            results.add(new SideData(locations, input.getName()));
        }
        return results;
    }

    private List<StageModel> sort(Collection<StageModel> stageModels) {
        List<StageModel> models = new ArrayList<StageModel>(stageModels);
        Collections.sort(models, new Comparator<StageModel>() {
            @Override
            public int compare(StageModel o1, StageModel o2) {
                int s1 = o1.getStageBlock().getStageNumber();
                int s2 = o2.getStageBlock().getStageNumber();
                if (s1 == s2) {
                    return 0;
                }
                if (s1 < s2) {
                    return -1;
                }
                return +1;
            }
        });
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
        Set<Source> opposites = new HashSet<Source>();
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
        Map<FlowBlock.Output, Source> sources = new HashMap<FlowBlock.Output, Source>();
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
