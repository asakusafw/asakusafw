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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandlerBase;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * An abstract implementation of process-based {@link CommandScriptHandler}.
 * <h3> Profile format </h3>
<pre><code>
# &lt;position&gt; = 0, 1, 2, ...
# &lt;prefix command token&gt; can contain "&#64;[position],"
# this will be replaced as original command tokens (0-origin position)
command.&lt;profile-name&gt; = &lt;subclass class name&gt;
command.&lt;profile-name&gt.env.ASAKUSA_HOME = ${ASAKUSA_HOME}
command.&lt;profile-name&gt.command.&lt;position&gt; = $&lt;prefix command token&gt;
command.&lt;profile-name&gt.env.&lt;key&gt; = $&lt;extra environment variables&gt;
</code></pre>
 * @since 0.2.3
 * @version 0.2.6
 */
public abstract class ProcessCommandScriptHandler extends ExecutionScriptHandlerBase implements CommandScriptHandler {

    static final Logger LOG = LoggerFactory.getLogger(ProcessCommandScriptHandler.class);

    private volatile ServiceProfile<?> currentProfile;

    private volatile List<String> commandPrefix;

    private volatile List<String> setupCommand;

    private volatile List<String> cleanupCommand;

    @Override
    protected final void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        this.currentProfile = profile;
        this.commandPrefix = extractCommand(profile, ProcessUtil.PREFIX_COMMAND);
        this.setupCommand = extractCommand(profile, ProcessUtil.PREFIX_SETUP);
        this.cleanupCommand = extractCommand(profile, ProcessUtil.PREFIX_CLEANUP);
        configureExtension(profile);
    }

    private List<String> extractCommand(
            ServiceProfile<?> profile,
            String prefix) throws IOException {
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
            CommandScript script) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            execute0(monitor, context, script);
        } finally {
            monitor.close();
        }
    }

    @Override
    public void setUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            if (setupCommand.isEmpty() == false) {
                command(monitor, context, null, setupCommand);
            }
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
            if (cleanupCommand.isEmpty() == false) {
                command(monitor, context, null, cleanupCommand);
            }
        } finally {
            monitor.close();
        }
    }

    private void execute0(
            ExecutionMonitor monitor,
            ExecutionContext context,
            CommandScript script)  throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;
        assert script != null;

        Map<String, String> env = buildEnvironmentVariables(context, script);
        LOG.debug("Env: {}", env);

        List<String> original = script.getCommandLineTokens();
        List<String> command;
        try {
            command = ProcessUtil.buildCommand(commandPrefix, original, Collections.<String>emptyList());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to build command: {6} (batch={0}, flow={1}, phase={3}, stage={4}, execution={2})",
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
        throw new ExitCodeException(MessageFormat.format(
                "Unexpected exit code from command job: "
                + "code={4} (batch={0}, flow={1}, phase={2}, stage={4}, exection={3})",
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId(),
                script.getId(),
                String.valueOf(exit)), exit);
    }

    private void command(
            ExecutionMonitor monitor,
            ExecutionContext context,
            ExecutionScript script,
            List<String> command) throws InterruptedException, IOException {
        assert monitor != null;
        assert context != null;
        assert command != null;
        assert command.isEmpty() == false;

        LOG.debug("Command: {}", command);
        monitor.checkCancelled();
        ProcessExecutor executor = getCommandExecutor();
        int exit = executor.execute(context, command, getEnvironmentVariables(context, script), monitor.getOutput());
        if (exit == 0) {
            return;
        }
        throw new ExitCodeException(MessageFormat.format(
                "Unexpected exit code from command job: "
                + "code={4} (batch={0}, flow={1}, phase={2}, exection={3})",
                context.getBatchId(),
                context.getFlowId(),
                context.getPhase(),
                context.getExecutionId(),
                String.valueOf(exit)), exit);
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
}
