/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.JobFlowClass;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * ジョブフロー用のテストドライバクラス。
 * @since 0.2.0
 * @version 0.5.2
 */
public class JobFlowTester extends TestDriverBase {

    static final Logger LOG = LoggerFactory.getLogger(JobFlowTester.class);
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
     * @throws IllegalStateException ジョブフローのコンパイル、入出力や検査ルールの用意に失敗した場合
     */
    public void runTest(Class<? extends FlowDescription> jobFlowDescriptionClass) {
        try {
            try {
                runTestInternal(jobFlowDescriptionClass);
            } finally {
                driverContext.cleanUpTemporaryResources();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void runTestInternal(Class<? extends FlowDescription> jobFlowDescriptionClass) throws IOException {
        LOG.info("テストを開始しています: {}", driverContext.getCallerClass().getName());

        // フローコンパイラの実行
        LOG.info("ジョブフローをコンパイルしています: {}", jobFlowDescriptionClass.getName());
        JobFlowDriver jobFlowDriver = JobFlowDriver.analyze(jobFlowDescriptionClass);
        assertFalse(jobFlowDriver.getDiagnostics().toString(), jobFlowDriver.hasError());

        // コンパイル環境の検証
        driverContext.validateCompileEnvironment();

        JobFlowClass jobFlowClass = jobFlowDriver.getJobFlowClass();
        File compileWorkDir = driverContext.getCompilerWorkingDirectory();
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        FlowGraph flowGraph = jobFlowClass.getGraph();
        String batchId = "bid";
        String flowId = jobFlowClass.getConfig().name();
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

        // 環境の検証
        driverContext.validateExecutionEnvironment();

        JobflowExecutor executor = new JobflowExecutor(driverContext);
        driverContext.prepareCurrentJobflow(jobflowInfo);

        LOG.info("テスト環境を初期化しています: {}", driverContext.getCallerClass().getName());
        executor.cleanWorkingDirectory();
        executor.cleanInputOutput(jobflowInfo);

        LOG.info("テストデータを配置しています: {}", driverContext.getCallerClass().getName());
        executor.prepareInput(jobflowInfo, inputs);
        executor.prepareOutput(jobflowInfo, outputs);

        LOG.info("ジョブフローを実行しています: {}", jobFlowDescriptionClass.getName());
        VerifyContext verifyContext = new VerifyContext(driverContext);
        executor.runJobflow(jobflowInfo);
        verifyContext.testFinished();

        LOG.info("実行結果を検証しています: {}", driverContext.getCallerClass().getName());
        executor.verify(jobflowInfo, verifyContext, outputs);
    }
}
