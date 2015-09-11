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

import static org.junit.Assert.*;

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

import com.asakusafw.compiler.flow.JobFlowClass;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * ジョブフロー用のテストドライバクラス。
 * @since 0.2.0
 * @version 0.5.2
 */
public class JobFlowTester extends TesterBase {

    static final Logger LOG = LoggerFactory.getLogger(JobFlowTester.class);

    /**
     * The flow inputs.
     */
    protected final List<JobFlowDriverInput<?>> inputs = new LinkedList<JobFlowDriverInput<?>>();

    /**
     * The flow outputs.
     */
    protected final List<JobFlowDriverOutput<?>> outputs = new LinkedList<JobFlowDriverOutput<?>>();

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
     * @param <T> the data model type
     * @param name 入力データ名。テストドライバに指定する入力データ間で一意の名前を指定する
     * @param modelType the data model type
     * @return テスト入力データオブジェクト
     */
    public <T> JobFlowDriverInput<T> input(String name, Class<T> modelType) {
        JobFlowDriverInput<T> input = new JobFlowDriverInput<T>(driverContext, name, modelType);
        inputs.add(input);
        return input;
    }

    /**
     * テスト結果の出力データ（期待値データ）を指定する。
     *
     * @param <T> the data model type
     * @param name 出力データ名。テストドライバに指定する出力データ間で一意の名前を指定する
     * @param modelType the data model type
     * @return テスト出力データオブジェクト
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
        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoStart"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));

        if (driverContext.isSkipValidateCondition() == false) {
            LOG.info(MessageFormat.format(
                    Messages.getString("JobFlowTester.infoVerifyCondition"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            validateTestCondition();
        }

        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoCompileDsl"), //$NON-NLS-1$
                jobFlowDescriptionClass.getName()));
        JobFlowDriver jobFlowDriver = JobFlowDriver.analyze(jobFlowDescriptionClass);
        assertFalse(jobFlowDriver.getDiagnostics().toString(), jobFlowDriver.hasError());

        driverContext.validateCompileEnvironment();

        JobFlowClass jobFlowClass = jobFlowDriver.getJobFlowClass();
        File compileWorkDir = driverContext.getCompilerWorkingDirectory();
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        FlowGraph flowGraph = jobFlowClass.getGraph();
        String batchId = "testing"; //$NON-NLS-1$
        String flowId = jobFlowClass.getConfig().name();
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                flowGraph,
                batchId,
                flowId,
                "test.jobflow", //$NON-NLS-1$
                Location.fromPath(driverContext.getClusterWorkDir(), '/'),
                compileWorkDir,
                Arrays.asList(new File[] {
                        DirectFlowCompiler.toLibraryPath(jobFlowDescriptionClass)
                }),
                jobFlowDescriptionClass.getClassLoader(),
                driverContext.getOptions());

        driverContext.validateExecutionEnvironment();

        JobflowExecutor executor = new JobflowExecutor(driverContext);
        driverContext.prepareCurrentJobflow(jobflowInfo);

        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoInitializeEnvironment"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));
        executor.cleanWorkingDirectory();
        executor.cleanInputOutput(jobflowInfo);
        executor.cleanExtraResources(getExternalResources());

        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoPrepareData"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));
        executor.prepareExternalResources(getExternalResources());
        executor.prepareInput(jobflowInfo, inputs);
        executor.prepareOutput(jobflowInfo, outputs);

        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoExecute"), //$NON-NLS-1$
                jobFlowDescriptionClass.getName()));
        VerifyContext verifyContext = new VerifyContext(driverContext);
        executor.runJobflow(jobflowInfo);
        verifyContext.testFinished();

        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoVerifyResult"), //$NON-NLS-1$
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
