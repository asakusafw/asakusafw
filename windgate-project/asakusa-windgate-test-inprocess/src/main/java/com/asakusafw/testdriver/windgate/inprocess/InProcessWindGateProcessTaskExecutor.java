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
package com.asakusafw.testdriver.windgate.inprocess;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.windgate.cli.CommandLineUtil;
import com.asakusafw.windgate.cli.ExecutionKind;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.GateTask;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.CommandTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * In-process {@link TaskExecutor} for {@code windgate/bin/process}.
 * @since 0.10.0
 */
public class InProcessWindGateProcessTaskExecutor implements TaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(InProcessWindGateProcessTaskExecutor.class);

    static final String COMMAND_SUFFIX = WindGateTaskExecutors.PATH_WINDGATE + "/bin/process"; //$NON-NLS-1$

    static final int ARG_SESSION_KIND = WindGateTaskExecutors.ARG_PROFILE + 1;

    static final int ARG_SCRIPT = ARG_SESSION_KIND + 1;

    static final int ARG_BATCH_ID = ARG_SCRIPT + 1;

    static final int ARG_FLOW_ID = ARG_BATCH_ID + 1;

    static final int ARG_EXECUTION_ID = ARG_FLOW_ID + 1;

    static final int ARG_ARGUMENTS = ARG_EXECUTION_ID + 1;

    static final int MINIMUM_TOKENS = ARG_EXECUTION_ID + 1;

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return WindGateTaskExecutors.isSupported(context, task, COMMAND_SUFFIX, MINIMUM_TOKENS);
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws InterruptedException, IOException {
        CommandTaskInfo mirror = (CommandTaskInfo) task;
        WindGateTaskExecutors.withLibraries(context, classLoadeer -> {
            GateProfile profile = WindGateTaskExecutors.loadProfile(context, classLoadeer, mirror);
            try (GateTask windgate = createTask(context, profile, classLoadeer, mirror)) {
                windgate.execute();
            }
        });
    }

    private static GateTask createTask(
            TaskExecutionContext context, GateProfile profile,
            ClassLoader classLoader, CommandTaskInfo command) throws IOException {
        List<String> commandArgs = TaskExecutors.resolveCommandTokens(context, command.getArguments());
        GateScript script = loadScript(classLoader, commandArgs.get(ARG_SCRIPT));
        String sessionId = context.getExecutionId();
        ExecutionKind mode = ExecutionKind.parse(commandArgs.get(ARG_SESSION_KIND));

        Map<String, String> arguments = new LinkedHashMap<>();
        arguments.putAll(CommandLineUtil.parseArguments(commandArgs.get(ARG_ARGUMENTS)).getPairs());
        arguments.put(StageConstants.VAR_USER, TaskExecutors.getUserName(context));
        arguments.put(StageConstants.VAR_BATCH_ID, commandArgs.get(ARG_BATCH_ID));
        arguments.put(StageConstants.VAR_FLOW_ID, commandArgs.get(ARG_FLOW_ID));
        arguments.put(StageConstants.VAR_EXECUTION_ID, commandArgs.get(ARG_EXECUTION_ID));

        return new GateTask(
                profile,
                script,
                sessionId,
                mode.createsSession,
                mode.completesSession,
                new ParameterList(arguments));
    }

    private static GateScript loadScript(ClassLoader classLoader, String script) {
        LOG.debug("loading script: {}", script); //$NON-NLS-1$
        try {
            URI uri = CommandLineUtil.toUri(script);
            Properties properties = CommandLineUtil.loadProperties(uri, classLoader);
            return GateScript.loadFrom(CommandLineUtil.toName(uri), properties, classLoader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "failed to load WindGate script: {0}",
                    script), e);
        }
    }
}
