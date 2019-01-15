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
package com.asakusafw.workflow.cli.run;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.workflow.executor.BatchExecutor;
import com.asakusafw.workflow.executor.CommandLauncher;
import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.executor.JobflowExecutor;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicBatchExecutor;
import com.asakusafw.workflow.executor.basic.BasicCommandLauncher;
import com.asakusafw.workflow.executor.basic.BasicCommandTaskExecutor;
import com.asakusafw.workflow.executor.basic.BasicJobflowExecutor;
import com.asakusafw.workflow.hadoop.BridgeDeleteTaskExecutor;
import com.asakusafw.workflow.hadoop.BridgeHadoopTaskExecutor;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Handles parameters about executors.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.workflow.cli.jcommander")
public class ExecutorParameter {

    /**
     * The output style of executor.
     */
    @Parameter(
            names = { "-O", "--output-style" },
            descriptionKey = "parameter.output-style",
            required = false
    )
    public BasicCommandLauncher.Output outputStyle = BasicCommandLauncher.Output.STANDARD;

    @Parameter(
            names = { "--no-parameter-check" },
            descriptionKey = "parameter.no-parameter-check",
            required = false
    )
    boolean skipParameterCheck = false;

    /**
     * Returns the task executors.
     * @param context the current context
     * @return the task executors
     */
    public List<TaskExecutor> getTaskExecutors(ExecutionContext context) {
        List<TaskExecutor> results = new ArrayList<>();
        results.addAll(TaskExecutors.loadDefaults(context.getClassLoader()));
        results.add(new BasicCommandTaskExecutor(this::getCommandLauncher));
        results.add(new BridgeHadoopTaskExecutor(this::getCommandLauncher));
        results.add(new BridgeDeleteTaskExecutor(this::getCommandLauncher));
        return results;
    }

    private CommandLauncher getCommandLauncher(TaskExecutionContext it) {
        return BasicCommandTaskExecutor.getCommandLauncher(it, getOutputStyle());
    }

    BasicCommandLauncher.OutputConsumer getOutputStyle() {
        if (outputStyle == BasicCommandLauncher.Output.LOGGING) {
            return new ExecutorLogger();
        } else {
            return outputStyle;
        }
    }

    /**
     * Returns the jobflow executor.
     * @param context the current context
     * @return the jobflow executor
     */
    public JobflowExecutor getJobflowExecutor(ExecutionContext context) {
        return new BasicJobflowExecutor(getTaskExecutors(context));
    }

    /**
     * Returns the batch executor.
     * @param context the current context
     * @return the batch executor
     */
    public BatchExecutor getBatchExecutor(ExecutionContext context) {
        return new BasicBatchExecutor(getJobflowExecutor(context))
                .withValidateParameters(skipParameterCheck == false);
    }
}
