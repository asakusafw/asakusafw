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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.JobflowExecutor;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.JobflowInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes jobflows.
 * @since 0.10.0
 */
public class BasicJobflowExecutor implements JobflowExecutor {

    static final Logger LOG = LoggerFactory.getLogger(BasicJobflowExecutor.class);

    private static final EnumSet<TaskInfo.Phase> BODY = EnumSet.of(TaskInfo.Phase.FINALIZE, TaskInfo.Phase.CLEANUP);

    private final List<TaskExecutor> taskExecutors;

    /**
     * Creates a new instance.
     * @param taskExecutors the task executors
     */
    public BasicJobflowExecutor(Collection<? extends TaskExecutor> taskExecutors) {
        this.taskExecutors = new ArrayList<>(taskExecutors);
    }

    /**
     * Creates a new instance.
     * @param taskExecutors the task executors
     */
    public BasicJobflowExecutor(TaskExecutor... taskExecutors) {
        this(Arrays.asList(taskExecutors));
    }

    @Override
    public void execute(TaskExecutionContext context, JobflowInfo jobflow) throws IOException, InterruptedException {
        try {
            for (TaskInfo.Phase phase : EnumSet.complementOf(BODY)) {
                executePhase(context, jobflow, phase);
            }
        } finally {
            executePhase(context, jobflow, TaskInfo.Phase.FINALIZE);
        }
        executePhase(context, jobflow, TaskInfo.Phase.CLEANUP);
    }

    private void executePhase(
            TaskExecutionContext context,
            JobflowInfo jobflow, TaskInfo.Phase phase) throws InterruptedException, IOException {
        List<TaskInfo> tasks = Util.sort(jobflow.getTasks(phase));
        if (LOG.isInfoEnabled() && tasks.isEmpty() == false) {
            LOG.info("starting phase: {}@{}", phase, context.getFlowId());
        }
        for (TaskInfo task : tasks) {
            TaskExecutor executor = findExecutor(context, task)
                    .orElseThrow(() -> new NoSuchElementException(MessageFormat.format(
                            "there are no suitable executor for task: kind={0}, module={1}",
                            task.getClass().getSimpleName(),
                            task.getModuleName())));
            executor.execute(context, task);
        }
    }

    private Optional<TaskExecutor> findExecutor(TaskExecutionContext context, TaskInfo task) {
        return taskExecutors.stream().filter(it -> it.isSupported(context, task)).findFirst();
    }
}
