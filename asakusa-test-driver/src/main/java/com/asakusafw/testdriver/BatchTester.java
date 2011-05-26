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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import com.asakusafw.compiler.batch.BatchClass;
import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestInputPreparator;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * バッチ用のテストドライバクラス。
 */
public class BatchTester extends TestDriverBase {

    private Map<String, JobFlowTester> jobFlowMap = new HashMap<String, JobFlowTester>();

    /**
     * コンストラクタ。
     * 
     * @param callerClass 呼出元クラス
     */
    public BatchTester(Class<?> callerClass) {
        super(callerClass);
    }
    
    /**
     * バッチに含まれるジョブフローを指定する。
     * 
     * @param name ジョブフロー名。ジョブフロークラスのアノテーションnameの値を指定する。
     * @return ジョブフローテストドライバ。
     */
    public JobFlowTester jobflow(String name) {
        JobFlowTester driver = new JobFlowTester(driverContext.getCallerClass());
        jobFlowMap.put(name, driver);
        return driver;
    }

    /**
     * バッチのテストを実行し、テスト結果を検証します。
     * @param batchDescriptionClass ジョブフロークラスのクラスオブジェクト
     * @throws RuntimeException テストの実行に失敗した場合
     */
    public void runTest(Class<? extends BatchDescription> batchDescriptionClass) {

        try {
            // 初期化
            initializeClusterDirectory(driverContext.getClusterWorkDir());
            ClassLoader classLoader = this.getClass().getClassLoader();

            // バッチコンパイラの実行
            BatchDriver batchDriver = BatchDriver.analyze(batchDescriptionClass);
            assertFalse(batchDriver.getDiagnostics().toString(), batchDriver.hasError());
            BatchClass batchClass = batchDriver.getBatchClass();

            String batchId = batchClass.getConfig().name();
            File compileWorkDir = new File(driverContext.getCompileWorkBaseDir(), batchId
                    + System.getProperty("file.separator") + driverContext.getExecutionId());
            if (compileWorkDir.exists()) {
                FileUtils.forceDelete(compileWorkDir);
            }

            File compilerOutputDir = new File(compileWorkDir, "output");
            File compilerLocalWorkingDir = new File(compileWorkDir, "build");

            BatchInfo batchInfo = DirectBatchCompiler.compile(batchDescriptionClass, "test.batch",
                    Location.fromPath(driverContext.getClusterWorkDir() + "/" + driverContext.getExecutionId(), '/'),
                    compilerOutputDir, compilerLocalWorkingDir,
                    Arrays.asList(new File[] { DirectFlowCompiler.toLibraryPath(batchDescriptionClass) }),
                    batchDescriptionClass.getClassLoader(), driverContext.getOptions());

            // バッチ実行前のtruncate
            List<JobflowInfo> jobflowInfos = batchInfo.getJobflows();
            for (JobflowInfo jobflowInfo : jobflowInfos) {
                String flowId = jobflowInfo.getJobflow().getFlowId();
                JobFlowTester driver = jobFlowMap.get(flowId);

                TestInputPreparator preparator = new TestInputPreparator(classLoader);
                for (JobFlowDriverInput<?> input : driver.inputs) {
                    ImporterDescription importerDescription = jobflowInfo.findImporter(input.getName());
                    preparator.truncate(input.getModelType(), importerDescription);
                }
                for (JobFlowDriverOutput<?> output : driver.outputs) {
                    ImporterDescription importerDescription = jobflowInfo.findImporter(output.getName());
                    preparator.truncate(output.getModelType(), importerDescription);
                }

            }

            // バッチに含まれるジョブフローを実行
            for (JobflowInfo jobflowInfo : jobflowInfos) {
                String flowId = jobflowInfo.getJobflow().getFlowId();
                JobFlowTester driver = jobFlowMap.get(flowId);

                // ジョブフローのjarをImporter/Exporterが要求するディレクトリにコピー
                String jobFlowJarName = "jobflow-" + flowId + ".jar";
                File srcFile = new File(compilerOutputDir, "lib/" + jobFlowJarName);
                File destDir = new File(System.getenv("ASAKUSA_HOME"), "batchapps/" + batchId + "/lib");
                FileUtils.copyFileToDirectory(srcFile, destDir);

                CommandContext context = new CommandContext(System.getenv("ASAKUSA_HOME") + "/",
                        driverContext.getExecutionId(), driverContext.getBatchArgs());

                Map<String, String> dPropMap = createHadoopProperties(context);

                TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context, dPropMap);
                savePlan(compileWorkDir, plan);

                // テストデータの配置
                if (driver != null) {
                    TestInputPreparator preparator = new TestInputPreparator(classLoader);
                    for (JobFlowDriverInput<?> input : driver.inputs) {
                        if (input.sourceUri != null) {
                            ImporterDescription importerDescription = jobflowInfo.findImporter(input.getName());
                            input.setImporterDescription(importerDescription);
                            preparator.prepare(input.getModelType(), input.getImporterDescription(), input.getSourceUri());
                        }
                    }
                    for (JobFlowDriverOutput<?> output : driver.outputs) {
                        if (output.sourceUri != null) {
                            ImporterDescription importerDescription = jobflowInfo.findImporter(output.getName());
                            output.setImporterDescription(importerDescription);
                            preparator.prepare(output.getModelType(), output.getImporterDescription(),
                                    output.getSourceUri());
                        }
                    }
                }

                // コンパイル結果のジョブフローを実行                
                VerifyContext verifyContext = new VerifyContext();
                executePlan(plan, jobflowInfo.getPackageFile());
                verifyContext.testFinished();

                // 実行結果の検証
                if (driver != null) {
                    TestResultInspector inspector = new TestResultInspector(this.getClass().getClassLoader());
                    StringBuilder sb = new StringBuilder("\n");
                    boolean failed = false;
                    for (JobFlowDriverOutput<?> output : driver.outputs) {
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
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
