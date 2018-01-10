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
package com.asakusafw.workflow.executor.basic;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.ExecutionConditionException;
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
        LOG.info("start jobflow: {} - {}", context.getBatchId(), context.getFlowId());
        boolean finalized = false;
        try {
            for (TaskInfo.Phase phase : EnumSet.complementOf(BODY)) {
                executePhase(context, jobflow, phase);
            }
            finalized = true;
            executePhase(context, jobflow, TaskInfo.Phase.FINALIZE);
        } catch (Exception e) {
            if (finalized == false && jobflow.getTasks(TaskInfo.Phase.FINALIZE).isEmpty() == false) {
                LOG.error(MessageFormat.format(
                        "batch \"{0}\" was failed. we try to execute finalize phase.",
                        context.getBatchId()), e);
                try {
                    executePhase(context, jobflow, TaskInfo.Phase.FINALIZE);
                } catch (Exception nested) {
                    LOG.warn(MessageFormat.format(
                            "error occurred while running finalize of batch \"{0}\".",
                            context.getBatchId()), nested);
                    e.addSuppressed(nested);
                }
            }
            throw e;
        }
        try {
            executePhase(context, jobflow, TaskInfo.Phase.CLEANUP);
        } catch (Exception e) {
            LOG.warn(MessageFormat.format(
                    "error occurred while runnning cleanup of batch \"{0}\".",
                    context.getBatchId()), e);
        }
        LOG.info("finish jobflow: {} - {}", context.getBatchId(), context.getFlowId());
    }

    private void executePhase(
            TaskExecutionContext context,
            JobflowInfo jobflow, TaskInfo.Phase phase) throws InterruptedException, IOException {
        List<TaskInfo> tasks = Util.sort(jobflow.getTasks(phase));
        if (tasks.isEmpty() == false) {
            LOG.info("start phase: {} ({} tasks)", phase, tasks.size());
        }
        int count = 0;
        for (TaskInfo task : tasks) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("starting task: {} ({}/{} in {})", task, ++count, tasks.size(), phase);
            }
            TaskExecutor executor = findExecutor(context, task)
                    .orElseThrow(() -> new ExecutionConditionException(MessageFormat.format(
                            "there are no suitable executor for task: task={0}, executors={1}",
                            task,
                            taskExecutors.stream()
                                .map(it -> it.getClass().getSimpleName())
                                .collect(Collectors.joining(", ", "{", "}")))));
            if (LOG.isDebugEnabled()) {
                LOG.debug("task executor \"{}\" is available: {}", executor, task);
            }
            executor.execute(context, task);
        }
    }

    private Optional<TaskExecutor> findExecutor(TaskExecutionContext context, TaskInfo task) {
        return taskExecutors.stream()
                .peek(it -> LOG.trace("testing task executor {}: {}", it, task))
                .filter(it -> it.isSupported(context, task)).findFirst();
    }
}
