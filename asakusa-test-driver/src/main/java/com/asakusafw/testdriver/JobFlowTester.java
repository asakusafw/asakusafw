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
package com.asakusafw.testdriver;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.JobFlowClass;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestInputPreparator;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * ジョブフロー用のテストドライバクラス。
 */
public class JobFlowTester extends TestDriverBase {

    /** バッチID。 */
    protected String batchId = "bid";
    /** 入力データのリスト。 */
    protected List<JobFlowDriverInput<?>> inputs = new LinkedList<JobFlowDriverInput<?>>();
    /** 出力データのリスト。 */
    protected List<JobFlowDriverOutput<?>> outputs = new LinkedList<JobFlowDriverOutput<?>>();

    /**
     * コンストラクタ。
     * 
     * @param callerClass 呼出元クラス
     */
    public JobFlowTester(Class<?> callerClass) {
        super(callerClass);
    }

    /**
     * テスト入力データを指定する。
     * 
     * @param <T> ModelType。
     * @param name 入力データ名。テストドライバに指定する入力データ間で一意の名前を指定する。
     * @param modelType ModelType。
     * @return テスト入力データオブジェクト。
     */
    public <T> JobFlowDriverInput<T> input(String name, Class<T> modelType) {
        JobFlowDriverInput<T> input = new JobFlowDriverInput<T>(driverContext, name, modelType);
        inputs.add(input);
        return input;
    }

    /**
     * テスト結果の出力データ（期待値データ）を指定する。
     * 
     * @param <T> ModelType。
     * @param name 出力データ名。テストドライバに指定する出力データ間で一意の名前を指定する。
     * @param modelType ModelType。
     * @return テスト入力データオブジェクト。
     */
    public <T> JobFlowDriverOutput<T> output(String name, Class<T> modelType) {
        JobFlowDriverOutput<T> output = new JobFlowDriverOutput<T>(driverContext, name, modelType);
        outputs.add(output);
        return output;
    }

    /**
     * ジョブフローのテストを実行し、テスト結果を検証します。
     * @param jobFlowDescriptionClass ジョブフロークラスのクラスオブジェクト
     * @throws RuntimeException テストの実行に失敗した場合
     */
    public void runTest(Class<? extends FlowDescription> jobFlowDescriptionClass) {

        try {
            // 初期化
            initializeClusterDirectory(driverContext.getClusterWorkDir());
            ClassLoader classLoader = this.getClass().getClassLoader();

            // フローコンパイラの実行
            JobFlowDriver jobFlowDriver = JobFlowDriver.analyze(jobFlowDescriptionClass);
            assertFalse(jobFlowDriver.getDiagnostics().toString(), jobFlowDriver.hasError());
            JobFlowClass jobFlowClass = jobFlowDriver.getJobFlowClass();

            String flowId = driverContext.getClassName().substring(driverContext.getClassName().lastIndexOf(".") + 1)
                    + "_" + driverContext.getMethodName();
            File compileWorkDir = new File(driverContext.getCompileWorkBaseDir(), flowId);
            if (compileWorkDir.exists()) {
                FileUtils.forceDelete(compileWorkDir);
            }

            FlowGraph flowGraph = jobFlowClass.getGraph();
            JobflowInfo jobflowInfo = DirectFlowCompiler.compile(flowGraph, batchId, flowId, "test.jobflow",
                    Location.fromPath(driverContext.getClusterWorkDir() + "/" + driverContext.getExecutionId(), '/'),
                    compileWorkDir,
                    Arrays.asList(new File[] { DirectFlowCompiler.toLibraryPath(jobFlowDescriptionClass) }),
                    jobFlowDescriptionClass.getClassLoader(), driverContext.getOptions());

            // ジョブフローのjarをImporter/Exporterが要求するディレクトリにコピー
            String jobFlowJarName = "jobflow-" + flowId + ".jar";
            File srcFile = new File(compileWorkDir, jobFlowJarName);
            File destDir = new File(System.getenv("ASAKUSA_HOME"), "batchapps/" + batchId + "/lib");
            FileUtils.copyFileToDirectory(srcFile, destDir);

            CommandContext context = new CommandContext(System.getenv("ASAKUSA_HOME") + "/",
                    driverContext.getExecutionId(), driverContext.getBatchArgs());

            Map<String, String> dPropMap = createHadoopProperties(context);

            TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context, dPropMap);
            savePlan(compileWorkDir, plan);

            // テストデータの配置
            TestInputPreparator preparator = new TestInputPreparator(classLoader);
            for (JobFlowDriverInput<?> input : inputs) {
                ImporterDescription importerDescription = jobflowInfo.findImporter(input.getName());
                preparator.truncate(input.getModelType(), importerDescription);
            }
            for (JobFlowDriverOutput<?> output : outputs) {
                ImporterDescription importerDescription = jobflowInfo.findImporter(output.getName());
                preparator.truncate(output.getModelType(), importerDescription);
            }

            for (JobFlowDriverInput<?> input : inputs) {
                if (input.sourceUri != null) {
                    ImporterDescription importerDescription = jobflowInfo.findImporter(input.getName());
                    input.setImporterDescription(importerDescription);
                    preparator.prepare(input.getModelType(), input.getImporterDescription(), input.getSourceUri());
                }
            }
            for (JobFlowDriverOutput<?> output : outputs) {
                if (output.sourceUri != null) {
                    ImporterDescription importerDescription = jobflowInfo.findImporter(output.getName());
                    output.setImporterDescription(importerDescription);
                    preparator.prepare(output.getModelType(), output.getImporterDescription(), output.getSourceUri());
                }
            }

            // コンパイル結果のジョブフローを実行            
            VerifyContext verifyContext = new VerifyContext();
            executePlan(plan, jobflowInfo.getPackageFile());
            verifyContext.testFinished();

            // 実行結果の検証
            TestResultInspector inspector = new TestResultInspector(this.getClass().getClassLoader());
            StringBuilder sb = new StringBuilder("\n");
            boolean failed = false;
            for (JobFlowDriverOutput<?> output : outputs) {
                if (output.expectedUri != null) {
                    ExporterDescription exporterDescription = jobflowInfo.findExporter(output.getName());
                    output.setExporterDescription(exporterDescription);
                    List<Difference> diffList = inspector.inspect(output.getModelType(),
                            output.getExporterDescription(), verifyContext, output.getExpectedUri(),
                            output.getVerifyRuleUri());
                    for (Difference difference : diffList) {
                        failed = true;
                        sb.append(output.getModelType().getSimpleName() + ": " + difference.getDiagnostic() + "\n");
                    }
                }
            }
            if (failed) {
                Assert.fail(sb.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
