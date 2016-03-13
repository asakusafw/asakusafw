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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
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
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;

/**
 * A tester for {@code JobFlow jobflow} classes.
 * @since 0.2.0
 * @version 0.8.0
 */
public class JobFlowTester extends TesterBase {

    static final Logger LOG = LoggerFactory.getLogger(JobFlowTester.class);

    private final CompilerToolkit toolkit;

    /**
     * The flow inputs.
     */
    protected final List<JobFlowDriverInput<?>> inputs = new LinkedList<>();

    /**
     * The flow outputs.
     */
    protected final List<JobFlowDriverOutput<?>> outputs = new LinkedList<>();

    /**
     * Creates a new instance.
     * @param callerClass the caller class (usually it is a test class)
     */
    public JobFlowTester(Class<?> callerClass) {
        super(callerClass);
        this.toolkit = Util.getToolkit(callerClass);
    }

    /**
     * Starts configuring the target flow input.
     * @param <T> the data model type
     * @param name the target input name (specified in {@link Import#name() &#64;Import(name=...)})
     * @param modelType the data model type
     * @return object for configuring the target input
     */
    public <T> JobFlowDriverInput<T> input(String name, Class<T> modelType) {
        JobFlowDriverInput<T> input = new JobFlowDriverInput<>(driverContext, name, modelType);
        inputs.add(input);
        return input;
    }

    /**
     * Starts configuring the target flow output.
     * @param <T> the data model type
     * @param name the target output name (specified in {@link Export#name() &#64;Export(name=...)})
     * @param modelType the data model type
     * @return object for configuring the target output
     */
    public <T> JobFlowDriverOutput<T> output(String name, Class<T> modelType) {
        JobFlowDriverOutput<T> output = new JobFlowDriverOutput<>(driverContext, name, modelType);
        outputs.add(output);
        return output;
    }

    /**
     * Executes a jobflow and then verifies the execution result.
     * @param description the target jobflow class
     * @throws IllegalStateException if error was occurred while building jobflow class or initializing this tester
     * @throws AssertionError if verification was failed
     */
    public void runTest(Class<? extends FlowDescription> description) {
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

    private void runTestInternal(Class<? extends FlowDescription> jobflowClass) throws IOException {
        LOG.info(MessageFormat.format(
                Messages.getString("JobFlowTester.infoStart"), //$NON-NLS-1$
                driverContext.getCallerClass().getName()));

        if (driverContext.isSkipValidateCondition() == false) {
            LOG.info(MessageFormat.format(
                    Messages.getString("JobFlowTester.infoVerifyCondition"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            validateTestCondition();
        }

        CompilerConfiguration configuration = Util.getConfiguration(toolkit, driverContext);
        try (CompilerSession compiler = toolkit.newSession(configuration)) {
            ArtifactMirror artifact = compiler.compileJobflow(jobflowClass);
            Util.deploy(driverContext, artifact);

            BatchMirror batch = artifact.getBatch();
            JobflowMirror jobflow = Util.getJobflow(batch);

            driverContext.validateExecutionEnvironment();

            JobflowExecutor executor = new JobflowExecutor(driverContext);
            Util.prepare(driverContext, batch, jobflow);

            LOG.info(MessageFormat.format(
                    Messages.getString("JobFlowTester.infoInitializeEnvironment"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            executor.cleanWorkingDirectory();
            executor.cleanInputOutput(jobflow);
            executor.cleanExtraResources(getExternalResources());

            LOG.info(MessageFormat.format(
                    Messages.getString("JobFlowTester.infoPrepareData"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            executor.prepareExternalResources(getExternalResources());
            executor.prepareInput(jobflow, inputs);
            executor.prepareOutput(jobflow, outputs);

            LOG.info(MessageFormat.format(
                    Messages.getString("JobFlowTester.infoExecute"), //$NON-NLS-1$
                    jobflowClass.getName()));
            VerifyContext verifyContext = new VerifyContext(driverContext);
            executor.runJobflow(jobflow);
            verifyContext.testFinished();

            LOG.info(MessageFormat.format(
                    Messages.getString("JobFlowTester.infoVerifyResult"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            executor.verify(jobflow, verifyContext, outputs);
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
