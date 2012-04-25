/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.TreeMap;

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
 *
 * <h3> Profile format </h3>
<pre><code>
# &lt;position&gt; = 0, 1, 2, ...
# &lt;prefix command token&gt; can contain "&#64;[position],"
# this will be replaced as original command tokens (0-origin position)
hadoop = &lt;this class name&gt;
hadoop.env.ASAKUSA_HOME = ${ASAKUSA_HOME}
hadoop.command.&lt;position&gt; = $&lt;prefix command token&gt;
hadoop.cleanup.&lt;position&gt; = $&lt;prefix command token&gt;
hadoop.workingDirectory = $<cluster working directory>
hadoop.env.&lt;key&gt; = $&lt;extra environment variables&gt;
</code></pre>
 * @since 0.2.3
 * @version 0.2.6
 */
public abstract class ProcessHadoopScriptHandler extends ExecutionScriptHandlerBase implements HadoopScriptHandler {

    static final Logger LOG = LoggerFactory.getLogger(ProcessHadoopScriptHandler.class);

    /**
     * (sub) key name of working directory.
     */
    public static final String KEY_WORKING_DIRECTORY = "workingDirectory";

    /**
     * The path to the Hadoop execution executable file (relative path from Asakusa home).
     */
    public static final String PATH_EXECUTE = "yaess-hadoop/bin/hadoop-execute.sh";

    /**
     * The path to the Hadoop cleanup executable file (relative path from Asakusa home).
     */
    public static final String PATH_CLEANUP = "yaess-hadoop/bin/hadoop-cleanup.sh";

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
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        this.currentProfile = profile;
        this.workingDirectory = profile.getConfiguration().get(KEY_WORKING_DIRECTORY);
        this.commandPrefix = extractCommand(profile, ProcessUtil.PREFIX_COMMAND);
        this.cleanupPrefix = extractCommand(profile, ProcessUtil.PREFIX_CLEANUP);
        configureExtension(profile);
    }

    private List<String> extractCommand(ServiceProfile<?> profile, String prefix) throws IOException {
        try {
            return ProcessUtil.extractCommandLineTokens(
                    prefix,
                    profile.getConfiguration(),
                    profile.getContext().getContextParameters());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve command line tokens ({0})",
                    profile.getPrefix() + '.' + prefix + '*'), e);
        }
    }

    /**
     * Configures this handler internally (extension point).
     * @param profile the profile of this service
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     */
    protected abstract void configureExtension(ServiceProfile<?> profile) throws InterruptedException, IOException;

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

        Map<String, String> env = buildEnvironmentVariables(context, script);
        LOG.debug("Env: {}", env);

        List<String> original = buildExecutionCommand(context, script);
        List<String> command;
        try {
            command = ProcessUtil.buildCommand(commandPrefix, original, Collections.<String>emptyList());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to build command: {6} (batch={0}, flow={1}, phase={2}, stage={4}, execution={3})",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getPhase(),
                    context.getExecutionId(),
                    script.getId(),
                    currentProfile.getPrefix(),
                    original), e);
        }
        LOG.debug("Command: {}", command);

        monitor.checkCancelled();
        ProcessExecutor executor = getCommandExecutor();
        int exit = executor.execute(context, command, env, monitor.getOutput());
        if (exit == 0) {
            return;
        }
        throw new IOException(MessageFormat.format(
                "Failed to execute Hadoop job: code={5} (batch={0}, flow={1}, phase={2}, stage={4}, exection={3})",
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId(),
                script.getId(),
                String.valueOf(exit)));
    }

    private void cleanUp0(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;

        Map<String, String> env = getEnvironmentVariables(context, null);
        LOG.debug("Env: {}", env);

        List<String> original = buildCleanupCommand(context);
        List<String> command;
        try {
            command = ProcessUtil.buildCommand(cleanupPrefix, original, Collections.<String>emptyList());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to build cleanUp command: {5} (batch={0}, flow={1}, phase={2}, execution={3})",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getPhase(),
                    context.getExecutionId(),
                    currentProfile.getPrefix(),
                    original), e);
        }
        LOG.debug("Command: {}", command);

        monitor.checkCancelled();
        ProcessExecutor executor = getCommandExecutor();
        int exit = executor.execute(context, command, env, monitor.getOutput());
        if (exit == 0) {
            return;
        }
        throw new IOException(MessageFormat.format(
                "Failed to execute Hadoop Cleanup job: code={4} (batch={0}, flow={1}, phase={2}, exection={3})",
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId(),
                String.valueOf(exit)));
    }

    private Map<String, String> buildEnvironmentVariables(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        assert script != null;
        Map<String, String> env = new HashMap<String, String>();
        env.putAll(getEnvironmentVariables(context, script));
        env.putAll(script.getEnvironmentVariables());
        return env;
    }

    private List<String> buildExecutionCommand(
            ExecutionContext context,
            HadoopScript script) throws IOException, InterruptedException {
        assert context != null;
        assert script != null;
        List<String> command = new ArrayList<String>();
        command.add(getCommand(context, PATH_EXECUTE, script));
        command.add(script.getClassName());
        command.add(context.getBatchId());
        command.add(context.getFlowId());
        command.add(context.getExecutionId());
        command.add(context.getArgumentsAsString());

        Map<String, String> props = buildHadoopProperties(context, script);

        for (Map.Entry<String, String> entry : props.entrySet()) {
            command.add("-D");
            command.add(MessageFormat.format("{0}={1}",
                    entry.getKey(),
                    entry.getValue()));
        }
        return command;
    }

    private Map<String, String> buildHadoopProperties(
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        assert context != null;
        assert script != null;
        Map<String, String> props = new TreeMap<String, String>();
        props.putAll(getProperties(context, script));
        props.putAll(script.getHadoopProperties());
        return props;
    }

    private List<String> buildCleanupCommand(ExecutionContext context) throws IOException, InterruptedException {
        assert workingDirectory != null;
        assert context != null;
        Map<String, String> map = new HashMap<String, String>();
        map.put(VAR_BATCH_ID, context.getBatchId());
        map.put(VAR_FLOW_ID, context.getFlowId());
        map.put(VAR_EXECUTION_ID, context.getExecutionId());
        VariableResolver resolver = new VariableResolver(map);
        String resolved = resolver.replace(workingDirectory, false);

        List<String> command = new ArrayList<String>();
        command.add(getCommand(context, PATH_CLEANUP, null));
        command.add(resolved);
        command.add(context.getBatchId());
        command.add(context.getFlowId());
        command.add(context.getExecutionId());
        command.add(context.getArgumentsAsString());
        return command;
    }

    private String getCommand(
            ExecutionContext context,
            String command,
            HadoopScript script) throws IOException, InterruptedException {
        assert command != null;
        Map<String, String> variables;
        if (script != null) {
            variables = buildEnvironmentVariables(context, script);
        } else {
            variables = getEnvironmentVariables(context, script);
        }
        String home = variables.get(ExecutionScript.ENV_ASAKUSA_HOME);
        if (home == null) {
            throw new IOException(MessageFormat.format(
                    "Asakusa installation path is not known: {0}",
                    currentProfile.getPrefix() + '.' + KEY_ENV_PREFIX + ExecutionScript.ENV_ASAKUSA_HOME));
        }
        if (home.endsWith(getPathSegmentSeparator())) {
            return home + command;
        } else {
            return home + getPathSegmentSeparator() + command;
        }
    }

    /**
     * Returns the path segment separator.
     * @return the path segment separator string
     */
    protected String getPathSegmentSeparator() {
        return "/";
    }
}
