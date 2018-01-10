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
package com.asakusafw.testdriver.executor;

import java.util.Map;
import java.util.Optional;

import org.apache.hadoop.conf.Configuration;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;

/**
 * A bridge implementation of context for {@link TaskExecutor}.
 * @since 0.10.0
 */
public class TaskExecutorContextAdapter implements TaskExecutionContext {

    private final TestDriverContext context;

    private final ConfigurationFactory configurations;

    /**
     * Creates a new instance.
     * @param context the current context
     * @param configurations the configuration factory
     */
    public TaskExecutorContextAdapter(TestDriverContext context, ConfigurationFactory configurations) {
        this.context = context;
        this.configurations = configurations;
    }

    @Override
    public ClassLoader getClassLoader() {
        return context.getClassLoader();
    }

    @Override
    public Map<String, String> getConfigurations() {
        return context.getExtraConfigurations();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return context.getEnvironmentVariables();
    }

    @Override
    public <T> Optional<T> findResource(Class<T> type) {
        if (type == Configuration.class) {
            return Optional.of(type.cast(configurations.newInstance()));
        }
        return Optional.empty();
    }

    @Override
    public String getBatchId() {
        return context.getCurrentBatchId();
    }

    @Override
    public String getFlowId() {
        return context.getCurrentFlowId();
    }

    @Override
    public String getExecutionId() {
        return context.getCurrentExecutionId();
    }

    @Override
    public Map<String, String> getBatchArguments() {
        return context.getBatchArgs();
    }
}
