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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.asakusafw.workflow.executor.BatchExecutor;
import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.model.basic.BasicBatchInfo;
import com.asakusafw.workflow.model.basic.BasicJobflowInfo;

/**
 * Test for {@link BasicBatchExecutor}.
 */
public class BasicBatchExecutorTest {

    private final ExecutionContext context = new BasicExecutionContext()
                .withEnvironmentVariables(m -> m.putAll(System.getenv()));

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        BasicBatchInfo batch = new BasicBatchInfo("b");
        BasicJobflowInfo jobflow = new BasicJobflowInfo("f");
        batch.addElement(jobflow);
        Map<String, String> arguments = Collections.singletonMap("testing", "OK");

        AtomicBoolean worked = new AtomicBoolean(false);
        BatchExecutor executor = new BasicBatchExecutor((c, j) -> {
            assertThat(worked.get(), is(false));
            assertThat(c.getBatchId(), is(batch.getId()));
            assertThat(c.getFlowId(), is(jobflow.getId()));
            assertThat(c.getExecutionId(), is("testing"));
            assertThat(c.getBatchArguments(), is(arguments));
            assertThat(j, is(jobflow));
            worked.set(true);
        }, () -> "testing");
        executor.execute(context, batch, arguments);
        assertThat(worked.get(), is(true));
    }

    /**
     * w/ dependencies.
     * @throws Exception if failed
     */
    @Test
    public void dependencies() throws Exception {
        BasicJobflowInfo a = new BasicJobflowInfo("A");
        BasicJobflowInfo b = new BasicJobflowInfo("B");
        BasicJobflowInfo c = new BasicJobflowInfo("C");
        a.addBlocker(b);
        b.addBlocker(c);

        BasicBatchInfo batch = new BasicBatchInfo("b");
        batch.addElement(a);
        batch.addElement(b);
        batch.addElement(c);

        List<String> flowIds = new ArrayList<>();
        List<String> execIds = new ArrayList<>();
        BatchExecutor executor = new BasicBatchExecutor((ctxt, jobflow) -> {
            flowIds.add(ctxt.getFlowId());
            execIds.add(ctxt.getExecutionId());
        });
        executor.execute(context, batch);

        assertThat(flowIds, contains("C", "B", "A"));
        assertThat(execIds.stream().distinct().count(), is(3L));
    }
}
