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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.launcher.LauncherOptionsParser;
import com.asakusafw.runtime.stage.optimizer.LibraryCopySuppressionConfigurator;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.testdriver.TestExecutionPlan.Command;
import com.asakusafw.testdriver.TestExecutionPlan.Job;
import com.asakusafw.testdriver.TestExecutionPlan.Task;
import com.asakusafw.testdriver.compiler.CommandTaskMirror;
import com.asakusafw.testdriver.compiler.CommandToken;
import com.asakusafw.testdriver.compiler.CompilerConstants;
import com.asakusafw.testdriver.compiler.HadoopTaskMirror;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.compiler.PortMirror;
import com.asakusafw.testdriver.compiler.TaskMirror;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Prepares and executes jobflows.
 * Application developers must not use this class directly.
 * @since 0.2.0
 * @version 0.8.0
 */
class JobflowExecutor {

    static final Logger LOG = LoggerFactory.getLogger(JobflowExecutor.class);

    private final TestDriverContext context;

    private final TestModerator moderator;

    private final ConfigurationFactory configurations;

    private final JobExecutor jobExecutor;

    /**
     * Creates a new instance.
     * @param context submission context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JobflowExecutor(TestDriverContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.moderator = new TestModerator(context.getRepository(), context);
        this.jobExecutor = context.getJobExecutor();
        this.configurations = ConfigurationFactory.getDefault();
    }

    /**
     * Cleans up the working directory on the DFS.
     * @throws IOException if failed to clean up
     */
    public void cleanWorkingDirectory() throws IOException {
        Configuration conf = configurations.newInstance();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(CompilerConstants.getRuntimeWorkingDirectory());
        Path fullPath = fs.makeQualified(path);
        LOG.debug("start initializing working directory on the testing runtime: {}", fullPath); //$NON-NLS-1$
        boolean deleted = fs.delete(fullPath, true);
        if (deleted) {
            LOG.debug("finish initializing working directory on the testing runtime: {}", fullPath); //$NON-NLS-1$
        } else {
            LOG.debug("failed to initialize working directory on the testing runtime: {}", fullPath); //$NON-NLS-1$
        }
    }

    /**
     * Cleans up target jobflow's input/output.
     * @param flow target jobflow
     * @throws IOException if failed to clean up
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.0
     */
    public void cleanInputOutput(JobflowMirror flow) throws IOException {
        if (flow == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipCleanInput() == false) {
            for (PortMirror<? extends ImporterDescription> port : flow.getInputs()) {
                LOG.debug("cleaning input: {}", port.getName()); //$NON-NLS-1$
                moderator.truncate(port.getDescription());
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipInitializeInput")); //$NON-NLS-1$
        }

        if (context.isSkipCleanOutput() == false) {
            for (PortMirror<? extends ExporterDescription> port : flow.getOutputs()) {
                LOG.debug("cleaning output: {}", port.getName()); //$NON-NLS-1$
                moderator.truncate(port.getDescription());
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipInitializeOutput")); //$NON-NLS-1$
        }
    }

    /**
     * Cleans up extra resources.
     * @param resources the external resource map
     * @throws IOException if failed to create job processes
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.3
     */
    public void cleanExtraResources(
            Map<? extends ImporterDescription, ? extends DataModelSourceFactory> resources) throws IOException {
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipCleanInput() == false) {
            for (ImporterDescription description : resources.keySet()) {
                LOG.debug("cleaning external resource: {}", description); //$NON-NLS-1$
                moderator.truncate(description);
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipInitializeExtraResources")); //$NON-NLS-1$
        }
    }

    /**
     * Prepares the target jobflow's inputs.
     * @param jobflow target jobflow
     * @param inputs target inputs
     * @throws IOException if failed to create job processes
     * @throws IllegalStateException if input is not defined in the jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepareInput(
            JobflowMirror jobflow,
            Iterable<? extends DriverInputBase<?>> inputs) throws IOException {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobflow must not be null"); //$NON-NLS-1$
        }
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipPrepareInput() == false) {
            for (DriverInputBase<?> input : inputs) {
                DataModelSourceFactory source = input.getSource();
                if (source != null) {
                    String name = input.getName();
                    LOG.debug("preparing input: {} ({})", name, source); //$NON-NLS-1$
                    PortMirror<? extends ImporterDescription> port = jobflow.findInput(name);
                    if (port == null) {
                        throw new IllegalStateException(MessageFormat.format(
                                Messages.getString("JobflowExecutor.errorMissingInput"), //$NON-NLS-1$
                                name,
                                jobflow.getFlowId()));
                    }
                    moderator.prepare(port.getDataType(), port.getDescription(), source);
                }
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipPrepareInput")); //$NON-NLS-1$
        }
    }

    /**
     * Prepares the target jobflow's output.
     * @param jobflow target jobflow
     * @param outputs target outputs
     * @throws IOException if failed to create job processes
     * @throws IllegalStateException if output is not defined in the jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepareOutput(
            JobflowMirror jobflow,
            Iterable<? extends DriverOutputBase<?>> outputs) throws IOException {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobflow must not be null"); //$NON-NLS-1$
        }
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipPrepareOutput() == false) {
            for (DriverOutputBase<?> output : outputs) {
                DataModelSourceFactory source = output.getSource();
                if (source != null) {
                    String name = output.getName();
                    LOG.debug("preparing output: {} ({})", name, source); //$NON-NLS-1$
                    PortMirror<? extends ExporterDescription> port = jobflow.findOutput(name);
                    if (port == null) {
                        throw new IllegalStateException(MessageFormat.format(
                                Messages.getString("JobflowExecutor.errorMissingOutput"), //$NON-NLS-1$
                                name,
                                jobflow.getFlowId()));
                    }
                    moderator.prepare(port.getDataType(), port.getDescription(), source);
                }
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipPrepareOutput")); //$NON-NLS-1$
        }
    }

    /**
     * Prepares external resources.
     * @param resources the external resource map
     * @throws IOException if failed to prepare external resources
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.3
     */
    public void prepareExternalResources(
            Map<? extends ImporterDescription, ? extends DataModelSourceFactory> resources) throws IOException {
        if (resources == null) {
            throw new IllegalArgumentException("resources must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipPrepareInput() == false) {
            for (Map.Entry<? extends ImporterDescription, ? extends DataModelSourceFactory> entry
                    : resources.entrySet()) {
                ImporterDescription description = entry.getKey();
                DataModelSourceFactory source = entry.getValue();
                LOG.debug("preparing external resource: {} ({})", description, source); //$NON-NLS-1$
                moderator.prepare(description.getModelType(), description, source);
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipPrepareExtraResource")); //$NON-NLS-1$
        }
    }

    /**
     * Runs the target jobflow.
     * @param jobflow target jobflow
     * @throws IOException if failed to create job processes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void runJobflow(JobflowMirror jobflow) throws IOException {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobflow must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipRunJobflow() == false) {
            TestExecutionPlan plan = createExecutionPlan(jobflow);
            validatePlan(plan);
            executePlan(plan);
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipExecute")); //$NON-NLS-1$
        }
    }

    private void validatePlan(TestExecutionPlan plan) {
        jobExecutor.validatePlan(plan);
    }

    private void executePlan(TestExecutionPlan plan) throws IOException {
        assert plan != null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing plan: " //$NON-NLS-1$
                    + "home={}, batchId={}, flowId={}, execId={}, args={}, executor={}", new Object[] { //$NON-NLS-1$
                    context.getFrameworkHomePath(),
                    context.getCurrentBatchId(),
                    context.getCurrentFlowId(),
                    context.getCurrentExecutionId(),
                    context.getBatchArgs(),
                    jobExecutor.getClass().getName(),
            });
        }
        try {
            runJobflowTasks(plan.getInitializers());
            runJobflowTasks(plan.getImporters());
            runJobflowTasks(plan.getJobs());
            runJobflowTasks(plan.getExporters());
        } finally {
            runJobflowTasks(plan.getFinalizers());
        }
    }

    private void runJobflowTasks(List<? extends Task> tasks) throws IOException {
        for (TestExecutionPlan.Task task : tasks) {
            switch (task.getTaskKind()) {
            case COMMAND:
                jobExecutor.execute((TestExecutionPlan.Command) task, getEnvironmentVariables());
                break;
            case HADOOP:
                jobExecutor.execute((TestExecutionPlan.Job) task, getEnvironmentVariables());
                break;
            default:
                throw new AssertionError(task);
            }
        }
    }

    private Map<String, String> getHadoopProperties() {
        Map<String, String> results = new HashMap<>();
        results.put(StageConstants.PROP_USER, context.getOsUser());
        results.put(StageConstants.PROP_EXECUTION_ID, context.getExecutionId());
        results.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS, getBatchArgumentsToken());
        // disables libraries cache
        results.put(LauncherOptionsParser.KEY_CACHE_ENABLED, String.valueOf(false));
        // suppresses library copying only if is on local mode
        results.put(LibraryCopySuppressionConfigurator.KEY_ENABLED, String.valueOf(true));
        results.putAll(context.getExtraConfigurations());
        return results;
    }

    private Map<String, String> getEnvironmentVariables() {
        Map<String, String> variables = Maps.from(context.getEnvironmentVariables());
        return variables;
    }

    private TestExecutionPlan createExecutionPlan(JobflowMirror jobflow) {
        List<Task> initializers = createTasks(jobflow, TaskMirror.Phase.INITIALIZE);
        List<Task> importers = createTasks(jobflow, TaskMirror.Phase.IMPORT);
        List<Task> jobs = new ArrayList<>();
        jobs.addAll(createTasks(jobflow, TaskMirror.Phase.PROLOGUE));
        jobs.addAll(createTasks(jobflow, TaskMirror.Phase.MAIN));
        jobs.addAll(createTasks(jobflow, TaskMirror.Phase.EPILOGUE));
        List<Task> exporters = createTasks(jobflow, TaskMirror.Phase.EXPORT);
        List<Task> finalizers = createTasks(jobflow, TaskMirror.Phase.FINALIZE);
        return new TestExecutionPlan(
                jobflow.getFlowId(),
                context.getExecutionId(),
                initializers,
                importers,
                jobs,
                exporters,
                finalizers);
    }

    private List<Task> createTasks(JobflowMirror jobflow, TaskMirror.Phase phase) {
        List<Task> results = new ArrayList<>();
        for (TaskMirror task : Util.sort(jobflow.getTasks(phase))) {
            results.add(createTask(jobflow, task));
        }
        return results;
    }

    private Task createTask(JobflowMirror jobflow, TaskMirror task) {
        if (task instanceof CommandTaskMirror) {
            CommandTaskMirror t = (CommandTaskMirror) task;
            List<String> commandLine = new ArrayList<>();
            commandLine.add(new File(context.getFrameworkHomePath(), t.getCommand()).getAbsolutePath());
            commandLine.addAll(resolveCommandTokens(t.getArguments()));
            return new Command(
                    commandLine,
                    t.getModuleName(),
                    t.getProfileName(),
                    getEnvironmentVariables());
        } else if (task instanceof HadoopTaskMirror) {
            HadoopTaskMirror t = (HadoopTaskMirror) task;
            return new Job(
                    t.getClassName(),
                    getHadoopProperties());
        } else {
            throw new AssertionError(task);
        }
    }

    private List<String> resolveCommandTokens(List<CommandToken> tokens) {
        List<String> results = new ArrayList<>();
        for (CommandToken token : tokens) {
            results.add(resolveCommandToken(token));
        }
        return results;
    }

    private String resolveCommandToken(CommandToken token) {
        switch (token.getTokenKind()) {
        case TEXT:
            return token.getImage();
        case BATCH_ID:
            return context.getCurrentBatchId();
        case FLOW_ID:
            return context.getCurrentFlowId();
        case EXECUTION_ID:
            return context.getExecutionId();
        case BATCH_ARGUMENTS:
            return getBatchArgumentsToken();
        default:
            throw new AssertionError(token);
        }
    }

    private String getBatchArgumentsToken() {
        VariableTable t = new VariableTable();
        t.defineVariables(context.getBatchArgs());
        return t.toSerialString();
    }

    /**
    /**
     * Verifies the jobflow's results.
     * @param jobflow target jobflow
     * @param verifyContext verification context
     * @param outputs output information
     * @throws IOException if failed to verify
     * @throws IllegalStateException if output is not defined in the jobflow
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @throws AssertionError if actual output is different for the expected output
     */
    public void verify(
            JobflowMirror jobflow,
            VerifyContext verifyContext,
            Iterable<? extends DriverOutputBase<?>> outputs) throws IOException {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobflow must not be null"); //$NON-NLS-1$
        }
        if (verifyContext == null) {
            throw new IllegalArgumentException("verifyContext must not be null"); //$NON-NLS-1$
        }
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null"); //$NON-NLS-1$
        }
        if (context.isSkipVerify() == false) {
            StringBuilder sb = new StringBuilder();
            boolean sawError = false;
            for (DriverOutputBase<?> output : outputs) {
                String name = output.getName();
                PortMirror<? extends ExporterDescription> port = jobflow.findOutput(name);
                if (port == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            Messages.getString("JobflowExecutor.errorMissingOutput"), //$NON-NLS-1$
                            name,
                            jobflow.getFlowId()));
                }
                if (output.getResultSink() != null) {
                    LOG.debug("saving result output: {} ({})", output.getName(), output.getResultSink()); //$NON-NLS-1$
                    moderator.save(port.getDataType(), port.getDescription(), output.getResultSink());
                }
                if (output.getVerifier() != null) {
                    LOG.debug("verifying result output: {} ({})", name, output.getVerifier()); //$NON-NLS-1$
                    List<Difference> diffList = moderator.inspect(
                            port.getDataType(),
                            port.getDescription(),
                            verifyContext,
                            output.getVerifier());
                    if (diffList.isEmpty() == false) {
                        sawError = true;
                        String message = MessageFormat.format(
                                Messages.getString("JobflowExecutor.messageDifferenceSummary"), //$NON-NLS-1$
                                jobflow.getFlowId(),
                                output.getName(),
                                diffList.size());
                        sb.append(String.format("%s:%n", message)); //$NON-NLS-1$
                        LOG.warn(message);
                        if (output.getDifferenceSink() != null) {
                            LOG.debug("saving output differences: {} ({})",  //$NON-NLS-1$
                                    name, output.getDifferenceSink());
                            moderator.save(port.getDataType(), diffList, output.getDifferenceSink());
                        }
                        for (Difference difference : diffList) {
                            sb.append(String.format("%s: %s%n", //$NON-NLS-1$
                                    port.getDataType().getSimpleName(),
                                    difference));
                        }
                    }
                }
            }
            if (sawError) {
                throw new AssertionError(sb);
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipVerifyResult")); //$NON-NLS-1$
        }
    }
}
