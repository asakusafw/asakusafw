/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.workflow.executor;

import java.util.Map;

/**
 * Represents a context of {@link TaskExecutor}.
 * @since 0.10.0
 */
public interface TaskExecutionContext extends ExecutionContext {

    /**
     * Returns the container batch information.
     * @return the container batch information
     */
    String getBatchId();

    /**
     * Returns the container jobflow information.
     * @return the container jobflow information
     */
    String getFlowId();

    /**
     * Returns the current execution ID.
     * @return the current execution ID
     */
    String getExecutionId();

    /**
     * Returns the batch arguments.
     * @return the batch arguments
     */
    Map<String, String> getBatchArguments();
}