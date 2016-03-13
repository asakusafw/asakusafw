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
import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A tester for {@code FlowPart flow-part} classes.
 * @since 0.2.0
 * @version 0.8.0
 */
public class FlowPartTester extends TesterBase {

    static final Logger LOG = LoggerFactory.getLogger(FlowPartTester.class);

    private final CompilerToolkit toolkit;

    private final FlowPortMap portMap;

    private final List<FlowPartDriverInput<?>> inputs = new LinkedList<>();

    private final List<FlowPartDriverOutput<?>> outputs = new LinkedList<>();

    /**
     * Creates a new instance.
     * @param callerClass the caller class (usually it is a test class)
     */
    public FlowPartTester(Class<?> callerClass) {
        super(callerClass);
        this.toolkit = Util.getToolkit(callerClass);
        this.portMap = toolkit.newFlowPortMap();
    }

    /**
     * Starts configuring the target flow input.
     * The resulting object implements {@link In} interface. Application developers should keep the object after
     * configuring the target input, and drive it into the flow-part's constructor.
     * @param <T> the data model type
     * @param name a unique input name
     * @param modelType the data model type
     * @return object for configuring the target input
     */
    public <T> FlowPartDriverInput<T> input(String name, Class<T> modelType) {
        In<T> vocabulary = portMap.addInput(name, modelType);
        FlowPartDriverInput<T> input = new FlowPartDriverInput<>(driverContext, name, modelType, vocabulary);
        inputs.add(input);
        return input;
    }

    /**
     * Starts configuring the target flow output.
     * The resulting object implements {@link Out} interface. Application developers should keep the object after
     * configuring the target output, and drive it into the flow-part's constructor.
     * @param <T> the data model type
     * @param name a unique output name
     * @param modelType the data model type
     * @return object for configuring the target output
     */
    public <T> FlowPartDriverOutput<T> output(String name, Class<T> modelType) {
        Out<T> vocabulary = portMap.addOutput(name, modelType);
        FlowPartDriverOutput<T> output = new FlowPartDriverOutput<>(driverContext, name, modelType, vocabulary);
        outputs.add(output);
        return output;
    }

    /**
     * Executes a flow-part and then verifies the execution result.
     * @param description the target flow-part object
     * @throws IllegalStateException if error was occurred while building jobflow class or initializing this tester
     * @throws AssertionError if verification was failed
     */
    public void runTest(FlowDescription description) {
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

        CompilerConfiguration configuration = Util.getConfiguration(toolkit, driverContext);
        try (CompilerSession compiler = toolkit.newSession(configuration)) {
            ArtifactMirror artifact = compiler.compileFlow(flowDescription, portMap);
            Util.deploy(driverContext, artifact);

            BatchMirror batch = artifact.getBatch();
            JobflowMirror jobflow = Util.getJobflow(batch);

            driverContext.validateExecutionEnvironment();

            JobflowExecutor executor = new JobflowExecutor(driverContext);
            Util.prepare(driverContext, batch, jobflow);

            LOG.info(MessageFormat.format(
                    Messages.getString("FlowPartTester.infoInitializeEnvironment"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            executor.cleanWorkingDirectory();
            executor.cleanInputOutput(jobflow);
            executor.cleanExtraResources(getExternalResources());

            LOG.info(MessageFormat.format(
                    Messages.getString("FlowPartTester.infoPrepareData"), //$NON-NLS-1$
                    driverContext.getCallerClass().getName()));
            executor.prepareExternalResources(getExternalResources());
            executor.prepareInput(jobflow, inputs);
            executor.prepareOutput(jobflow, outputs);

            LOG.info(MessageFormat.format(
                    Messages.getString("FlowPartTester.infoExecute"), //$NON-NLS-1$
                    flowDescription.getClass().getName()));
            VerifyContext verifyContext = new VerifyContext(driverContext);
            executor.runJobflow(jobflow);
            verifyContext.testFinished();

            LOG.info(MessageFormat.format(
                    Messages.getString("FlowPartTester.infoVerifyResult"), //$NON-NLS-1$
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
