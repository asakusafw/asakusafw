/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.IOException;

import com.asakusafw.workflow.model.JobflowInfo;

/**
 * Executes jobflows.
 * @since 0.10.0
 */
public interface JobflowExecutor {

    /**
     * Executes the given jobflow.
     * @param context the current context
     * @param jobflow target jobflow
     * @throws IOException if failed to create job processes
     * @throws InterruptedException if interrupted while running tasks
     */
    void execute(TaskExecutionContext context, JobflowInfo jobflow) throws IOException, InterruptedException;
}
