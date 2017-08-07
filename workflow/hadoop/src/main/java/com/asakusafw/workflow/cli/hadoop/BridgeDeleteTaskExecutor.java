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
package com.asakusafw.workflow.cli.hadoop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.CommandLauncher;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicCommandTaskExecutor;
import com.asakusafw.workflow.executor.basic.BasicDeleteTaskExecutor;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes delete tasks via {@code tools/bin/libexec/workflow/hadoop-bridge.sh}.
 * @since 0.10.0
 */
public class BridgeDeleteTaskExecutor extends BasicDeleteTaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(BridgeDeleteTaskExecutor.class);

    /**
     * The target class name.
     */
    public static final String DELEGATE_CLASS = HadoopDelete.class.getName();

    private final Function<? super TaskExecutionContext, ? extends CommandLauncher> launchers;

    /**
     * Creates a new instance.
     */
    public BridgeDeleteTaskExecutor() {
        this(BasicCommandTaskExecutor::getCommandLauncher);
    }

    /**
     * Creates a new instance.
     * @param launchers the launcher provider
     */
    public BridgeDeleteTaskExecutor(Function<? super TaskExecutionContext, ? extends CommandLauncher> launchers) {
        this.launchers = launchers;
    }

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return super.isSupported(context, task) && getBridgeScript(context).isPresent();
    }

    @Override
    protected void deleteOnHadoopFileSystem(
            TaskExecutionContext context, String path) throws IOException, InterruptedException {
        CommandLauncher launcher = launchers.apply(context);
        Path command = getBridgeScript(context).orElseThrow(IllegalStateException::new);
        List<String> arguments = Arrays.asList(
                TaskExecutors.findFrameworkFile(context, Constants.PATH_BRIDGE_LIBRARY)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .get(),
                DELEGATE_CLASS,
                path);
        BasicCommandTaskExecutor.execute(launcher, command, arguments);
    }

    private static Optional<Path> getBridgeScript(TaskExecutionContext context) {
        return TaskExecutors.findFrameworkFile(context, Constants.PATH_BRIDGE_SCRIPT)
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable);
    }
}
