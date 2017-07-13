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
package com.asakusafw.testdriver.tools.runner;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.AbstractCleanupStageClient;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.compiler.basic.BasicJobflowMirror;
import com.asakusafw.testdriver.executor.DefaultCommandTaskExecutor;
import com.asakusafw.testdriver.executor.DefaultDeleteTaskExecutor;
import com.asakusafw.testdriver.executor.DefaultHadoopTaskExecutor;
import com.asakusafw.testdriver.executor.TaskExecutorContextAdapter;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.workflow.executor.JobflowExecutor;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicJobflowExecutor;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.TaskInfo;
import com.asakusafw.workflow.model.basic.BasicCommandTaskInfo;
import com.asakusafw.workflow.model.basic.BasicHadoopTaskInfo;
import com.asakusafw.workflow.model.basic.BasicTaskInfo;
import com.asakusafw.yaess.core.BatchScript;
import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.FlowScript;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Runs an Asakusa batch application using test driver facilities.
 * @since 0.6.0
 * @version 0.10.0
 */
public class RunTask {

    static final Logger LOG = LoggerFactory.getLogger(RunTask.class);

    private final Configuration configuration;

    private final ExecutionScriptHandler<?> handler = new ExecutionScriptHandler<ExecutionScript>() {
        @Override
        public void configure(ServiceProfile<?> profile) {
            return;
        }
        @Override
        public String getHandlerId() {
            return "testing"; //$NON-NLS-1$
        }
        @Override
        public String getResourceId(ExecutionContext context, ExecutionScript script) {
            return "testing"; //$NON-NLS-1$
        }
        @Override
        public Map<String, String> getProperties(ExecutionContext context, ExecutionScript script) {
            return Collections.emptyMap();
        }
        @Override
        public Map<String, String> getEnvironmentVariables(ExecutionContext context, ExecutionScript script) {
            return context.getEnvironmentVariables();
        }
        @Override
        public void setUp(ExecutionMonitor monitor, ExecutionContext context) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void execute(ExecutionMonitor monitor, ExecutionContext context, ExecutionScript script) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void cleanUp(ExecutionMonitor monitor, ExecutionContext context) {
            throw new UnsupportedOperationException();
        }
    };

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
        for (FlowScript flow : configuration.script.getAllFlows()) {
            context.setCurrentBatchId(configuration.script.getId());
            context.setCurrentFlowId(flow.getId());
            context.setCurrentExecutionId(getExecutionId(flow.getId()));

            ConfigurationFactory configurations = ConfigurationFactory.getDefault();

            JobflowMirror mirror = toMirror(flow);
            List<TaskExecutor> taskExecutors = new ArrayList<>();
            taskExecutors.addAll(TaskExecutors.loadDefaults(context.getClassLoader()));
            taskExecutors.add(new DefaultCommandTaskExecutor());
            taskExecutors.add(new DefaultHadoopTaskExecutor());
            taskExecutors.add(new DefaultDeleteTaskExecutor());
            JobflowExecutor executor = new BasicJobflowExecutor(taskExecutors);
            TaskExecutionContext execContext = new TaskExecutorContextAdapter(context, configurations);
            try {
                executor.execute(execContext, mirror);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            if (configuration.cleanUp == false || hasCleanUp(execContext) == false) {
                return;
            }
        }
    }

    private static boolean hasCleanUp(TaskExecutionContext context) throws IOException {
        Path library = TaskExecutors.findJobflowLibrary(context)
                .filter(Files::isRegularFile)
                .orElse(null);
        if (library == null) {
            return false;
        }
        try (FileSystem fs = FileSystems.newFileSystem(library, context.getClassLoader())) {
            for (Path root : fs.getRootDirectories()) {
                Path entry = root.resolve(AbstractCleanupStageClient.IMPLEMENTATION.replace('.', '/') + ".class");
                if (Files.exists(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JobflowMirror toMirror(FlowScript flow) throws IOException {
        BasicJobflowMirror mirror = new BasicJobflowMirror(flow.getId());
        for (TaskInfo.Phase phase : TaskInfo.Phase.values()) {
            processPhase(mirror, flow, phase);
        }
        if (flow.getEnabledScriptKinds().contains(ExecutionScript.Kind.HADOOP)) {
            mirror.addTask(TaskInfo.Phase.CLEANUP,
                    new BasicHadoopTaskInfo(AbstractCleanupStageClient.IMPLEMENTATION));
        }
        return mirror;
    }

    private void processPhase(BasicJobflowMirror mirror, FlowScript flow, TaskInfo.Phase phase) throws IOException {
        ExecutionContext context = createExecutionContext(mirror.getId(), ExecutionPhase.valueOf(phase.name()));
        Set<ExecutionScript> scripts = flow.getScripts().get(context.getPhase());
        Map<String, ExecutionScript> ids = scripts.stream()
                .collect(Collectors.toMap(ExecutionScript::getId, Function.identity()));
        Graph<ExecutionScript> deps = Graphs.newInstance();
        scripts.forEach(script -> {
            deps.addNode(script);
            script.getBlockerIds().stream()
                .map(ids::get)
                .filter(it -> it != null)
                .forEach(blocker -> deps.addEdge(script, blocker));
        });
        Map<ExecutionScript, TaskInfo> resolved = new HashMap<>();
        for (ExecutionScript script : Graphs.sortPostOrder(deps)) {
            BasicTaskInfo task = toMirror(context, script);
            deps.getConnected(script).stream()
                .map(resolved::get)
                .forEach(it -> task.addBlocker(it));
            mirror.addTask(phase, task);
            resolved.put(script, task);
        }
    }

    private BasicTaskInfo toMirror(ExecutionContext context, ExecutionScript script) throws IOException {
        if (script.getKind() == ExecutionScript.Kind.COMMAND) {
            CommandScript resolved = (CommandScript) resolveScript(script, context);
            return new BasicCommandTaskInfo(
                    resolved.getModuleName(),
                    resolved.getProfileName(),
                    getRelativeCommandPath((CommandScript) script),
                    resolved.getCommandLineTokens().subList(1, resolved.getCommandLineTokens().size()).stream()
                        .map(CommandToken::of)
                        .collect(Collectors.toList()));
        } else if (script.getKind() == ExecutionScript.Kind.HADOOP) {
            HadoopScript resolved = (HadoopScript) resolveScript(script, context);
            return new BasicHadoopTaskInfo(resolved.getClassName());
        } else {
            throw new AssertionError(script);
        }
    }

    private static String getRelativeCommandPath(CommandScript script) {
        int offset = 0;
        String location = script.getCommandLineTokens().get(0);
        if (location.startsWith(ExecutionScript.PLACEHOLDER_HOME)) {
            offset += ExecutionScript.PLACEHOLDER_HOME.length();
        } else {
            throw new IllegalStateException(location);
        }
        while (offset < location.length() && location.charAt(offset) == '/') {
            offset++;
        }
        return location.substring(offset);
    }

    private ExecutionScript resolveScript(ExecutionScript script, ExecutionContext context) throws IOException {
        try {
            return script.resolve(context, handler);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("RunTask.errorFailedToResolveJob"), //$NON-NLS-1$
                    context,
                    script.getId()), e);
        }
    }

    private ExecutionContext createExecutionContext(String flowId, ExecutionPhase phase) {
        return new ExecutionContext(
                configuration.script.getId(),
                flowId,
                getExecutionId(flowId),
                phase,
                configuration.context.getBatchArgs(),
                configuration.context.getEnvironmentVariables());
    }

    private String getExecutionId(String flowId) {
        return String.format("%s-%s-%s", //$NON-NLS-1$
                configuration.executionIdPrefix,
                configuration.script.getId(),
                flowId);
    }

    /**
     * Represents a configuration for {@link RunTask}.
     * @since 0.6.0
     * @version 0.7.1
     */
    public static final class Configuration {

        final TestDriverContext context;

        final BatchScript script;

        final String executionIdPrefix;

        boolean cleanUp = true;

        /**
         * Creates a new instance.
         * @param context the current test driver context
         * @param script the target execution script
         * @param executionIdPrefix the execution ID prefix for each jobflow execution
         */
        public Configuration(TestDriverContext context, BatchScript script, String executionIdPrefix) {
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
