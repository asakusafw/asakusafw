/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.compiler.CompilerConstants;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.compiler.PortMirror;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.executor.DefaultCommandTaskExecutor;
import com.asakusafw.testdriver.executor.DefaultDeleteTaskExecutor;
import com.asakusafw.testdriver.executor.DefaultHadoopTaskExecutor;
import com.asakusafw.testdriver.executor.TaskExecutorContextAdapter;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicJobflowExecutor;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Prepares and executes jobflows.
 * Application developers must not use this class directly.
 * @since 0.2.0
 * @version 0.10.0
 */
class JobflowExecutor {

    static final Logger LOG = LoggerFactory.getLogger(JobflowExecutor.class);

    private final TestDriverContext driverContext;

    private final TestModerator moderator;

    private final ConfigurationFactory configurations;

    private final List<TaskExecutor> taskExecutors;

    /**
     * Creates a new instance.
     * @param context submission context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    JobflowExecutor(TestDriverContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.driverContext = context;
        this.moderator = new TestModerator(context.getRepository(), context);
        this.taskExecutors = new ArrayList<>();
        taskExecutors.addAll(TaskExecutors.loadDefaults(context.getClassLoader()));
        taskExecutors.add(new DefaultHadoopTaskExecutor());
        taskExecutors.add(new DefaultCommandTaskExecutor());
        taskExecutors.add(new DefaultDeleteTaskExecutor());
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
        if (driverContext.isSkipCleanInput() == false) {
            for (PortMirror<? extends ImporterDescription> port : flow.getInputs()) {
                LOG.debug("cleaning input: {}", port.getName()); //$NON-NLS-1$
                moderator.truncate(port.getDescription());
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipInitializeInput")); //$NON-NLS-1$
        }

        if (driverContext.isSkipCleanOutput() == false) {
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
        if (driverContext.isSkipCleanInput() == false) {
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
        if (driverContext.isSkipPrepareInput() == false) {
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
                                jobflow.getId()));
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
        if (driverContext.isSkipPrepareOutput() == false) {
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
                                jobflow.getId()));
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
        if (driverContext.isSkipPrepareInput() == false) {
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
     * Checks if the given jobflow is valid.
     * @param jobflow the target jobflow
     */
    public void validateJobflow(JobflowMirror jobflow) {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobflow must not be null"); //$NON-NLS-1$
        }
        TaskExecutionContext context = new TaskExecutorContextAdapter(driverContext, configurations);
        List<? extends TaskInfo> tasks = Arrays.stream(TaskInfo.Phase.values())
            .flatMap(it -> jobflow.getTasks(it).stream())
            .filter(task -> findExecutor(context, task).isPresent() == false)
            .collect(Collectors.toList());
        for (TaskInfo task : tasks) {
            if (task instanceof CommandTaskInfo) {
                DefaultCommandTaskExecutor.checkSupported(context, (CommandTaskInfo) task);
            } else if (task instanceof HadoopTaskInfo) {
                DefaultHadoopTaskExecutor.checkSupported(context, (HadoopTaskInfo) task);
            } else {
                throw new IllegalStateException(MessageFormat.format(
                        "unsupported task type: {0}",
                        task.getClass().getSimpleName()));
            }
        }
    }

    private Optional<TaskExecutor> findExecutor(TaskExecutionContext context, TaskInfo task) {
        return taskExecutors.stream().filter(it -> it.isSupported(context, task)).findFirst();
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
        if (driverContext.isSkipRunJobflow() == false) {
            TaskExecutionContext context = new TaskExecutorContextAdapter(driverContext, configurations);
            try {
                new BasicJobflowExecutor(taskExecutors).execute(context, jobflow);
            } catch (InterruptedException e) {
                throw new IOException("interrupted while running jobflow", e);
            }
        } else {
            LOG.info(Messages.getString("JobflowExecutor.infoSkipExecute")); //$NON-NLS-1$
        }
    }

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
        if (driverContext.isSkipVerify() == false) {
            StringBuilder sb = new StringBuilder();
            boolean sawError = false;
            for (DriverOutputBase<?> output : outputs) {
                String name = output.getName();
                PortMirror<? extends ExporterDescription> port = jobflow.findOutput(name);
                if (port == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            Messages.getString("JobflowExecutor.errorMissingOutput"), //$NON-NLS-1$
                            name,
                            jobflow.getId()));
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
                                jobflow.getId(),
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
