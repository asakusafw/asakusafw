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

import java.util.Map;
import java.util.Optional;

import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutionContext;

/**
 * A basic implementation of {@link TaskExecutionContext}.
 * @since 0.10.0
 */
public class BasicTaskExecutionContext implements TaskExecutionContext {

    private final ExecutionContext parent;

    private final String batchId;

    private final String flowId;

    private final String executionId;

    private final Map<String, String> batchArguments;

    /**
     * Creates a new instance.
     * @param parent the parent context
     * @param batchId the batch ID
     * @param flowId the flow ID
     * @param executionId the execution ID
     * @param batchArguments the batch arguments
     */
    public BasicTaskExecutionContext(
            ExecutionContext parent,
            String batchId, String flowId, String executionId,
            Map<String, String> batchArguments) {
        this.parent = parent;
        this.batchId = batchId;
        this.flowId = flowId;
        this.executionId = executionId;
        this.batchArguments = batchArguments;
    }

    @Override
    public String getBatchId() {
        return batchId;
    }

    @Override
    public String getFlowId() {
        return flowId;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public Map<String, String> getBatchArguments() {
        return batchArguments;
    }

    @Override
    public ClassLoader getClassLoader() {
        return parent.getClassLoader();
    }

    @Override
    public Map<String, String> getConfigurations() {
        return parent.getConfigurations();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return parent.getEnvironmentVariables();
    }

    @Override
    public <T> Optional<T> findResource(Class<T> type) {
        return parent.findResource(type);
    }
}
