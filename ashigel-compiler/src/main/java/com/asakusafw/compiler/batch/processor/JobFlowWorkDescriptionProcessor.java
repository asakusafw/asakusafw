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
package com.asakusafw.compiler.batch.processor;

import java.io.File;
import java.io.IOException;

import com.asakusafw.compiler.batch.AbstractWorkDescriptionProcessor;
import com.asakusafw.compiler.batch.BatchCompilerConfiguration;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompiler;
import com.asakusafw.compiler.flow.FlowCompilerConfiguration;
import com.asakusafw.compiler.flow.JobFlowClass;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.Packager;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.packager.FilePackager;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;


/**
 * {@link JobFlowWorkDescription}を処理するプロセッサ。
 */
public class JobFlowWorkDescriptionProcessor
        extends AbstractWorkDescriptionProcessor<JobFlowWorkDescription> {

    /**
     * ジョブフローパッケージ(JAR)の出力先ディレクトリ (バッチ結果出力先の相対パス)。
     */
    public static final String JOBFLOW_PACKAGE = "lib";

    /**
     * ジョブフローのビルド用作業ディレクトリ。
     */
    private static final String JOBFLOW_TEMPORARY = "build";

    @Override
    public JobflowModel process(
            JobFlowWorkDescription description) throws IOException {
        JobflowModel model = build(description);
        return model;
    }

    private JobflowModel build(
            JobFlowWorkDescription description) throws IOException {
        JobFlowClass jobflow = analyze(description);
        if (jobflow == null) {
            return null;
        }
        FlowCompilerConfiguration config = createConfiguration(jobflow);
        FlowCompiler compiler = new FlowCompiler(config);
        JobflowModel model = compiler.compile(jobflow.getGraph());

        File batchOutput = getEnvironment().getConfiguration().getOutputDirectory();
        String flowId = compiler.getTargetFlowId();
        compiler.buildSources(getPackageLocation(batchOutput, flowId));
        compiler.collectSources(getSourceLocation(batchOutput, flowId));
        return model;
    }

    private FlowCompilerConfiguration createConfiguration(JobFlowClass jobflow) {
        assert jobflow != null;
        BatchCompilerConfiguration batch = getEnvironment().getConfiguration();
        FlowCompilerConfiguration result = new FlowCompilerConfiguration();
        result.setBatchId(batch.getBatchId());
        result.setDataClasses(batch.getDataClasses());
        result.setExternals(batch.getExternals());
        result.setFactory(batch.getFactory());
        result.setFlowId(jobflow.getConfig().name());
        result.setGraphRewriters(batch.getGraphRewriters());
        result.setPackager(createPackager(jobflow));
        result.setProcessors(batch.getFlowElements());
        result.setRootLocation(batch.getRootLocation());
        result.setRootPackageName(batch.getRootPackageName());
        result.setServiceClassLoader(batch.getServiceClassLoader());
        result.setOptions(getEnvironment().getConfiguration().getFlowCompilerOptions());
        return result;
    }

    private Packager createPackager(JobFlowClass jobflow) {
        assert jobflow != null;
        BatchCompilerConfiguration batch = getEnvironment().getConfiguration();
        return new FilePackager(
                new File(
                        new File(batch.getWorkingDirectory(), jobflow.getConfig().name()),
                        JOBFLOW_TEMPORARY),
                batch.getLinkingResources());
    }

    private JobFlowClass analyze(JobFlowWorkDescription description) {
        assert description != null;
        Class<? extends FlowDescription> flowClass = description.getFlowClass();
        JobFlowDriver driver = JobFlowDriver.analyze(flowClass);
        if (driver.hasError()) {
            for (String message : driver.getDiagnostics()) {
                getEnvironment().error(message);
            }
            return null;
        }
        return driver.getJobFlowClass();
    }

    /**
     * 対象のジョブフローをコンパイルした結果のクラスパッケージファイルへのパスを返す。
     * @param batchOutput バッチの出力先ディレクトリ
     * @param flowId 対象のフローID
     * @return 対象ファイルへのパス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static File getPackageLocation(File batchOutput, String flowId) {
        Precondition.checkMustNotBeNull(batchOutput, "batchOutput"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        File dir = new File(batchOutput, JOBFLOW_PACKAGE);
        File file = new File(dir, Naming.getJobflowClassPackageName(flowId));
        return file;
    }

    /**
     * 対象のジョブフローをコンパイルした結果のソースバンドルファイルへのパスを返す。
     * @param batchOutput バッチの出力先ディレクトリ
     * @param flowId 対象のフローID
     * @return 対象ファイルへのパス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static File getSourceLocation(File batchOutput, String flowId) {
        Precondition.checkMustNotBeNull(batchOutput, "batchOutput"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        File dir = new File(batchOutput, JOBFLOW_PACKAGE);
        File file = new File(dir, Naming.getJobflowSourceBundleName(flowId));
        return file;
    }
}
