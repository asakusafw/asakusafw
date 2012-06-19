/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.Work;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * バッチDSLをコンパイルするコンパイラ。
 */
public class BatchCompiler {

    private final BatchCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param configuration コンパイラの設定
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public BatchCompiler(BatchCompilerConfiguration configuration) {
        Precondition.checkMustNotBeNull(configuration, "configuration"); //$NON-NLS-1$
        this.environment = new BatchCompilingEnvironment(configuration).bless();
    }

    /**
     * 指定のバッチ記述に対するコンパイルを実行する。
     * @param description 対象のバッチ記述
     * @return コンパイル結果のワークフロー
     * @throws IOException コンパイルに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Workflow compile(BatchDescription description) throws IOException {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        Workflow workflow = createWorkflow(description);
        processUnits(workflow.getGraph().getNodeSet());
        if (environment.hasError()) {
            throw new IOException(MessageFormat.format(
                    "ワークフローの解析に失敗したため、処理を中断します: {0}",
                    environment.getErrorMessage()));
        }
        processWorkflow(workflow);
        if (environment.hasError()) {
            throw new IOException(MessageFormat.format(
                    "ワークフローの出力に失敗したため、処理を中断します: {0}",
                    environment.getErrorMessage()));
        }
        return workflow;
    }

    private void processWorkflow(Workflow workflow) throws IOException {
        assert workflow != null;
        Set<WorkDescription> descriptions = Sets.create();
        for (Workflow.Unit unit : workflow.getGraph().getNodeSet()) {
            descriptions.add(unit.getDescription());
        }
        WorkflowProcessor.Repository repo = environment.getWorkflows();
        Set<WorkflowProcessor> procs = repo.findWorkflowProcessors(descriptions);
        for (WorkflowProcessor proc : procs) {
            proc.process(workflow);
        }
    }

    private void processUnits(Set<Workflow.Unit> units) throws IOException {
        assert units != null;
        WorkflowProcessor.Repository repo = environment.getWorkflows();
        for (Workflow.Unit unit : units) {
            WorkDescriptionProcessor<?> proc = repo.findDescriptionProcessor(unit.getDescription());
            if (proc == null) {
                environment.error("{}に対するプロセッサが見つかりません",
                        unit.getClass().getName());
                continue;
            }
            processUnit(unit, proc);
        }
    }

    private <T extends WorkDescription> void processUnit(
            Workflow.Unit unit,
            WorkDescriptionProcessor<T> proc) throws IOException {
        assert unit != null;
        assert proc != null;
        assert proc.getTargetType().isInstance(unit.getDescription());
        T desc = proc.getTargetType().cast(unit.getDescription());
        Object result = proc.process(desc);
        unit.setProcessed(result);
    }

    private Workflow createWorkflow(BatchDescription description) {
        assert description != null;
        Collection<Work> works = description.getWorks();
        Map<Work, Workflow.Unit> units = Maps.create();
        for (Work work : works) {
            units.put(work, new Workflow.Unit(work.getDescription()));
        }
        Graph<Workflow.Unit> graph = Graphs.newInstance();
        for (Map.Entry<Work, Workflow.Unit> entry : units.entrySet()) {
            Workflow.Unit unit = entry.getValue();
            graph.addNode(unit);
            for (Work dependency : entry.getKey().getDependencies()) {
                Workflow.Unit predecessor = units.get(dependency);
                assert predecessor != null;
                graph.addEdge(unit, predecessor);
            }
        }
        Workflow workflow = new Workflow(description, graph);
        return workflow;
    }
}
