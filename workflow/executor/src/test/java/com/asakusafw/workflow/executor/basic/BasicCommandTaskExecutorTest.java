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
package com.asakusafw.workflow.executor.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.workflow.executor.MockTaskInfo;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.basic.BasicCommandTaskInfo;

/**
 * Test for {@link BasicCommandTaskExecutor}.
 */
public class BasicCommandTaskExecutorTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    private final BasicExecutionContext parent = new BasicExecutionContext()
        .withEnvironmentVariables(m -> m.putAll(System.getenv()));

    private final TaskExecutionContext context = new BasicTaskExecutionContext(
            parent,
            "b", "f", "e",
            Collections.singletonMap("testing", "OK"));

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        String cmd = command("mock.cmd");
        TaskExecutor executor = new BasicCommandTaskExecutor(c -> (command, arguments) -> {
            assertThat(command.getFileName().toString(), is(cmd));
            assertThat(arguments, contains(
                    "Hello, world!",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    "testing=OK"));
            return 0;
        });
        executor.execute(context, new BasicCommandTaskInfo("mock", "testing", cmd, Arrays.asList(
                CommandToken.of("Hello, world!"),
                CommandToken.BATCH_ID,
                CommandToken.FLOW_ID,
                CommandToken.EXECUTION_ID,
                CommandToken.BATCH_ARGUMENTS)));
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void path_ext() throws Exception {
        String cmd = command("mock");
        String cmdExt = command("mock.cmd");
        String expect;
        if (BasicCommandTaskExecutor.WINDOWS) {
            parent.withEnvironmentVariables(m -> m.put("PathExt", ".EXE;.BAT;.CMD"));
            expect = cmdExt;
        } else {
            expect = cmd;
        }
        TaskExecutor executor = new BasicCommandTaskExecutor(c -> (command, arguments) -> {
            assertThat(command.getFileName().toString().toLowerCase(Locale.ENGLISH), is(expect));
            assertThat(arguments, contains(
                    "Hello, world!",
                    context.getBatchId(),
                    context.getFlowId(),
                    context.getExecutionId(),
                    "testing=OK"));
            return 0;
        });
        executor.execute(context, new BasicCommandTaskInfo("mock", "testing", cmd, Arrays.asList(
                CommandToken.of("Hello, world!"),
                CommandToken.BATCH_ID,
                CommandToken.FLOW_ID,
                CommandToken.EXECUTION_ID,
                CommandToken.BATCH_ARGUMENTS)));
    }

    /**
     * w/ abnormal exit.
     * @throws Exception expected
     */
    @Test(expected = Exception.class)
    public void abend() throws Exception {
        String cmd = command("mock.cmd");
        TaskExecutor executor = new BasicCommandTaskExecutor(c -> (command, arguments) -> {
            return 1;
        });
        executor.execute(context, new BasicCommandTaskInfo("mock", "testing", cmd, Arrays.asList()));
    }

    /**
     * check supported.
     * @throws Exception if failed
     */
    @Test
    public void support() throws Exception {
        TaskExecutor executor = new BasicCommandTaskExecutor();
        assertThat(
                executor.isSupported(context, new MockTaskInfo("mock")),
                is(false));
        assertThat(
                executor.isSupported(context, new BasicCommandTaskInfo("m", "t", "missing", Arrays.asList())),
                is(false));
        assertThat(
                executor.isSupported(context, new BasicCommandTaskInfo("m", "t", command("mock.cmd"), Arrays.asList())),
                is(true));
    }

    private String command(String name) {
        parent.withEnvironmentVariables(m -> m.put(
                TaskExecutors.ENV_FRAMEWORK_PATH,
                temporary.getRoot().getAbsolutePath()));
        try {
            File path = temporary.newFile(name);
            path.setExecutable(true);
            return path.getName();
        } catch (IOException e) {
            Assume.assumeNoException(e);
            throw new AssertionError(e);
        }
    }
}
