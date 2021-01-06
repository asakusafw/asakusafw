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
package com.asakusafw.testdriver.executor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicCommandTaskExecutor;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes {@link CommandTaskInfo} via fork/exec.
 * @since 0.10.0
 */
public class DefaultCommandTaskExecutor extends BasicCommandTaskExecutor {

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof CommandTaskInfo
                && isSupported(context, ((CommandTaskInfo) task).getCommand());
    }

    static boolean isSupported(TaskExecutionContext context, String command) {
        if (TaskExecutors.findFrameworkFile(context, command)
                .filter(Files::isExecutable)
                .isPresent() == false) {
            return false;
        }
        Map<String, String> env = context.getEnvironmentVariables();
        if (ConfigurationProvider.findHadoopCommand(env) == null
                && TaskExecutors.findHadoopEmbeddedLibraries(context).isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Checks if this executor can execute the given task, or raise an exception.
     * @param context the current context
     * @param task the target task
     */
    public static void checkSupported(TaskExecutionContext context, CommandTaskInfo task) {
        checkSupported(context, task.getCommand());
    }

    static void checkSupported(TaskExecutionContext context, String command) {
        Path home = TaskExecutors.findFrameworkHome(context)
                .filter(Files::exists)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "Asakusa Framework is not installed: ${0}",
                        TaskExecutors.ENV_FRAMEWORK_PATH)));
        Path commandFile = TaskExecutors.findFrameworkFile(context, command)
                .filter(Files::exists)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "command \"{1}\" is not found under the installation: {0}",
                        home,
                        command)));
        if (Files.isExecutable(commandFile) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "command \"{0}\" is not executable",
                    commandFile));
        }
        Map<String, String> env = context.getEnvironmentVariables();
        if (ConfigurationProvider.findHadoopCommand(env) == null
                && TaskExecutors.findHadoopEmbeddedLibraries(context).isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "\"hadoop\" command is not found to execute command \"{0}\"",
                    commandFile));
        }
    }
}
