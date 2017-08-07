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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.asakusafw.workflow.executor.BatchExecutor;
import com.asakusafw.workflow.executor.ExecutionConditionException;
import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.executor.JobflowExecutor;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.model.BatchInfo;
import com.asakusafw.workflow.model.JobflowInfo;
import com.asakusafw.workflow.model.attribute.ParameterInfo;
import com.asakusafw.workflow.model.attribute.ParameterListAttribute;

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
        batch.findAttribute(ParameterListAttribute.class)
                .ifPresent(it -> validateParameters(it, arguments));
        for (JobflowInfo jobflow : Util.sort(batch.getElements())) {
            executeJobflow(context, batch, jobflow, arguments);
        }
    }

    private static void validateParameters(
            ParameterListAttribute parameters,
            Map<String, String> arguments) {
        Set<String> consumed = new HashSet<>();
        parameters.getElements().forEach(p -> {
            String name = p.getName();
            consumed.add(name);
            validateParameter(p, arguments.get(name));
        });
        if (parameters.isStrict()) {
            List<String> unknown = arguments.keySet().stream()
                    .filter(it -> consumed.contains(it) == false)
                    .collect(Collectors.toList());
            if (unknown.isEmpty() == false) {
                throw new ExecutionConditionException(MessageFormat.format(
                        "unknown batch arguments: {0}",
                        unknown));
            }
        }
    }

    private static void validateParameter(ParameterInfo parameter, String value) {
        if (parameter.isMandatory() && value == null) {
            throw new ExecutionConditionException(MessageFormat.format(
                    "batch argument \"{0}\" [{1}] must be defined",
                    parameter.getName(),
                    Optional.ofNullable(parameter.getComment()).orElse("?")));
        }
        if (parameter.getPattern() != null && value != null) {
            Pattern pattern = Pattern.compile(parameter.getPattern());
            if (pattern.matcher(value).matches() == false) {
                throw new ExecutionConditionException(MessageFormat.format(
                        "batch argument \"{0}\" [{1}] must match the pattern \"{2}\": {3}",
                        parameter.getName(),
                        Optional.ofNullable(parameter.getComment()).orElse("?"),
                        pattern,
                        value));

            }
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
