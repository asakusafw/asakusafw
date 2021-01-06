/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.CommandTaskInfo;

/**
 * Test for {@link InProcessWindGateFinalizeTaskExecutor}.
 */
public class InProcessWindGateFinalizeTaskExecutorTest extends InProcessWindGateTaskExecutorTestRoot {

    /**
     * accepts - simple case.
     */
    @Test
    public void accepts() {
        TaskExecutor executor = new InProcessWindGateFinalizeTaskExecutor();
        boolean result = executor.isSupported(
                context,
                command());
        assertThat(result, is(true));
    }

    /**
     * accepts - invalid command path.
     */
    @Test
    public void accepts_invalid_path() {
        TaskExecutor executor = new InProcessWindGateFinalizeTaskExecutor();
        boolean result = executor.isSupported(
                context,
                command("INVALID.sh", PROFILE));
        assertThat(result, is(false));
    }

    /**
     * execute - simple case.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        TaskExecutor executor = new InProcessWindGateFinalizeTaskExecutor();
        executor.execute(context, command());
    }

    /**
     * accepts - invalid arguments.
     */
    @Test
    public void accepts_invalid_args() {
        TaskExecutor executor = new InProcessWindGateFinalizeTaskExecutor();
        boolean result = executor.isSupported(
                context,
                command(InProcessWindGateFinalizeTaskExecutor.COMMAND_SUFFIX));
        assertThat(result, is(false));
    }

    private CommandTaskInfo command() {
        return command(
                InProcessWindGateFinalizeTaskExecutor.COMMAND_SUFFIX,
                PROFILE,
                getContext().getBatchId(),
                getContext().getFlowId(),
                getContext().getExecutionId());
    }
}
