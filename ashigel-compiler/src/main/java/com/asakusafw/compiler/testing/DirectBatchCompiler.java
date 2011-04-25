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
package com.asakusafw.compiler.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchCompiler;
import com.asakusafw.compiler.batch.BatchCompilerConfiguration;
import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.repository.SpiDataClassRepository;
import com.asakusafw.compiler.repository.SpiExternalIoDescriptionProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowElementProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowGraphRewriterRepository;
import com.asakusafw.compiler.repository.SpiWorkflowProcessorRepository;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.util.graph.Graph;
import com.ashigeru.util.graph.Graphs;

/**
 * バッチを直接コンパイルして、JARのパッケージを作成する。
 */
public final class DirectBatchCompiler {

    static final Logger LOG = LoggerFactory.getLogger(DirectBatchCompiler.class);

    /**
     * バッチ記述をコンパイルして、JARのパッケージを作成する。
     * @param batchClass 対象のバッチクラス
     * @param basePackageName 対象フローのプログラムを出力する既定のパッケージ名
     * @param clusterWorkingDirectory 対象フローのプログラムが利用するクラスター上のディレクトリ
     * @param outputDirectory コンパイル結果の出力先
     * @param localWorkingDirectory コンパイル時に利用するローカル環境のワーキングディレクトリ
     * @param extraResources 追加リソースのディレクトリまたはZIPアーカイブの一覧
     * @param serviceClassLoader サービス情報をロードするためのクラスローダ
     * @param flowCompilerOptions フローDSLコンパイラのオプション設定
     * @return コンパイル結果
     * @throws IOException コンパイルに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static BatchInfo compile(
            Class<? extends BatchDescription> batchClass,
            String basePackageName,
            Location clusterWorkingDirectory,
            File outputDirectory,
            File localWorkingDirectory,
            List<File> extraResources,
            ClassLoader serviceClassLoader,
            FlowCompilerOptions flowCompilerOptions) throws IOException {
        Precondition.checkMustNotBeNull(batchClass, "batchClass"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(clusterWorkingDirectory, "clusterWorkingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputDirectory, "outputDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(localWorkingDirectory, "localWorkingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(extraResources, "extraResources"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(serviceClassLoader, "serviceClassLoader"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowCompilerOptions, "flowCompilerOptions"); //$NON-NLS-1$

        if (localWorkingDirectory.exists()) {
            clean(localWorkingDirectory);
        }
        BatchDriver driver = BatchDriver.analyze(batchClass);
        if (driver.hasError()) {
            throw new IOException(driver.getDiagnostics().toString());
        }

        String batchId = driver.getBatchClass().getConfig().name();
        BatchCompilerConfiguration config = createConfig(
                batchId,
                basePackageName,
                clusterWorkingDirectory,
                outputDirectory,
                localWorkingDirectory,
                extraResources,
                serviceClassLoader,
                flowCompilerOptions);

        BatchCompiler compiler = new BatchCompiler(config);
        Workflow workflow = compiler.compile(driver.getBatchClass().getDescription());
        return toInfo(workflow, outputDirectory);
    }

    /**
     * コンパイル済みのワークフローをバッチの簡易実行計画に変換して返す。
     * @param workflow 対象のワークフロー
     * @param outputDirectory ワークフロー情報の出力先
     * @return バッチの簡易実行計画
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static BatchInfo toInfo(Workflow workflow, File outputDirectory) {
        Precondition.checkMustNotBeNull(workflow, "workflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputDirectory, "outputDirectory"); //$NON-NLS-1$
        List<JobflowInfo> jobflows = new ArrayList<JobflowInfo>();
        for (Workflow.Unit unit : Graphs.sortPostOrder(workflow.getGraph())) {
            JobflowInfo jobflow = toJobflow(unit, outputDirectory);
            if (jobflow != null) {
                jobflows.add(jobflow);
            }
        }
        return new BatchInfo(workflow, outputDirectory, jobflows);
    }

    private static void clean(File localWorkingDirectory) {
        assert localWorkingDirectory != null;
        LOG.info("Cleaning local working directory: {}", localWorkingDirectory);
        delete(localWorkingDirectory);
    }

    private static boolean delete(File target) {
        assert target != null;
        boolean success = true;
        if (target.isDirectory()) {
            for (File child : target.listFiles()) {
                success &= delete(child);
            }
        }
        success &= target.delete();
        return success;
    }

    /**
     * 指定の情報を含む設定を返す。
     * @param batchId バッチID
     * @param basePackageName パッケージ名
     * @param clusterWorkingLocation クラスタ上のワーキングディレクトリ
     * @param outputDirectory 出力先のディレクトリ
     * @param localWorkingDirectory ローカルワーキングディレクトリ
     * @param extraResources 追加するクラスライブラリの一覧
     * @param serviceClassLoader サービス情報をロードするためのクラスローダ
     * @param flowCompilerOptions フローDSLコンパイラの設定
     * @return 生成した設定
     * @throws IOException 設定の展開中に解析に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static BatchCompilerConfiguration createConfig(
            String batchId,
            String basePackageName,
            Location clusterWorkingLocation,
            File outputDirectory,
            File localWorkingDirectory,
            List<File> extraResources,
            ClassLoader serviceClassLoader,
            FlowCompilerOptions flowCompilerOptions) throws IOException {
        assert batchId != null;
        assert basePackageName != null;
        assert clusterWorkingLocation != null;
        assert outputDirectory != null;
        assert localWorkingDirectory != null;
        assert extraResources != null;
        assert serviceClassLoader != null;
        assert flowCompilerOptions != null;
        BatchCompilerConfiguration config = new BatchCompilerConfiguration();
        config.setBatchId(batchId);
        config.setDataClasses(new SpiDataClassRepository());
        config.setExternals(new SpiExternalIoDescriptionProcessorRepository());
        config.setGraphRewriters(new SpiFlowGraphRewriterRepository());
        config.setFactory(Models.getModelFactory());
        config.setFlowElements(new SpiFlowElementProcessorRepository());
        config.setLinkingResources(DirectFlowCompiler.createRepositories(extraResources));
        config.setOutputDirectory(outputDirectory);
        config.setRootLocation(clusterWorkingLocation);
        config.setRootPackageName(basePackageName);
        config.setWorkflows(new SpiWorkflowProcessorRepository());
        config.setServiceClassLoader(serviceClassLoader);
        config.setWorkingDirectory(localWorkingDirectory);
        config.setFlowCompilerOptions(flowCompilerOptions);
        return config;
    }

    private static JobflowInfo toJobflow(
            Workflow.Unit unit,
            File outputDirectory) {
        assert unit != null;
        assert outputDirectory != null;
        if ((unit.getDescription() instanceof JobFlowWorkDescription) == false) {
            return null;
        }
        JobflowModel model = (JobflowModel) unit.getProcessed();
        String flowId = model.getFlowId();
        return new JobflowInfo(
                model,
                JobFlowWorkDescriptionProcessor.getPackageLocation(outputDirectory, flowId),
                JobFlowWorkDescriptionProcessor.getSourceLocation(outputDirectory, flowId),
                toStagePlan(model));
    }

    private static List<StageInfo> toStagePlan(JobflowModel jobflow) {
        assert jobflow != null;
        List<StageInfo> results = new ArrayList<StageInfo>();
        for (CompiledStage compiled : jobflow.getCompiled().getPrologueStages()) {
            results.add(toInfo(compiled));
        }
        Graph<JobflowModel.Stage> depenedencies = jobflow.getDependencyGraph();
        for (JobflowModel.Stage stage : Graphs.sortPostOrder(depenedencies)) {
            results.add(toInfo(stage.getCompiled()));
        }
        for (CompiledStage compiled : jobflow.getCompiled().getEpilogueStages()) {
            results.add(toInfo(compiled));
        }
        return results;
    }

    private static StageInfo toInfo(CompiledStage stage) {
        assert stage != null;
        String className = stage.getQualifiedName().toNameString();
        return new StageInfo(className);
    }

    private DirectBatchCompiler() {
        return;
    }
}
