/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate.emulation;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.windgate.bootstrap.CommandLineUtil;
import com.asakusafw.windgate.bootstrap.ExecutionKind;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.GateTask;

/**
 * Emulates {@code windgate/bin/process.sh} command.
 * @since 0.6.0
 */
public class WindGateProcessCommandEmulator extends AbstractWindGateCommandEmulator {

    static final Logger LOG = LoggerFactory.getLogger(WindGateProcessCommandEmulator.class);

    private static final String PATH_COMMAND = PATH_WINDGATE + "/bin/process.sh";

    private static final int ARG_SESSION_KIND = 2;

    private static final int ARG_SCRIPT = 3;

    private static final int ARG_ARGUMENTS = 7;

    @Override
    public String getName() {
        return "windgate-process";
    }

    @Override
    public boolean accepts(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command) {
        if (command.getModuleName().startsWith(MODULE_NAME_PREFIX) == false) {
            return false;
        }
        List<String> cmd = command.getCommandTokens();
        if (cmd.size() < 7) {
            return false;
        }
        if (cmd.get(0).endsWith('/' + PATH_COMMAND) == false) {
            return false;
        }
        return true;
    }

    @Override
    protected void execute0(
            TestDriverContext context,
            ClassLoader classLoader,
            GateProfile profile,
            TestExecutionPlan.Command command) throws IOException, InterruptedException {
        GateTask task = createTask(context, classLoader, profile, command);
        try {
            task.execute();
        } finally {
            task.close();
        }
    }

    private GateTask createTask(
            TestDriverContext context,
            ClassLoader classLoader,
            GateProfile profile,
            TestExecutionPlan.Command command) throws IOException {
        List<String> cmd = command.getCommandTokens();
        GateScript script = loadScript(context, classLoader, cmd.get(ARG_SCRIPT));
        String sessionId = context.getExecutionId();
        ExecutionKind mode = ExecutionKind.parse(cmd.get(ARG_SESSION_KIND));
        String arguments = cmd.get(ARG_ARGUMENTS);
        GateTask task = new GateTask(
                profile,
                script,
                sessionId,
                mode.createsSession,
                mode.completesSession,
                CommandLineUtil.parseArguments(arguments));
        return task;
    }

    static GateScript loadScript(
            TestDriverContext context,
            ClassLoader classLoader,
            String script) {
        LOG.debug("Loading script: {}", script);
        try {
            URI uri = CommandLineUtil.toUri(script);
            Properties properties = CommandLineUtil.loadProperties(uri, classLoader);
            return GateScript.loadFrom(CommandLineUtil.toName(uri), properties, classLoader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid script \"{0}\".",
                    script), e);
        }
    }

    static void closeQuietly(ClassLoader object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
