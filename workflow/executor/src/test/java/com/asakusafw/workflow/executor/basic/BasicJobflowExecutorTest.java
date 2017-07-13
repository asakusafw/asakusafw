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

import java.util.Collections;

import org.junit.Test;

import com.asakusafw.workflow.executor.JobflowExecutor;
import com.asakusafw.workflow.executor.MockExecutor;
import com.asakusafw.workflow.executor.MockTaskInfo;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.model.TaskInfo;
import com.asakusafw.workflow.model.basic.BasicJobflowInfo;

/**
 * Test for {@link JobflowExecutor}.
 */
public class BasicJobflowExecutorTest {

    private final TaskExecutionContext context = new BasicTaskExecutionContext(
            new BasicExecutionContext()
                .withEnvironmentVariables(m -> m.putAll(System.getenv())),
            "b", "f", "e",
            Collections.emptyMap());

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        BasicJobflowInfo jobflow = new BasicJobflowInfo(context.getFlowId());
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("A"));

        MockExecutor e0 = new MockExecutor(t -> true);
        JobflowExecutor executor = new BasicJobflowExecutor(e0);
        executor.execute(context, jobflow);

        assertThat(e0.getValues(), contains("A"));
    }

    /**
     * w/ phases.
     * @throws Exception if failed
     */
    @Test
    public void phases() throws Exception {
        BasicJobflowInfo jobflow = new BasicJobflowInfo(context.getFlowId());
        jobflow.addTask(TaskInfo.Phase.INITIALIZE, new MockTaskInfo("A"));
        jobflow.addTask(TaskInfo.Phase.IMPORT, new MockTaskInfo("B"));
        jobflow.addTask(TaskInfo.Phase.PROLOGUE, new MockTaskInfo("C"));
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("D"));
        jobflow.addTask(TaskInfo.Phase.EPILOGUE, new MockTaskInfo("E"));
        jobflow.addTask(TaskInfo.Phase.EXPORT, new MockTaskInfo("F"));
        jobflow.addTask(TaskInfo.Phase.FINALIZE, new MockTaskInfo("G"));
        jobflow.addTask(TaskInfo.Phase.CLEANUP, new MockTaskInfo("H"));

        MockExecutor e0 = new MockExecutor(t -> true);
        JobflowExecutor executor = new BasicJobflowExecutor(e0);
        executor.execute(context, jobflow);

        assertThat(e0.getValues(), contains("A", "B", "C", "D", "E", "F", "G", "H"));
    }

    /**
     * w/ dependencies.
     * @throws Exception if failed
     */
    @Test
    public void dependencies() throws Exception {
        BasicJobflowInfo jobflow = new BasicJobflowInfo(context.getFlowId());
        MockTaskInfo a = new MockTaskInfo("A");
        MockTaskInfo b = new MockTaskInfo("B");
        MockTaskInfo c = new MockTaskInfo("C");
        a.addBlocker(b);
        b.addBlocker(c);
        jobflow.addTask(TaskInfo.Phase.MAIN, a);
        jobflow.addTask(TaskInfo.Phase.MAIN, b);
        jobflow.addTask(TaskInfo.Phase.MAIN, c);

        MockExecutor e0 = new MockExecutor(t -> true);
        JobflowExecutor executor = new BasicJobflowExecutor(e0);
        executor.execute(context, jobflow);

        assertThat(e0.getValues(), contains("C", "B", "A"));
    }

    /**
     * w/ multiple executors.
     * @throws Exception if failed
     */
    @Test
    public void executors() throws Exception {
        BasicJobflowInfo jobflow = new BasicJobflowInfo(context.getFlowId());
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("0", "A"));
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("1", "B"));
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("2", "C"));
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("3", "D"));

        MockExecutor e0 = new MockExecutor(t -> t.getModuleName().equals("0"));
        MockExecutor e1 = new MockExecutor(t -> t.getModuleName().equals("1") || t.getModuleName().equals("3"));
        MockExecutor e2 = new MockExecutor(t -> t.getModuleName().equals("2") || t.getModuleName().equals("3"));
        JobflowExecutor executor = new BasicJobflowExecutor(e0, e1, e2);
        executor.execute(context, jobflow);

        assertThat(e0.getValues(), contains("A"));
        assertThat(e1.getValues(), containsInAnyOrder("B", "D"));
        assertThat(e2.getValues(), contains("C"));
    }

    /**
     * w/ multiple executors.
     * @throws Exception expected
     */
    @Test(expected = Exception.class)
    public void no_executors() throws Exception {
        BasicJobflowInfo jobflow = new BasicJobflowInfo(context.getFlowId());
        jobflow.addTask(TaskInfo.Phase.MAIN, new MockTaskInfo("A"));
        MockExecutor e0 = new MockExecutor(t -> false);
        JobflowExecutor executor = new BasicJobflowExecutor(e0);
        executor.execute(context, jobflow);
    }
}
