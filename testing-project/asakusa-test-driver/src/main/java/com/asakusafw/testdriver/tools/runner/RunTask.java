/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.tools.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.executor.DefaultCommandTaskExecutor;
import com.asakusafw.testdriver.executor.DefaultDeleteTaskExecutor;
import com.asakusafw.testdriver.executor.DefaultHadoopTaskExecutor;
import com.asakusafw.testdriver.executor.TaskExecutorContextAdapter;
import com.asakusafw.testdriver.executor.VoidDeleteTaskExecutor;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.workflow.executor.BatchExecutor;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicBatchExecutor;
import com.asakusafw.workflow.executor.basic.BasicJobflowExecutor;
import com.asakusafw.workflow.model.BatchInfo;
import com.asakusafw.workflow.model.JobflowInfo;

/**
 * Runs an Asakusa batch application using test driver facilities.
 * @since 0.6.0
 * @version 0.10.0
 */
public class RunTask {

    static final Logger LOG = LoggerFactory.getLogger(RunTask.class);

    private final Configuration configuration;

    /**
     * Creates a new instance.
     * @param configuration the task configuration
     */
    public RunTask(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Executes an Asakusa batch application.
     * @throws IOException if failed to prepare execution plan
     * @throws AssertionError if failed to execute each job
     */
    public void perform() throws IOException {
        TestDriverContext context = configuration.context;

        List<TaskExecutor> taskExecutors = new ArrayList<>();
        taskExecutors.addAll(TaskExecutors.loadDefaults(context.getClassLoader()));
        taskExecutors.add(new DefaultCommandTaskExecutor());
        taskExecutors.add(new DefaultHadoopTaskExecutor());
        taskExecutors.add(configuration.cleanUp ? new DefaultDeleteTaskExecutor() : new VoidDeleteTaskExecutor());
        BatchExecutor executor = new BasicBatchExecutor(new BasicJobflowExecutor(taskExecutors), this::getExecutionId);

        ConfigurationFactory configurations = ConfigurationFactory.getDefault();
        TaskExecutionContext execContext = new TaskExecutorContextAdapter(context, configurations);
        try {
            executor.execute(execContext, configuration.script, context.getArguments());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private String getExecutionId(JobflowInfo flow) {
        return String.format("%s-%s-%s", //$NON-NLS-1$
                configuration.executionIdPrefix,
                configuration.script.getId(),
                flow.getId());
    }

    /**
     * Represents a configuration for {@link RunTask}.
     * @since 0.6.0
     * @version 0.10.0
     */
    public static final class Configuration {

        final TestDriverContext context;

        final BatchInfo script;

        final String executionIdPrefix;

        boolean cleanUp = true;

        /**
         * Creates a new instance.
         * @param context the current test driver context
         * @param script the target execution script
         * @param executionIdPrefix the execution ID prefix for each jobflow execution
         */
        public Configuration(TestDriverContext context, BatchInfo script, String executionIdPrefix) {
            this.context = context;
            this.script = script;
            this.executionIdPrefix = executionIdPrefix;
        }

        /**
         * Sets whether jobflow clean up is enabled or not.
         * @param enable {@code true} if it is enabled
         * @return this
         * @since 0.7.1
         */
        public Configuration withCleanUp(boolean enable) {
            this.cleanUp = enable;
            return this;
        }
    }
}
