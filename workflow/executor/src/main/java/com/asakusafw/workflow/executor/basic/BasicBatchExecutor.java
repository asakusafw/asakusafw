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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.asakusafw.workflow.executor.BatchExecutor;
import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.executor.JobflowExecutor;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.model.BatchInfo;
import com.asakusafw.workflow.model.JobflowInfo;

/**
 * A basic implementation of {@link BatchExecutor}.
 * @since 0.10.0
 */
public class BasicBatchExecutor implements BatchExecutor {

    private final JobflowExecutor jobflowExecutor;

    private final Function<JobflowInfo, String> executionIds;

    /**
     * Creates a new instance.
     * @param jobflowExecutor the jobflow executor
     * @param executionIds the execution ID generator
     */
    public BasicBatchExecutor(JobflowExecutor jobflowExecutor, Function<JobflowInfo, String> executionIds) {
        Objects.requireNonNull(jobflowExecutor);
        Objects.requireNonNull(executionIds);
        this.jobflowExecutor = jobflowExecutor;
        this.executionIds = executionIds;
    }

    /**
     * Creates a new instance.
     * @param jobflowExecutor the jobflow executor
     * @param executionIds the execution ID generator
     */
    public BasicBatchExecutor(JobflowExecutor jobflowExecutor, Supplier<String> executionIds) {
        this(jobflowExecutor, jobflow -> executionIds.get());
    }

    /**
     * Creates a new instance.
     * @param jobflowExecutor the jobflow executor
     */
    public BasicBatchExecutor(JobflowExecutor jobflowExecutor) {
        this(jobflowExecutor, jobflow -> UUID.randomUUID().toString());
    }

    @Override
    public void execute(
            ExecutionContext context,
            BatchInfo batch, Map<String, String> arguments) throws IOException, InterruptedException {
        for (JobflowInfo jobflow : Util.sort(batch.getElements())) {
            executeJobflow(context, batch, jobflow, arguments);
        }
    }

    private void executeJobflow(
            ExecutionContext context,
            BatchInfo batch, JobflowInfo jobflow,
            Map<String, String> arguments) throws IOException, InterruptedException {
        String executionId = executionIds.apply(jobflow);
        TaskExecutionContext taskContext = new BasicTaskExecutionContext(
                context,
                batch.getId(), jobflow.getId(), executionId,
                arguments);
        jobflowExecutor.execute(taskContext, jobflow);
    }
}
