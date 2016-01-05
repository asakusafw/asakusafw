/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

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
@SuppressWarnings("deprecation")
public class JobFlowTestDriver extends TestDriverTestToolsBase {

    /** バッチID。 */
    private final String batchId;

    /**
     * コンストラクタ。
     * <p>
     * バッチIDはデフォルト値("bid")を使用します。
     * </p>
     * @throws RuntimeException インスタンスの生成に失敗した場合
     */
    public JobFlowTestDriver() {
        super();
        this.batchId = "bid";
    }

    /**
     * コンストラクタ。
     *
     * @param batchId バッチID
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see JobFlowTestDriver#JobFlowTestDriver()
     */
    public JobFlowTestDriver(String batchId) {
        super();
        this.batchId = batchId;
    }

    /**
     * コンストラクタ。
     *
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see JobFlowTestDriver#JobFlowTestDriver()
     */
    public JobFlowTestDriver(List<File> testDataFileList) {
        super(testDataFileList);
        this.batchId = "bid";
    }

    /**
     * コンストラクタ。
     *
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @param batchId バッチID
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see JobFlowTestDriver#JobFlowTestDriver()
     */
    public JobFlowTestDriver(List<File> testDataFileList, String batchId) {
        super(testDataFileList);
        this.batchId = batchId;
    }

    /**
     * ジョブフローのテストを実行し、テスト結果を検証します。
     * @param jobFlowDescriptionClass ジョブフロークラスのクラスオブジェクト
     * @throws RuntimeException テストの実行に失敗した場合
     */
    public void runTest(Class<? extends FlowDescription> jobFlowDescriptionClass) {

        try {
            JobflowExecutor executor = new JobflowExecutor(driverContext);

            // クラスタワークディレクトリ初期化
            executor.cleanWorkingDirectory();

            // テストデータ生成ツールを実行し、Excel上のテストデータ定義をデータベースに登録する。
            storeDatabase();
            setLastModifiedTimestamp(new Timestamp(0L));

            // フローコンパイラの実行
            JobFlowDriver jobFlowDriver = JobFlowDriver.analyze(jobFlowDescriptionClass);
            assertFalse(
                    jobFlowDriver.getDiagnostics().toString(),
                    jobFlowDriver.hasError());
            JobFlowClass jobFlowClass = jobFlowDriver.getJobFlowClass();

            String flowId = jobFlowClass.getConfig().name();
            File compileWorkDir = driverContext.getCompilerWorkingDirectory();
            if (compileWorkDir.exists()) {
                FileUtils.forceDelete(compileWorkDir);
            }

            FlowGraph flowGraph = jobFlowClass.getGraph();
            JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                flowGraph,
                batchId,
                flowId,
                "test.jobflow",
                Location.fromPath(driverContext.getClusterWorkDir(), '/'),
                compileWorkDir,
                Arrays.asList(new File[] {
                        DirectFlowCompiler.toLibraryPath(jobFlowDescriptionClass)
                }),
                jobFlowDescriptionClass.getClassLoader(),
                driverContext.getOptions());

            driverContext.prepareCurrentJobflow(jobflowInfo);

            executor.runJobflow(jobflowInfo);

            // テスト結果検証ツールを実行し、Excel上の期待値とDB上の実際値を比較する。
            loadDatabase();
            if (!testUtils.inspect()) {
                Assert.fail(testUtils.getCauseMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            driverContext.cleanUpTemporaryResources();
        }
    }

    private void storeDatabase() {
        testUtils.storeToDatabase(false);
    }

    private void loadDatabase() {
        testUtils.loadFromDatabase();
    }
}
