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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * ジョブフロー用のテストドライバクラス。
 */
public class JobFlowTestDriver extends TestDriverBase {

    /** バッチID。 */
    private String batchId;

    /**
     * コンストラクタ。
     * <p>
     * バッチIDはデフォルト値("bid")を使用します。
     * 使い方：本コンストラクタは、必ずJUnitのテストメソッドから直接呼び出して下さい。
     * テストメソッド内で、ユーティリティクラスやプライベートメソッドを経由して呼び出すことは出来ません。
     * （呼び出し元のテストクラス名、テストメソッド名に基づいて入出力データを取得・生成するため）
     * </p>
     * @throws RuntimeException インスタンスの生成に失敗した場合
     */
    public JobFlowTestDriver() throws RuntimeException {
        super();
        this.batchId = "bid";
    }

    /**
     * コンストラクタ。
     * <p>
     * 使い方の注意点は{@link JobFlowTestDriver#JobFlowTestDriver()}を参照。
     * </p>
     * @param batchId バッチID
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see JobFlowTestDriver#JobFlowTestDriver()
     */
    public JobFlowTestDriver(String batchId) throws RuntimeException {
        super();
        this.batchId = batchId;
    }

    /**
     * コンストラクタ。
     * <p>
     * 使い方の注意点は{@link JobFlowTestDriver#JobFlowTestDriver()}を参照。
     * </p>
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see JobFlowTestDriver#JobFlowTestDriver()
     */
    public JobFlowTestDriver(List<File> testDataFileList) throws RuntimeException {
        super(testDataFileList);
        this.batchId = "bid";
    }

    /**
     * コンストラクタ。
     * <p>
     * 使い方の注意点は{@link JobFlowTestDriver#JobFlowTestDriver()}を参照。
     * </p>
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @param batchId バッチID
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see JobFlowTestDriver#JobFlowTestDriver()
     */
    public JobFlowTestDriver(List<File> testDataFileList, String batchId) throws RuntimeException {
        super(testDataFileList);
        this.batchId = batchId;
    }

    /**
     * ジョブフローのテストを実行し、テスト結果を検証します。
     * @param jobFlowDescriptionClass ジョブフロークラスのクラスオブジェクト
     * @throws RuntimeException テストの実行に失敗した場合
     */
    public void runTest(Class<? extends FlowDescription> jobFlowDescriptionClass) throws RuntimeException {

        try {
            // クラスタワークディレクトリ初期化
            initializeClusterDirectory(driverContext.getClusterWorkDir());

            // テストデータ生成ツールを実行し、Excel上のテストデータ定義をデータベースに登録する。
            storeDatabase();

            // フローコンパイラの実行
            JobFlowDriver jobFlowDriver = JobFlowDriver.analyze(jobFlowDescriptionClass);
            assertFalse(
                    jobFlowDriver.getDiagnostics().toString(),
                    jobFlowDriver.hasError());
            JobFlowClass jobFlowClass = jobFlowDriver.getJobFlowClass();

            String flowId = driverContext.getClassName().substring(driverContext.getClassName().lastIndexOf(".") + 1) + "_" + driverContext.getMethodName();
            File compileWorkDir = new File(driverContext.getCompileWorkBaseDir(), flowId);
            if (compileWorkDir.exists()) {
                FileUtils.forceDelete(compileWorkDir);
            }

            FlowGraph flowGraph = jobFlowClass.getGraph();
            JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                flowGraph,
                batchId,
                flowId,
                "test.jobflow",
                Location.fromPath(driverContext.getClusterWorkDir() + "/" + driverContext.getExecutionId(), '/'),
                compileWorkDir,
                Arrays.asList(new File[] {
                        DirectFlowCompiler.toLibraryPath(jobFlowDescriptionClass)
                }),
                jobFlowDescriptionClass.getClassLoader(),
                driverContext.getOptions());

            // ジョブフローのjarをImporter/Exporterが要求するディレクトリにコピー
            String jobFlowJarName = "jobflow-" + flowId + ".jar";
            File srcFile = new File(compileWorkDir, jobFlowJarName);
            File destDir = new File(System.getenv("ASAKUSA_HOME"), "batchapps/" + batchId + "/lib");
            FileUtils.copyFileToDirectory(srcFile, destDir);

            CommandContext context = new CommandContext(
                    System.getenv("ASAKUSA_HOME") + "/",
                    driverContext.getExecutionId(),
                    driverContext.getBatchArgs());

            Map<String, String> dPropMap = createHadoopProperties(context);

            TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context, dPropMap);
            savePlan(compileWorkDir, plan);
            executePlan(plan, jobflowInfo.getPackageFile());

            // テスト結果検証ツールを実行し、Excel上の期待値とDB上の実際値を比較する。
            loadDatabase();
            if (!testUtils.inspect()) {
                Assert.fail(testUtils.getCauseMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeDatabase() {
        testUtils.storeToDatabase(false);
    }

    private void loadDatabase() {
        testUtils.loadFromDatabase();
    }
}
