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
package com.asakusafw.workflow.executor;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.asakusafw.workflow.model.BatchInfo;

/**
 * Executes batches.
 * @since 0.10.0
 */
@FunctionalInterface
public interface BatchExecutor {

    /**
     * Executes the given batch.
     * @param context the current context
     * @param batch the target batch
     * @param arguments the batch arguments
     * @throws IOException if failed to create job processes
     * @throws InterruptedException if interrupted while running tasks
     */
    void execute(
            ExecutionContext context,
            BatchInfo batch, Map<String, String> arguments) throws IOException, InterruptedException;

    /**
     * Executes the given batch without any batch arguments.
     * @param context the current context
     * @param batch the target batch
     * @throws IOException if failed to create job processes
     * @throws InterruptedException if interrupted while running tasks
     * @see #execute(ExecutionContext, BatchInfo, Map)
     */
    default void execute(ExecutionContext context, BatchInfo batch) throws IOException, InterruptedException {
        execute(context, batch, Collections.emptyMap());
    }
}
