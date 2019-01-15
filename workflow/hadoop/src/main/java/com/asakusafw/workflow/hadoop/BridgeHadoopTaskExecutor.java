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
package com.asakusafw.workflow.hadoop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.workflow.executor.CommandLauncher;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicCommandTaskExecutor;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes hadoop tasks via {@code tools/bin/libexec/workflow/hadoop-bridge}.
 * @since 0.10.0
 */
public class BridgeHadoopTaskExecutor implements TaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(BridgeDeleteTaskExecutor.class);

    /**
     * The Asakusa application launcher class name.
     */
    public static final String LAUNCHER_CLASS = "com.asakusafw.runtime.stage.launcher.ApplicationLauncher";

    private final Function<? super TaskExecutionContext, ? extends CommandLauncher> launchers;

    /**
     * Creates a new instance.
     */
    public BridgeHadoopTaskExecutor() {
        this(BasicCommandTaskExecutor::getCommandLauncher);
    }

    /**
     * Creates a new instance.
     * @param launchers the launcher provider
     */
    public BridgeHadoopTaskExecutor(Function<? super TaskExecutionContext, ? extends CommandLauncher> launchers) {
        this.launchers = launchers;
    }

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof HadoopTaskInfo && getBridgeScript(context).isPresent();
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws IOException, InterruptedException {
        HadoopTaskInfo mirror = (HadoopTaskInfo) task;
        CommandLauncher launcher = launchers.apply(context);
        Path command = TaskExecutors.findFrameworkFile(context, Constants.PATH_BRIDGE_SCRIPT).get();
        List<String> arguments = new ArrayList<>();

        arguments.add(TaskExecutors.findFrameworkFile(context, Constants.PATH_LAUNCHER_LIBRARY)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .get());

        arguments.add(LAUNCHER_CLASS);

        arguments.add(mirror.getClassName());

        TaskExecutors.findCoreConfigurationFile(context)
                .filter(Files::isRegularFile)
                .map(Path::toAbsolutePath)
                .ifPresent(f -> Collections.addAll(arguments, "-conf", f.toString()));

        Collections.addAll(arguments, "-libjars", getLibraries(context).stream()
                .filter(Files::isRegularFile)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining(",")));

        arguments.addAll(getHadoopArguments(context));
        BasicCommandTaskExecutor.execute(context, launcher, command, arguments);
    }

    private static List<Path> getLibraries(TaskExecutionContext context) {
        List<Path> results = new ArrayList<>();
        TaskExecutors.findJobflowLibrary(context).ifPresent(results::add);
        TaskExecutors.findAttachedLibraries(context).forEach(results::add);
        TaskExecutors.findExtensionLibraries(context).forEach(results::add);
        TaskExecutors.findCoreLibraries(context).forEach(results::add);
        return results;
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
        properties.put(StageConstants.PROP_BATCH_ID, context.getBatchId());
        properties.put(StageConstants.PROP_FLOW_ID, context.getFlowId());
        properties.put(StageConstants.PROP_EXECUTION_ID, context.getExecutionId());
        properties.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS,
                TaskExecutors.resolveCommandToken(context, CommandToken.BATCH_ARGUMENTS));
        properties.putAll(context.getConfigurations());
        return properties;
    }

    private static Optional<Path> getBridgeScript(TaskExecutionContext context) {
        return TaskExecutors.findFrameworkFile(context, Constants.PATH_BRIDGE_SCRIPT)
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable);
    }
}
