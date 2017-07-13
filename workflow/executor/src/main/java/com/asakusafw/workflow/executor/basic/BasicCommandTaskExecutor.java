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
package com.asakusafw.workflow.executor.basic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.asakusafw.workflow.executor.CommandLauncher;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes {@link CommandTaskInfo} via fork/exec.
 * @since 0.10.0
 */
public class BasicCommandTaskExecutor implements TaskExecutor {

    private final Function<? super TaskExecutionContext, ? extends CommandLauncher> launchers;

    /**
     * Creates a new instance.
     */
    public BasicCommandTaskExecutor() {
        this(BasicCommandTaskExecutor::getCommandLauncher);
    }

    /**
     * Creates a new instance.
     * @param launchers the command launcher provider
     */
    public BasicCommandTaskExecutor(Function<? super TaskExecutionContext, ? extends CommandLauncher> launchers) {
        Objects.requireNonNull(launchers);
        this.launchers = launchers;
    }

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof CommandTaskInfo
                && isSupported(context, ((CommandTaskInfo) task).getCommand());
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws InterruptedException, IOException {
        CommandTaskInfo mirror = (CommandTaskInfo) task;
        CommandLauncher launcher = launchers.apply(context);
        Path command = TaskExecutors.findFrameworkFile(context, mirror.getCommand()).get();
        List<String> arguments = TaskExecutors.resolveCommandTokens(
                context,
                mirror.getArguments(context.getConfigurations()));
        execute(launcher, command, arguments);
    }

    static boolean isSupported(TaskExecutionContext context, String command) {
        if (TaskExecutors.findFrameworkFile(context, command)
                .filter(Files::isExecutable)
                .isPresent() == false) {
            return false;
        }
        return true;
    }

    /**
     * Returns a {@link CommandLauncher}.
     * @param context the current context
     * @return the command launcher
     */
    public static CommandLauncher getCommandLauncher(TaskExecutionContext context) {
        return new BasicCommandLauncher(
                TaskExecutors.getUserHome(context),
                context.getEnvironmentVariables());
    }

    /**
     * Executes a command.
     * @param context the current context
     * @param command the command path
     * @param arguments the resolved arguments
     * @throws IOException if I/O error was occurred while executing the command
     * @throws InterruptedException if interrupted while executing the command
     */
    public static void execute(
            TaskExecutionContext context,
            Path command, List<String> arguments) throws IOException, InterruptedException {
        execute(getCommandLauncher(context), command, arguments);
    }

    /**
     * Executes a command.
     * @param launcher the command launcher
     * @param command the command path
     * @param arguments the resolved arguments
     * @throws IOException if I/O error was occurred while executing the command
     * @throws InterruptedException if interrupted while executing the command
     */
    public static void execute(
            CommandLauncher launcher,
            Path command, List<String> arguments) throws IOException, InterruptedException {
        int exit = launcher.launch(command, arguments);
        if (exit != 0) {
            throw new IOException(MessageFormat.format(
                    "failed to execute task: command={0}, arguments={1}, exit={2}",
                    command,
                    arguments,
                    exit));
        }
    }
}
