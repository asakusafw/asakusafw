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
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.DeleteTaskInfo.PathKind;
import com.asakusafw.workflow.model.basic.BasicDeleteTaskInfo;

/**
 * Test for {@link BasicDeleteTaskExecutor}.
 */
public class BasicDeleteTaskExecutorTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    private final TaskExecutionContext context = new BasicTaskExecutionContext(
            new BasicExecutionContext()
                .withEnvironmentVariables(m -> m.putAll(System.getenv())),
            "b", "f", "e",
            Collections.singletonMap("testing", "OK"));

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        TaskExecutor executor = new BasicDeleteTaskExecutor();
        temporary.newFile();
        executor.execute(context, new BasicDeleteTaskInfo(
                "testing",
                PathKind.LOCAL_FILE_SYSTEM,
                temporary.getRoot().getAbsolutePath()));
        assertThat(temporary.getRoot().exists(), is(false));
    }

    /**
     * attempt to delete missing files.
     * @throws Exception if failed
     */
    @Test
    public void missing() throws Exception {
        TaskExecutor executor = new BasicDeleteTaskExecutor();
        executor.execute(context, new BasicDeleteTaskInfo(
                "testing",
                PathKind.LOCAL_FILE_SYSTEM,
                new File(temporary.getRoot(), "__MISSING__").getAbsolutePath()));
        assertThat(temporary.getRoot().exists(), is(true));
    }

    /**
     * w/ variables.
     * @throws Exception if failed
     */
    @Test
    public void vars() throws Exception {
        TaskExecutor executor = new BasicDeleteTaskExecutor();
        File batch = temporary.newFile(context.getBatchId());
        File flow = temporary.newFile(context.getFlowId());
        File exec = temporary.newFile(context.getExecutionId());

        assertThat(batch.isFile(), is(true));
        assertThat(flow.isFile(), is(true));
        assertThat(exec.isFile(), is(true));

        executor.execute(context, new BasicDeleteTaskInfo(
                "testing",
                PathKind.LOCAL_FILE_SYSTEM,
                new File(temporary.getRoot(), "${batch_id}").getAbsolutePath()));
        assertThat(batch.isFile(), is(false));
        assertThat(flow.isFile(), is(true));
        assertThat(exec.isFile(), is(true));

        executor.execute(context, new BasicDeleteTaskInfo(
                "testing",
                PathKind.LOCAL_FILE_SYSTEM,
                new File(temporary.getRoot(), "${flow_id}").getAbsolutePath()));
        assertThat(batch.isFile(), is(false));
        assertThat(flow.isFile(), is(false));
        assertThat(exec.isFile(), is(true));

        executor.execute(context, new BasicDeleteTaskInfo(
                "testing",
                PathKind.LOCAL_FILE_SYSTEM,
                new File(temporary.getRoot(), "${execution_id}").getAbsolutePath()));
        assertThat(batch.isFile(), is(false));
        assertThat(flow.isFile(), is(false));
        assertThat(exec.isFile(), is(false));

        executor.execute(context, new BasicDeleteTaskInfo(
                "testing",
                PathKind.LOCAL_FILE_SYSTEM,
                new File(temporary.getRoot(), "${user}").getAbsolutePath()));
    }
}
