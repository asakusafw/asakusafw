/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.executor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.launcher.LauncherOptionsParser;
import com.asakusafw.runtime.stage.optimizer.LibraryCopySuppressionConfigurator;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicCommandTaskExecutor;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes {@link HadoopTaskInfo} via fork/exec.
 * @since 0.10.0
 */
public class DefaultHadoopTaskExecutor implements TaskExecutor {

    static final String PATH_HADOOP_SCRIPT = "testing/libexec/hadoop-execute.sh"; //$NON-NLS-1$

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof HadoopTaskInfo
                && DefaultCommandTaskExecutor.isSupported(context, PATH_HADOOP_SCRIPT);
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws IOException, InterruptedException {
        HadoopTaskInfo mirror = (HadoopTaskInfo) task;
        Path command = TaskExecutors.findFrameworkFile(context, PATH_HADOOP_SCRIPT).get();
        List<String> arguments = new ArrayList<>();
        arguments.addAll(TaskExecutors.resolveCommandTokens(context, Arrays.asList(
                CommandToken.of(mirror.getClassName()),
                CommandToken.BATCH_ID,
                CommandToken.FLOW_ID)));
        arguments.addAll(getHadoopArguments(context));
        BasicCommandTaskExecutor.execute(context, command, arguments);
    }

    private static List<String> getHadoopArguments(TaskExecutionContext context) {
        Map<String, String> properties = getHadoopProperties(context);
        List<String> results = new ArrayList<>();
        properties.forEach((k, v) -> {
            results.add("-D");
            results.add(String.format("%s=%s", k, v));
        });
        return results;
    }

    static Map<String, String> getHadoopProperties(TaskExecutionContext context) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(StageConstants.PROP_USER, TaskExecutors.getUserName(context));
        properties.put(StageConstants.PROP_EXECUTION_ID, context.getExecutionId());
        properties.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS,
                TaskExecutors.resolveCommandToken(context, CommandToken.BATCH_ARGUMENTS));
        properties.put(LauncherOptionsParser.KEY_CACHE_ENABLED, String.valueOf(false));
        properties.put(LibraryCopySuppressionConfigurator.KEY_ENABLED, String.valueOf(true));
        properties.putAll(context.getConfigurations());
        return properties;
    }

    /**
     * Checks if this executor can execute the given task, or raise an exception.
     * @param context the current context
     * @param task the target task
     */
    public static void checkSupported(TaskExecutionContext context, HadoopTaskInfo task) {
        DefaultCommandTaskExecutor.checkSupported(context, PATH_HADOOP_SCRIPT);
    }
}
