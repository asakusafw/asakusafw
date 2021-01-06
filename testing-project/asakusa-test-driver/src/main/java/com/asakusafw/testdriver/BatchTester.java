/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.BatchMirror;
import com.asakusafw.testdriver.compiler.CompilerConfiguration;
import com.asakusafw.testdriver.compiler.CompilerSession;
import com.asakusafw.testdriver.compiler.CompilerToolkit;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.JobflowInfo;

/**
 * A tester for {@code Batch batch} classes.
 * @since 0.2.0
 * @version 0.8.0
 */
public class BatchTester extends TesterBase {

    static final Logger LOG = LoggerFactory.getLogger(BatchTester.class);

    private final CompilerToolkit toolkit;

    private final Map<String, JobFlowTester> jobflowMap = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     * @param callerClass the caller class (usually it is a test class)
     */
    public BatchTester(Class<?> callerClass) {
        super(callerClass);
        this.toolkit = Util.getToolkit(callerClass);
    }

    /**
     * Returns a jobflow tester for the target jobflow in the testing batch.
     * @param flowId the target flow ID
     * @return the corresponding jobflow tester
     */
    public JobFlowTester jobflow(String flowId) {
        JobFlowTester driver = jobflowMap.get(flowId);
        if (driver == null) {
            driver = new JobFlowTester(driverContext.getCallerClass());
            jobflowMap.put(flowId, driver);
        }
        return driver;
    }

    /**
     * Executes a batch and then verifies the execution result.
     * @param description the target batch class
     * @throws IllegalStateException if error was occurred while building batch class or initializing this tester
     * @throws AssertionError if verification was failed
     */
    public void runTest(Class<? extends BatchDescription> description) {
        try {
            try {
                runTestInternal(description);
            } finally {
                driverContext.cleanUpTemporaryResources();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void runTestInternal(Class<? extends BatchDescription> batchClass) throws IOException {
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
                batchClass.getName()));

        CompilerConfiguration configuration = Util.getConfiguration(toolkit, driverContext);
        try (CompilerSession compiler = toolkit.newSession(configuration)) {
            ArtifactMirror artifact = compiler.compileBatch(batchClass);
            Util.deploy(driverContext, artifact);

            BatchMirror batch = artifact.getBatch();

            for (String flowId : jobflowMap.keySet()) {
                if (batch.findElement(flowId).isPresent() == false) {
                    throw new IllegalStateException(MessageFormat.format(
                            Messages.getString("BatchTester.errorMissingJobflow"), //$NON-NLS-1$
                            driverContext.getCallerClass().getName(),
                            flowId));
                }
            }

            TaskExecutors.findFrameworkHome(driverContext.getEnvironmentVariables())
                    .filter(Files::isDirectory)
                    .orElseThrow(() -> new IllegalStateException(
                            "BatchTester requires Asakusa Framework installation"));
            driverContext.validateExecutionEnvironment();

            LOG.info(MessageFormat.format(
                    Messages.getString("BatchTester.infoInitializeEnvironment"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            JobflowExecutor executor = new JobflowExecutor(driverContext);
            executor.cleanWorkingDirectory();

            for (JobflowMirror jobflow : batch.getElements()) {
                Util.prepare(driverContext, batch, jobflow);
                executor.validateJobflow(jobflow);
                executor.cleanInputOutput(jobflow);
            }
            executor.cleanExtraResources(getExternalResources());

            if (getExternalResources().isEmpty() == false) {
                LOG.debug("initializing external resources: {}", //$NON-NLS-1$
                        batchClass.getName());
                executor.prepareExternalResources(getExternalResources());
            }

            Date startDate = null;
            for (JobflowInfo jobflow : Util.sort(batch.getElements())) {
                JobflowMirror mirror = (JobflowMirror) jobflow;
                Util.prepare(driverContext, batch, jobflow);
                String flowId = jobflow.getId();
                JobFlowTester tester = jobflowMap.get(flowId);
                if (tester != null) {
                    LOG.debug("initializing jobflow input/output: {}#{}", //$NON-NLS-1$
                            batchClass.getName(), flowId);
                    executor.prepareInput(mirror, tester.inputs);
                    executor.prepareOutput(mirror, tester.outputs);

                    LOG.info(MessageFormat.format(
                            Messages.getString("BatchTester.infoExecute"), //$NON-NLS-1$
                            batchClass.getName(), flowId));
                    if (startDate == null) {
                        startDate = new Date();
                    }
                    VerifyContext verifyContext = new VerifyContext(driverContext, startDate);
                    executor.runJobflow(mirror);
                    verifyContext.testFinished();

                    LOG.info(MessageFormat.format(
                            Messages.getString("BatchTester.infoVerifyResult"), //$NON-NLS-1$
                            batchClass.getName(), flowId));
                    executor.verify(mirror, verifyContext, tester.outputs);
                }
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
        for (Map.Entry<String, JobFlowTester> entry : jobflowMap.entrySet()) {
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
