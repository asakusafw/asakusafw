/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.workflow.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicCommandTaskExecutor;
import com.asakusafw.workflow.executor.basic.BasicExecutionContext;
import com.asakusafw.workflow.executor.basic.BasicTaskExecutionContext;
import com.asakusafw.workflow.model.TaskInfo;
import com.asakusafw.workflow.model.basic.BasicHadoopTaskInfo;

/**
 * Test for {@link BridgeHadoopTaskExecutor}.
 */
public class BridgeHadoopTaskExecutorTest {

    private static final String PATH_BRIDGE_SCRIPT = BasicCommandTaskExecutor.WINDOWS
            ? Constants.PATH_BRIDGE_SCRIPT_WINDOWS
            : Constants.PATH_BRIDGE_SCRIPT;

    static final Path HOME_DIR = Paths.get("src/main/dist");

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    final BasicExecutionContext parent = new BasicExecutionContext()
        .withEnvironmentVariables(m -> m.putAll(System.getenv()))
        .withEnvironmentVariables(m -> m.put(
                TaskExecutors.ENV_FRAMEWORK_PATH,
                HOME_DIR.toAbsolutePath().toString()));

    final TaskExecutionContext context = new BasicTaskExecutionContext(
            parent,
            "b", "f", "testing",
            Collections.singletonMap("testing", "OK"));

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        TaskInfo task = new BasicHadoopTaskInfo("testing", "TEST_CLASS");
        TaskExecutor executor = new BridgeHadoopTaskExecutor(ctxt -> (command, arguments) -> {
            assertThat(command.toRealPath(), is(HOME_DIR.resolve(PATH_BRIDGE_SCRIPT).toRealPath()));
            assertThat(Paths.get(arguments.get(0)), pathEndsWith(Constants.PATH_LAUNCHER_LIBRARY));
            assertThat(arguments.get(1), is(BridgeHadoopTaskExecutor.LAUNCHER_CLASS));
            assertThat(arguments.get(2), is("TEST_CLASS"));
            return 0;
        });
        assertThat(executor.isSupported(context, task), is(true));
        executor.execute(context, task);
    }

    static Matcher<Path> pathEndsWith(String suffix) {
        return new BaseMatcher<Path>() {
            @Override
            public boolean matches(Object item) {
                return ((Path) item).endsWith(suffix);
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("ends with ").appendValue(suffix);
            }
        };
    }
}
