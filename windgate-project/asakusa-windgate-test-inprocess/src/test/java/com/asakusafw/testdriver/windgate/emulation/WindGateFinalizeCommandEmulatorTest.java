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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;

/**
 * Test for {@link WindGateFinalizeCommandEmulator}.
 */
public class WindGateFinalizeCommandEmulatorTest extends WindGateCommandEmulatorTestRoot {

    private final ConfigurationFactory configurations = ConfigurationFactory.getDefault();

    /**
     * accepts - simple case.
     */
    @Test
    public void accepts() {
        WindGateFinalizeCommandEmulator emulator = new WindGateFinalizeCommandEmulator();
        boolean result = emulator.accepts(
                context,
                configurations,
                command());
        assertThat(result, is(true));
    }

    /**
     * accepts - invalid command path.
     */
    @Test
    public void accepts_invalid_path() {
        WindGateFinalizeCommandEmulator emulator = new WindGateFinalizeCommandEmulator();
        boolean result = emulator.accepts(
                context,
                configurations,
                command("INVALID.sh", PROFILE));
        assertThat(result, is(false));
    }

    /**
     * execute - simple case.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        WindGateFinalizeCommandEmulator emulator = new WindGateFinalizeCommandEmulator();
        emulator.execute(context, configurations, command());
    }

    /**
     * accepts - invalid arguments.
     */
    @Test
    public void accepts_invalid_args() {
        WindGateFinalizeCommandEmulator emulator = new WindGateFinalizeCommandEmulator();
        boolean result = emulator.accepts(
                context,
                configurations,
                command(WindGateFinalizeCommandEmulator.COMMAND_SUFFIX));
        assertThat(result, is(false));
    }

    private TestExecutionPlan.Command command() {
        return command(
                WindGateFinalizeCommandEmulator.COMMAND_SUFFIX,
                PROFILE,
                getContext().getCurrentBatchId(),
                getContext().getCurrentFlowId(),
                getContext().getCurrentExecutionId());
    }
}
