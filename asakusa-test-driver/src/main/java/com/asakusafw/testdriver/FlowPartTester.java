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
import org.junit.Assert;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestInputPreparator;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * フロー部品用のテストドライバクラス。
 * @since 0.2.0
 */
public class FlowPartTester extends TestDriverBase {

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
     *
     * @param flowDescription フロー部品クラスのインスタンス
     * @throws IOException
     */
    public void runTest(FlowDescription flowDescription) throws IOException {

        // 初期化
        initializeClusterDirectory(driverContext.getClusterWorkDir());
        ClassLoader classLoader = this.getClass().getClassLoader();

        // テストデータの配置
        TestInputPreparator preparator = new TestInputPreparator(classLoader);
        for (FlowPartDriverInput<?> input : inputs) {
            if (input.sourceUri != null) {
                preparator.prepare(input.getModelType(), input.getImporterDescription(), input.getSourceUri());
            }
        }
        for (FlowPartDriverOutput<?> output : outputs) {
            if (output.sourceUri != null) {
                preparator.prepare(output.getModelType(), output.getImporterDescription(), output.getSourceUri());
            }
        }

        // フローコンパイラの実行
        String flowId = driverContext.getClassName().substring(driverContext.getClassName().lastIndexOf('.') + 1) + "_"
                + driverContext.getMethodName();
        File compileWorkDir = new File(driverContext.getCompileWorkBaseDir(), flowId);
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        FlowGraph flowGraph = descDriver.createFlowGraph(flowDescription);
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(flowGraph, "test.batch", flowId, "test.flowpart",
                FlowPartDriverUtils.createWorkingLocation(driverContext), compileWorkDir,
                Arrays.asList(new File[] { DirectFlowCompiler.toLibraryPath(flowDescription.getClass()) }),
                flowDescription.getClass().getClassLoader(), driverContext.getOptions());

        CommandContext context = new CommandContext(
                getFrameworkHomePath().getAbsolutePath() + "/",
                driverContext.getExecutionId(),
                driverContext.getBatchArgs());

        Map<String, String> dPropMap = createHadoopProperties(context);

        TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context, dPropMap);
        savePlan(compileWorkDir, plan);

        // コンパイル結果のジョブフローを実行
        VerifyContext verifyContext = new VerifyContext();
        executePlan(plan, jobflowInfo.getPackageFile());
        verifyContext.testFinished();

        // 実行結果の検証
        TestResultInspector inspector = new TestResultInspector(this.getClass().getClassLoader());
        StringBuilder sb = new StringBuilder("\n");
        boolean failed = false;
        for (FlowPartDriverOutput<?> output : outputs) {
            if (output.expectedUri != null) {
                List<Difference> diffList = inspect(output, verifyContext, inspector);
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
