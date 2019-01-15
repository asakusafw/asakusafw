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

import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandlerBase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.Job;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.YaessLogger;
import com.asakusafw.yaess.core.util.HadoopScriptUtil;

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
 * Additionally, the handler lanuches a command if {@code hadoop.cleanup} is true.
 * <ol>
 * <li> {@link #CLEANUP_STAGE_CLASS} </li>
 * <li> {@link ExecutionContext#getBatchId() batch-id} </li>
 * <li> {@link ExecutionContext#getFlowId() flow-id} </li>
 * <li> {@link ExecutionContext#getExecutionId() execution-id} </li>
 * <li> {@link ExecutionContext#getArgumentsAsString() batch-arguments} </li>
 * <li> hadoop properties (with "-D") </li>
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
hadoop.cleanup = whether enables cleanup
hadoop.env.&lt;key&gt; = $&lt;extra environment variables&gt;
hadoop.prop.&lt;key&gt; = $&lt;extra Hadoop properties&gt;
</code></pre>
 * @since 0.2.3
 * @version 0.5.0
 */
public abstract class ProcessHadoopScriptHandler extends ExecutionScriptHandlerBase implements HadoopScriptHandler {

    static final YaessLogger YSLOG = new YaessBasicLogger(ProcessHadoopScriptHandler.class);

    static final Logger LOG = LoggerFactory.getLogger(ProcessHadoopScriptHandler.class);

    /**
     * The class name of cleanup stage client.
     * @since 0.4.0
     */
    public static final String CLEANUP_STAGE_CLASS = HadoopScriptUtil.CLEANUP_STAGE_CLASS;

    /**
     * (sub) key name of working directory.
     * @deprecated cleanup is obsoleted
     */
    @Deprecated
    public static final String KEY_WORKING_DIRECTORY = "workingDirectory";

    /**
     * (sub) key name of cleanup enabled.
     */
    public static final String KEY_CLEANUP = "cleanup";

    /**
     * The path to the Hadoop execution executable file (relative path from Asakusa home).
     */
    public static final String PATH_EXECUTE = "yaess-hadoop/libexec/hadoop-execute.sh";

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

    private boolean cleanup;

    @Override
    protected final void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        this.currentProfile = profile;
        this.commandPrefix = extractCommand(profile, ProcessUtil.PREFIX_COMMAND);
        this.cleanup = extractBoolean(profile, KEY_CLEANUP, true);
        checkCleanupConfigurations(profile);
        configureExtension(profile);
    }

    private void checkCleanupConfigurations(ServiceProfile<?> profile) throws IOException {
        assert profile != null;
        String workingDirectory = profile.getConfiguration().get(KEY_WORKING_DIRECTORY);
        if (workingDirectory != null) {
            YSLOG.warn("W10001", profile.getPrefix(), KEY_WORKING_DIRECTORY, KEY_CLEANUP);
        }
        List<String> cleanupPrefix = extractCommand(profile, ProcessUtil.PREFIX_CLEANUP);
        if (cleanupPrefix.isEmpty() == false) {
            YSLOG.warn("W10001", profile.getPrefix(), ProcessUtil.PREFIX_CLEANUP + "*", KEY_CLEANUP);
        }
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

    private boolean extractBoolean(ServiceProfile<?> profile, String key, boolean defaultValue) throws IOException {
        assert profile != null;
        assert key != null;
        String string = profile.getConfiguration(key, false, true);
        if (string == null) {
            return defaultValue;
        }
        string = string.trim();
        if (string.isEmpty()) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(string);
        } catch (RuntimeException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve boolean value ({0}={1})",
                    profile.getPrefix() + '.' + key,
                    string), e);
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
            if (cleanup) {
                YSLOG.info("I51001",
                        context.getBatchId(),
                        context.getFlowId(),
                        context.getExecutionId(),
                        getHandlerId());
                HadoopScript script = new HadoopScript(
                        context.getPhase().getSymbol(),
                        Collections.emptySet(),
                        CLEANUP_STAGE_CLASS,
                        Collections.emptyMap(),
                        Collections.emptyMap());
                execute0(monitor, context, script);
            } else {
                YSLOG.info("I51002",
                        context.getBatchId(),
                        context.getFlowId(),
                        context.getExecutionId(),
                        getHandlerId());
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
        LOG.debug("env: {}", env);

        List<String> original = buildExecutionCommand(context, script);
        List<String> command;
        try {
            command = ProcessUtil.buildCommand(commandPrefix, original, Collections.emptyList());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to build command: "
                    + "{6} (batch={0}, flow={1}, phase={2}, stage={4}, execution={3})",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getPhase(),
                    context.getExecutionId(),
                    script.getId(),
                    currentProfile.getPrefix(),
                    original), e);
        }
        LOG.debug("command: {}", command);

        Map<String, Blob> extensions = BlobUtil.getExtensions(context, script);
        LOG.debug("extensions: {}", extensions);

        monitor.checkCancelled();
        ProcessExecutor executor = getCommandExecutor();
        int exit = executor.execute(context, command, env, extensions, monitor.getOutput());
        if (exit == 0) {
            return;
        }
        throw new ExitCodeException(MessageFormat.format(
                "Unexpected exit code from Hadoop job: "
                + "code={5} (batch={0}, flow={1}, phase={2}, stage={4}, exection={3})",
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId(),
                script.getId(),
                String.valueOf(exit)), exit);
    }

    private Map<String, String> buildEnvironmentVariables(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        assert script != null;
        Map<String, String> env = new HashMap<>();
        env.putAll(getEnvironmentVariables(context, script));
        env.putAll(context.getEnvironmentVariables());
        env.putAll(script.getEnvironmentVariables());
        return env;
    }

    private List<String> buildExecutionCommand(
            ExecutionContext context,
            HadoopScript script) throws IOException, InterruptedException {
        assert context != null;
        assert script != null;
        List<String> command = new ArrayList<>();
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
        Map<String, String> props = new TreeMap<>();
        props.putAll(getProperties(context, script));
        props.putAll(script.getHadoopProperties());
        props.put(HadoopScriptUtil.PROP_TRACKING_ID, Job.computeTrackingId(context, script));
        return props;
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
            variables = getEnvironmentVariables(context, null);
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
