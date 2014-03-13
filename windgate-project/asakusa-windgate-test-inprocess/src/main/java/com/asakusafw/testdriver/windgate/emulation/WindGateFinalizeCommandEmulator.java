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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.windgate.core.AbortTask;
import com.asakusafw.windgate.core.GateProfile;

/**
 * Emulates {@code windgate/bin/finalize.sh} command.
 * @since 0.6.0
 */
public class WindGateFinalizeCommandEmulator extends AbstractWindGateCommandEmulator {

    static final Logger LOG = LoggerFactory.getLogger(WindGateFinalizeCommandEmulator.class);

    private static final String PATH_COMMAND = PATH_WINDGATE + "/bin/finalize.sh";

    @Override
    public String getName() {
        return "windgate-finalize";
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
        if (cmd.size() < 1) {
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
        AbortTask task = new AbortTask(profile, context.getExecutionId());
        task.execute();
    }
}
