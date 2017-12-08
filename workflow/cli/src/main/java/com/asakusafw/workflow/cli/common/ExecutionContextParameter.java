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
package com.asakusafw.workflow.cli.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.ExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.executor.basic.BasicExecutionContext;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Handles parameters about execution context.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.workflow.cli.jcommander")
public class ExecutionContextParameter {

    static final Logger LOG = LoggerFactory.getLogger(ExecutionContextParameter.class);

    /**
     * The batch application base directory.
     */
    @Parameter(
            names = { "-B", "--batchapps" },
            descriptionKey = "parameter.batchapps",
            required = false
    )
    public String batchappsPath;

    private final BasicExecutionContext context = new BasicExecutionContext()
            .withEnvironmentVariables(m -> m.putAll(System.getenv()));

    /**
     * Returns the execution context.
     * @return the execution context
     */
    public ExecutionContext getExecutionContext() {
        if (batchappsPath != null) {
            context.withEnvironmentVariables(m -> m.put(TaskExecutors.ENV_BATCHAPPS_PATH, batchappsPath));
        }
        return context;
    }
}
