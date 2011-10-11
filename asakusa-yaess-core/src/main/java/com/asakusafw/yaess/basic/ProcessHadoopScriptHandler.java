/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandlerBase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;

/**
 * An abstract implementations of process-based {@link HadoopScriptHandler}.
 * This handler just launches a command with following arguments in its tail.
 * <ol>
 * <li> {@link HadoopScript#getClassName() class name} </li>
 * <li> {@link ExecutionContext#getBatchId() batch-id} </li>
 * <li> {@link ExecutionContext#getFlowId() flow-id} </li>
 * <li> {@link ExecutionContext#getExecutionId() execution-id} </li>
 * <li> {@link ExecutionContext#getArgumentsAsString() batch-arguments} </li>
 * <li> {@link HadoopScript#getHadoopProperties() hadoop properties (with "-D")} </li>
 * </ol>
 * Additionally, the handler lanuches a command if {@code hadoop.workingDirectory} is defined.
 * <ol>
 * <li> Specified in {@code hadoop.workingDirectory} </li>
 * <li> {@link ExecutionContext#getBatchId() batch-id} </li>
 * <li> {@link ExecutionContext#getFlowId() flow-id} </li>
 * <li> {@link ExecutionContext#getExecutionId() execution-id} </li>
 * <li> {@link ExecutionContext#getArgumentsAsString() batch-arguments} </li>
 * <li> {@link HadoopScript#getHadoopProperties() hadoop properties (with "-D")} </li>
 * </ol>
 * You must specify a executable file for above arguments,
 * by setting {@code hadoop.command.0 = <executable-file>} in the profile set.
 *
 * <h3> Profile format </h3>
<pre><code>
# &lt;position&gt; = 0, 1, 2, ...
# &lt;prefix command token&gt; can contain "&#64;[position],"
# this will be replaced as original command tokens (0-origin position)
hadoop = &lt;this class name&gt;
hadoop.env.ASAKUSA_HOME = ${ASAKUSA_HOME}
hadoop.command.0 = ${ASAKUSA_HOME}/yaess-basic/bin/hadoop-command.sh
hadoop.command.&lt;position&gt; = $&lt;prefix command token&gt;
hadoop.cleanup.0 = ${ASAKUSA_HOME}/yaess-basic/bin/hadoop-cleanup.sh
hadoop.workingDirectory = $<cluster working directory>
hadoop.env.&lt;key&gt; = $&lt;extra environment variables&gt;
</code></pre>
 * @since 0.2.3
 */
public abstract class ProcessHadoopScriptHandler extends ExecutionScriptHandlerBase implements HadoopScriptHandler {

    static final Logger LOG = LoggerFactory.getLogger(ProcessHadoopScriptHandler.class);

    /**
     * (sub) key name of working directory.
     */
    private static final String KEY_WORKING_DIRECTORY = "workingDirectory";

    /**
     * Variable name of batch ID.
     */
    public static final String VAR_BATCH_ID = "batch_id";

    /**
     * Variable name of flow ID.
     */
    public static final String VAR_FLOW_ID = "flow_id";

    /**
     * Variable name of execution ID.
     */
    public static final String VAR_EXECUTION_ID = "execution_id";

    private volatile ServiceProfile<?> currentProfile;

    private volatile List<String> commandPrefix;

    private volatile List<String> cleanupPrefix;

    private volatile String workingDirectory;

    @Override
    protected final void doConfigure(
            ServiceProfile<?> profile,
            VariableResolver variables,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        this.currentProfile = profile;
        this.workingDirectory = profile.getConfiguration().get(KEY_WORKING_DIRECTORY);
        this.commandPrefix = extractCommand(profile, variables, ProcessUtil.PREFIX_COMMAND);
        this.cleanupPrefix = extractCommand(profile, variables, ProcessUtil.PREFIX_CLEANUP);
        if (commandPrefix.size() == 0) {
            throw new IOException(MessageFormat.format(
                    "Executable file is not defined: {0}",
                    profile.getPrefix() + '.' + ProcessUtil.PREFIX_COMMAND + '0'));
        }
        if (workingDirectory != null && cleanupPrefix.size() == 0) {
            throw new IOException(MessageFormat.format(
                    "Executable file is not defined: {0}",
                    profile.getPrefix() + '.' + ProcessUtil.PREFIX_CLEANUP + '0'));
        }
        configureExtension(profile, variables);
    }

    private List<String> extractCommand(
            ServiceProfile<?> profile,
            VariableResolver variables,
            String prefix) throws IOException {
        try {
            return ProcessUtil.extractCommandLineTokens(prefix, profile.getConfiguration(), variables);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve command line tokens ({0})",
                    profile.getPrefix() + '.' + prefix + '*'), e);
        }
    }

    /**
     * Configures this handler internally (extension point).
     * @param profile the profile of this service
     * @param variables variable resolver
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     */
    protected abstract void configureExtension(
            ServiceProfile<?> profile,
            VariableResolver variables) throws InterruptedException, IOException;

    /**
     * Returns command executor for this handler (extension point).
     * @return command executor
     */
    protected abstract ProcessExecutor getCommandExecutor();

    @Override
    public final void execute(
            ExecutionMonitor monitor,
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            execute0(monitor, context, script);
        } finally {
            monitor.close();
        }
    }

    @Override
    public void cleanUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            if (workingDirectory != null) {
                cleanUp0(monitor, context);
            }
        } finally {
            monitor.close();
        }
    }

    private void execute0(
            ExecutionMonitor monitor,
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;
        assert script != null;

        Map<String, String> env = buildEnvironmentVariables(script);
        LOG.debug("Env: {}", env);

        List<String> original = buildExecuteCommand(context, script);
        List<String> command;
        try {
            command = ProcessUtil.buildCommand(commandPrefix, original, Collections.<String>emptyList());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to build command: {6} (batch={0}, flow={1}, phase={3}, stage={4}, execution={2})",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    context.getPhase(),
                    script.getId(),
                    currentProfile.getPrefix(),
                    original), e);
        }
        LOG.debug("Command: {}", command);

        monitor.checkCancelled();
        ProcessExecutor executor = getCommandExecutor();
        int exit = executor.execute(command, env);
        if (exit == 0) {
            return;
        }
        throw new IOException(MessageFormat.format(
                "Failed to execute Hadoop job: code={5} (batch={0}, flow={1}, phase={3}, stage={4}, exection={2})",
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase(),
                script.getId(),
                String.valueOf(exit)));
    }

    private void cleanUp0(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;

        Map<String, String> env = getEnvironmentVariables();
        LOG.debug("Env: {}", env);

        List<String> original = buildCleanupCommand(context);
        List<String> command;
        try {
            command = ProcessUtil.buildCommand(cleanupPrefix, original, Collections.<String>emptyList());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to build cleanUp command: {5} (batch={0}, flow={1}, phase={3}, execution={2})",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    context.getPhase(),
                    currentProfile.getPrefix(),
                    original), e);
        }
        LOG.debug("Command: {}", command);

        monitor.checkCancelled();
        ProcessExecutor executor = getCommandExecutor();
        int exit = executor.execute(command, env);
        if (exit == 0) {
            return;
        }
        throw new IOException(MessageFormat.format(
                "Failed to execute Hadoop Cleanup job: code={4} (batch={0}, flow={1}, phase={3}, exection={2})",
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase(),
                String.valueOf(exit)));
    }

    private Map<String, String> buildEnvironmentVariables(ExecutionScript script) {
        assert script != null;
        Map<String, String> env = new HashMap<String, String>();
        env.putAll(getEnvironmentVariables());
        env.putAll(script.getEnvironmentVariables());
        return env;
    }

    private List<String> buildExecuteCommand(ExecutionContext context, HadoopScript script) {
        assert context != null;
        assert script != null;
        List<String> command = new ArrayList<String>();
        command.add(script.getClassName());
        command.add(context.getBatchId());
        command.add(context.getFlowId());
        command.add(context.getExecutionId());
        command.add(context.getArgumentsAsString());
        for (Map.Entry<String, String> entry : script.getHadoopProperties().entrySet()) {
            command.add("-D");
            command.add(MessageFormat.format("{0}={1}",
                    entry.getKey(),
                    entry.getValue()));
        }
        return command;
    }

    private List<String> buildCleanupCommand(ExecutionContext context) {
        assert workingDirectory != null;
        assert context != null;
        Map<String, String> map = new HashMap<String, String>();
        map.put(VAR_BATCH_ID, context.getBatchId());
        map.put(VAR_FLOW_ID, context.getFlowId());
        map.put(VAR_EXECUTION_ID, context.getExecutionId());
        VariableResolver resolver = new VariableResolver(map);
        String resolved = resolver.replace(workingDirectory, false);

        List<String> command = new ArrayList<String>();
        command.add(resolved);
        command.add(context.getBatchId());
        command.add(context.getFlowId());
        command.add(context.getExecutionId());
        command.add(context.getArgumentsAsString());
        return command;
    }
}
