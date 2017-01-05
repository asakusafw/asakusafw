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
package com.asakusafw.testdriver.windgate.emulation;

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
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.inprocess.EmulatorUtils;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.windgate.bootstrap.CommandLineUtil;
import com.asakusafw.windgate.bootstrap.ExecutionKind;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.GateTask;
import com.asakusafw.windgate.core.ParameterList;

/**
 * Emulates {@code windgate/bin/process.sh} command.
 * @since 0.6.0
 */
public class WindGateProcessCommandEmulator extends AbstractWindGateCommandEmulator {

    static final Logger LOG = LoggerFactory.getLogger(WindGateProcessCommandEmulator.class);

    // FIXME - move to windgate-assembly
    static final String COMMAND_SUFFIX = PATH_WINDGATE + "/bin/process.sh"; //$NON-NLS-1$

    static final int ARG_SESSION_KIND = ARG_PROFILE + 1;

    static final int ARG_SCRIPT = ARG_SESSION_KIND + 1;

    static final int ARG_BATCH_ID = ARG_SCRIPT + 1;

    static final int ARG_FLOW_ID = ARG_BATCH_ID + 1;

    static final int ARG_EXECUTION_ID = ARG_FLOW_ID + 1;

    static final int ARG_ARGUMENTS = ARG_EXECUTION_ID + 1;

    static final int MINIMUM_TOKENS = ARG_EXECUTION_ID + 1;

    @Override
    public String getName() {
        return "windgate-process"; //$NON-NLS-1$
    }

    @Override
    public boolean accepts(
            TestDriverContext context,
            ConfigurationFactory configurations,
            TestExecutionPlan.Command command) {
        if (command.getModuleName().equals(Constants.MODULE_NAME) == false
                && command.getModuleName().startsWith(MODULE_NAME_PREFIX) == false) {
            return false;
        }
        List<String> cmd = command.getCommandTokens();
        if (cmd.size() < MINIMUM_TOKENS) {
            return false;
        }
        if (EmulatorUtils.hasCommandSuffix(cmd.get(0), COMMAND_SUFFIX) == false) {
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
        try (GateTask task = createTask(context, classLoader, profile, command)) {
            task.execute();
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

        Map<String, String> arguments = new LinkedHashMap<>();
        arguments.putAll(CommandLineUtil.parseArguments(cmd.get(ARG_ARGUMENTS)).getPairs());
        arguments.put(StageConstants.VAR_USER, context.getOsUser());
        arguments.put(StageConstants.VAR_BATCH_ID, cmd.get(ARG_BATCH_ID));
        arguments.put(StageConstants.VAR_FLOW_ID, cmd.get(ARG_FLOW_ID));
        arguments.put(StageConstants.VAR_EXECUTION_ID, cmd.get(ARG_EXECUTION_ID));

        GateTask task = new GateTask(
                profile,
                script,
                sessionId,
                mode.createsSession,
                mode.completesSession,
                new ParameterList(arguments));
        return task;
    }

    static GateScript loadScript(
            TestDriverContext context,
            ClassLoader classLoader,
            String script) {
        LOG.debug("Loading script: {}", script); //$NON-NLS-1$
        try {
            URI uri = CommandLineUtil.toUri(script);
            Properties properties = CommandLineUtil.loadProperties(uri, classLoader);
            return GateScript.loadFrom(CommandLineUtil.toName(uri), properties, classLoader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("WindGateProcessCommandEmulator.errorInvalidScript"), //$NON-NLS-1$
                    script), e);
        }
    }
}
