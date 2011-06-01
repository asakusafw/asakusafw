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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestDataPreparator;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * フロー部品用のテストドライバクラス。
 * @since 0.2.0
 */
public class FlowPartTester extends TestDriverBase {

    static final Logger LOG = LoggerFactory.getLogger(FlowPartTester.class);

    private final List<FlowPartDriverInput<?>> inputs = new LinkedList<FlowPartDriverInput<?>>();
    private final List<FlowPartDriverOutput<?>> outputs = new LinkedList<FlowPartDriverOutput<?>>();

    private final FlowDescriptionDriver descDriver = new FlowDescriptionDriver();

    /**
     * コンストラクタ。
     *
     * @param callerClass 呼出元クラス
     */
    public FlowPartTester(Class<?> callerClass) {
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
    public <T> FlowPartDriverInput<T> input(String name, Class<T> modelType) {
        FlowPartDriverInput<T> input = new FlowPartDriverInput<T>(driverContext, descDriver, name, modelType);
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
    public <T> FlowPartDriverOutput<T> output(String name, Class<T> modelType) {
        FlowPartDriverOutput<T> output = new FlowPartDriverOutput<T>(driverContext, descDriver, name, modelType);
        outputs.add(output);
        return output;
    }

    /**
     * フロー部品のテストを実行し、テスト結果を検証する。
     * @param flowDescription フロー部品クラスのインスタンス
     */
    public void runTest(FlowDescription flowDescription) {
        try {
            runTestInternal(flowDescription);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void runTestInternal(FlowDescription flowDescription) throws IOException {
        LOG.info("テストを開始しています: {}", driverContext.getCallerClass().getName());

        // 初期化
        initializeClusterDirectory(driverContext.getClusterWorkDir());
        ClassLoader classLoader = this.getClass().getClassLoader();

        // フローコンパイラの実行
        LOG.info("フロー部品をコンパイルしています: {}", flowDescription.getClass().getName());
        String batchId = "testing";
        String flowId = driverContext.getMethodName();
        File compileWorkDir = driverContext.getCompilerWorkingDirectory();
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        FlowGraph flowGraph = descDriver.createFlowGraph(flowDescription);
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                flowGraph,
                batchId,
                flowId,
                "test.flowpart",
                FlowPartDriverUtils.createWorkingLocation(driverContext),
                compileWorkDir,
                Arrays.asList(new File[] {
                        DirectFlowCompiler.toLibraryPath(flowDescription.getClass())
                }),
                flowDescription.getClass().getClassLoader(),
                driverContext.getOptions());

        CommandContext context = driverContext.getCommandContext();

        Map<String, String> dPropMap = createHadoopProperties(context);

        TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context, dPropMap);
        savePlan(compileWorkDir, plan);

        // テストデータの配置
        LOG.info("テストデータを配置しています: {}", driverContext.getCallerClass().getName());
        TestDataPreparator preparator = new TestDataPreparator(classLoader);
        for (FlowPartDriverInput<?> input : inputs) {
            if (input.sourceUri != null) {
                LOG.debug("入力{}を配置しています: {}", input.getName(), input.getSourceUri());
                preparator.prepare(input.getModelType(), input.getImporterDescription(), input.getSourceUri());
            }
        }
        for (FlowPartDriverOutput<?> output : outputs) {
            if (output.sourceUri != null) {
                LOG.debug("出力{}を配置しています: {}", output.getName(), output.getSourceUri());
                preparator.prepare(output.getModelType(), output.getImporterDescription(), output.getSourceUri());
            }
        }

        // コンパイル結果のフロー部品を実行
        LOG.info("フロー部品を実行しています: {}", flowDescription.getClass().getName());
        VerifyContext verifyContext = new VerifyContext();
        executePlan(plan, jobflowInfo.getPackageFile());
        verifyContext.testFinished();

        // 実行結果の検証
        LOG.info("実行結果を検証しています: {}", driverContext.getCallerClass().getName());
        TestResultInspector inspector = new TestResultInspector(this.getClass().getClassLoader());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n"));
        boolean failed = false;
        for (FlowPartDriverOutput<?> output : outputs) {
            if (output.expectedUri != null) {
                LOG.debug("出力{}を検証しています: {}", output.getName(), output.getExpectedUri());
                List<Difference> diffList = inspect(output, verifyContext, inspector);
                if (diffList.isEmpty() == false) {
                    LOG.warn("{}の出力{}には{}個の差異があります", new Object[] {
                            flowDescription.getClass().getName(),
                            output.getName(),
                            diffList.size(),
                    });
                }
                for (Difference difference : diffList) {
                    failed = true;
                    sb.append(String.format("%s: %s%n",
                            output.getModelType().getSimpleName(),
                            difference));
                }
            }
        }
        LOG.info("実行結果を検証しました(succeeded={}): {}", !failed, driverContext.getCallerClass().getName());
        if (failed) {
            throw new AssertionError(sb);
        }
    }
}
