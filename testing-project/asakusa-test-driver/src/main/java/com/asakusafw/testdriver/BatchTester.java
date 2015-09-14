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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

// TODO i18n
/**
 * バッチ用のテストドライバクラス。
 * @since 0.2.0
 * @version 0.5.2
 */
public class BatchTester extends TesterBase {

    static final Logger LOG = LoggerFactory.getLogger(BatchTester.class);

    private final Map<String, JobFlowTester> jobFlowMap = new LinkedHashMap<String, JobFlowTester>();

    /**
     * インスタンスを生成する。
     * @param callerClass 呼出元クラス
     */
    public BatchTester(Class<?> callerClass) {
        super(callerClass);
    }

    /**
     * 設定対象のジョブフローに関するドライバを返す。
     * @param name 対象のフローID
     * @return ジョブフローテストドライバ
     */
    public JobFlowTester jobflow(String name) {
        JobFlowTester driver = jobFlowMap.get(name);
        if (driver == null) {
            driver = new JobFlowTester(driverContext.getCallerClass());
            jobFlowMap.put(name, driver);
        }
        return driver;
    }

    /**
     * バッチのテストを実行し、テスト結果を検証する。
     * @param batchDescriptionClass ジョブフロークラスのクラスオブジェクト
     * @throws IllegalStateException バッチのコンパイル、入出力や検査ルールの用意に失敗した場合
     */
    public void runTest(Class<? extends BatchDescription> batchDescriptionClass) {
        try {
            try {
                runTestInternal(batchDescriptionClass);
            } finally {
                driverContext.cleanUpTemporaryResources();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void runTestInternal(Class<? extends BatchDescription> batchDescriptionClass) throws IOException {
        LOG.info(MessageFormat.format(
                Messages.getString("BatchTester.infoStart"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));

        if (driverContext.isSkipValidateCondition() == false) {
            LOG.info(MessageFormat.format(
                    Messages.getString("BatchTester.infoVerifyCondition"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            validateTestCondition();
        }

        LOG.info(MessageFormat.format(
                Messages.getString("BatchTester.infoCompile"), //$NON-NLS-1$
                batchDescriptionClass.getName()));

        BatchDriver batchDriver = BatchDriver.analyze(batchDescriptionClass);
        assertFalse(batchDriver.getDiagnostics().toString(), batchDriver.hasError());

        driverContext.validateCompileEnvironment();

        File compileWorkDir = driverContext.getCompilerWorkingDirectory();
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        File compilerOutputDir = new File(compileWorkDir, "output"); //$NON-NLS-1$
        File compilerLocalWorkingDir = new File(compileWorkDir, "build"); //$NON-NLS-1$

        BatchInfo batchInfo = DirectBatchCompiler.compile(
                batchDescriptionClass,
                "test.batch", //$NON-NLS-1$
                Location.fromPath(driverContext.getClusterWorkDir(), '/'),
                compilerOutputDir,
                compilerLocalWorkingDir,
                Arrays.asList(new File[] {
                        DirectFlowCompiler.toLibraryPath(batchDescriptionClass)
                }),
                batchDescriptionClass.getClassLoader(),
                driverContext.getOptions());

        for (String flowId : jobFlowMap.keySet()) {
            if (batchInfo.findJobflow(flowId) == null) {
                throw new IllegalStateException(MessageFormat.format(
                        Messages.getString("BatchTester.errorMissingJobflow"), //$NON-NLS-1$
                        driverContext.getCallerClass().getName(),
                        flowId));
            }
        }

        driverContext.validateExecutionEnvironment();

        LOG.info(MessageFormat.format(
                Messages.getString("BatchTester.infoInitializeEnvironment"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));
        JobflowExecutor executor = new JobflowExecutor(driverContext);
        executor.cleanWorkingDirectory();
        for (JobflowInfo jobflowInfo : batchInfo.getJobflows()) {
            driverContext.prepareCurrentJobflow(jobflowInfo);
            executor.cleanInputOutput(jobflowInfo);
        }
        executor.cleanExtraResources(getExternalResources());

        if (getExternalResources().isEmpty() == false) {
            LOG.debug("initializing external resources: {}", //$NON-NLS-1$
                    batchDescriptionClass.getName());
            executor.prepareExternalResources(getExternalResources());
        }

        for (JobflowInfo jobflowInfo : batchInfo.getJobflows()) {
            driverContext.prepareCurrentJobflow(jobflowInfo);
            String flowId = jobflowInfo.getJobflow().getFlowId();
            JobFlowTester tester = jobFlowMap.get(flowId);
            if (tester != null) {
                LOG.debug("initializing jobflow input/output: {}#{}", //$NON-NLS-1$
                        batchDescriptionClass.getName(), flowId);
                executor.prepareInput(jobflowInfo, tester.inputs);
                executor.prepareOutput(jobflowInfo, tester.outputs);

                LOG.info(MessageFormat.format(
                        Messages.getString("BatchTester.infoExecute"), //$NON-NLS-1$
                        batchDescriptionClass.getName(), flowId));
                VerifyContext verifyContext = new VerifyContext(driverContext);
                executor.runJobflow(jobflowInfo);
                verifyContext.testFinished();

                LOG.info(MessageFormat.format(
                        Messages.getString("BatchTester.infoVerifyResult"), //$NON-NLS-1$
                        batchDescriptionClass.getName(), flowId));
                executor.verify(jobflowInfo, verifyContext, tester.outputs);
            }
        }
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
        for (Map.Entry<String, JobFlowTester> entry : jobFlowMap.entrySet()) {
            validateTestCondition(entry.getValue(), entry.getKey());
        }
    }

    private void validateTestCondition(JobFlowTester flow, String id) throws IOException {
        TestModerator moderator = new TestModerator(driverContext.getRepository(), driverContext);
        for (DriverInputBase<?> port : flow.inputs) {
            String label = String.format("Input(flow=%s, name=%s)", id, port.getName()); //$NON-NLS-1$
            Class<?> type = port.getModelType();
            DataModelSourceFactory source = port.getSource();
            if (source != null) {
                moderator.validate(type, label, source);
            }
        }
        for (DriverOutputBase<?> port : flow.outputs) {
            String label = String.format("Output(flow=%s, name=%s)", id, port.getName()); //$NON-NLS-1$
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
