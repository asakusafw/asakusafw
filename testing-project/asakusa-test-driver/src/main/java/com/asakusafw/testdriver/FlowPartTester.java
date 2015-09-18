/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

// i18n
/**
 * フロー部品用のテストドライバクラス。
 * @since 0.2.0
 * @version 0.5.2
 */
public class FlowPartTester extends TesterBase {

    static final Logger LOG = LoggerFactory.getLogger(FlowPartTester.class);

    private final List<FlowPartDriverInput<?>> inputs = new LinkedList<FlowPartDriverInput<?>>();
    private final List<FlowPartDriverOutput<?>> outputs = new LinkedList<FlowPartDriverOutput<?>>();

    private final FlowDescriptionDriver descDriver = new FlowDescriptionDriver();

    /**
     * インスタンスを生成する。
     * @param callerClass 呼出元クラス
     */
    public FlowPartTester(Class<?> callerClass) {
        super(callerClass);
    }

    /**
     * テスト入力データを指定する。
     * @param <T> the data model type
     * @param name 入力データ名 - テストドライバに指定する入力データ間で一意の名前を指定する
     * @param modelType the data model class
     * @return テスト入力データオブジェクト
     */
    public <T> FlowPartDriverInput<T> input(String name, Class<T> modelType) {
        FlowPartDriverInput<T> input = new FlowPartDriverInput<T>(driverContext, descDriver, name, modelType);
        inputs.add(input);
        return input;
    }

    /**
     * テスト結果の出力データ（期待値データ）を指定する。
     * @param <T> the data model type
     * @param name 出力データ名 - テストドライバに指定する出力データ間で一意の名前を指定する
     * @param modelType the data model class
     * @return テスト出力データオブジェクト
     */
    public <T> FlowPartDriverOutput<T> output(String name, Class<T> modelType) {
        FlowPartDriverOutput<T> output = new FlowPartDriverOutput<T>(driverContext, descDriver, name, modelType);
        outputs.add(output);
        return output;
    }

    /**
     * フロー部品のテストを実行し、テスト結果を検証する。
     * @param flowDescription フロー部品クラスのインスタンス
     * @throws IllegalStateException 入出力や検査ルールの用意に失敗した場合
     */
    public void runTest(FlowDescription flowDescription) {
        try {
            try {
                runTestInternal(flowDescription);
            } finally {
                driverContext.cleanUpTemporaryResources();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void runTestInternal(FlowDescription flowDescription) throws IOException {
        LOG.info(MessageFormat.format(
                Messages.getString("FlowPartTester.infoStart"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));

        if (driverContext.isSkipValidateCondition() == false) {
            LOG.info(MessageFormat.format(
                    Messages.getString("FlowPartTester.infoVerifyCondition"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            validateTestCondition();
        }

        LOG.info(MessageFormat.format(
                Messages.getString("FlowPartTester.infoCompileDsl"), //$NON-NLS-1$
                flowDescription.getClass().getName()));
        FlowGraph flowGraph = descDriver.createFlowGraph(flowDescription);

        driverContext.validateCompileEnvironment();

        File compileWorkDir = driverContext.getCompilerWorkingDirectory();
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        String batchId = "testing"; //$NON-NLS-1$
        String flowId = "flowpart"; //$NON-NLS-1$
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                flowGraph,
                batchId,
                flowId,
                "test.flowpart", //$NON-NLS-1$
                FlowPartDriverUtils.createWorkingLocation(driverContext),
                compileWorkDir,
                Arrays.asList(new File[] {
                        DirectFlowCompiler.toLibraryPath(flowDescription.getClass())
                }),
                flowDescription.getClass().getClassLoader(),
                driverContext.getOptions());

        driverContext.validateExecutionEnvironment();

        JobflowExecutor executor = new JobflowExecutor(driverContext);
        driverContext.prepareCurrentJobflow(jobflowInfo);

        LOG.info(MessageFormat.format(
                Messages.getString("FlowPartTester.infoInitializeEnvironment"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));
        executor.cleanWorkingDirectory();
        executor.cleanInputOutput(jobflowInfo);
        executor.cleanExtraResources(getExternalResources());

        LOG.info(MessageFormat.format(
                Messages.getString("FlowPartTester.infoPrepareData"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));
        executor.prepareExternalResources(getExternalResources());
        executor.prepareInput(jobflowInfo, inputs);
        executor.prepareOutput(jobflowInfo, outputs);

        LOG.info(MessageFormat.format(
                Messages.getString("FlowPartTester.infoExecute"), //$NON-NLS-1$
                flowDescription.getClass().getName()));
        VerifyContext verifyContext = new VerifyContext(driverContext);
        executor.runJobflow(jobflowInfo);
        verifyContext.testFinished();

        LOG.info(MessageFormat.format(
                Messages.getString("FlowPartTester.infoVerifyResult"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));
        executor.verify(jobflowInfo, verifyContext, outputs);
    }

    private void validateTestCondition() throws IOException {
        TestModerator moderator = new TestModerator(driverContext.getRepository(), driverContext);
        for (Map.Entry<? extends ImporterDescription, ? extends DataModelSourceFactory> entry
                : getExternalResources().entrySet()) {
            ImporterDescription description = entry.getKey();
            String label = String.format("Resource(%s)", description); //$NON-NLS-1$
            DataModelSourceFactory source = entry.getValue();
            moderator.validate(entry.getKey().getModelType(), label, source);
        }
        for (DriverInputBase<?> port : inputs) {
            String label = String.format("Input(%s)", port.getName()); //$NON-NLS-1$
            Class<?> type = port.getModelType();
            DataModelSourceFactory source = port.getSource();
            if (source != null) {
                moderator.validate(type, label, source);
            }
        }
        for (DriverOutputBase<?> port : outputs) {
            String label = String.format("Output(%s)", port.getName()); //$NON-NLS-1$
            Class<?> type = port.getModelType();
            DataModelSourceFactory source = port.getSource();
            if (source != null) {
                moderator.validate(type, label, source);
            }
            VerifierFactory verifier = port.getVerifier();
            if (verifier != null) {
                moderator.validate(type, label, verifier);
            }
        }
    }
}
