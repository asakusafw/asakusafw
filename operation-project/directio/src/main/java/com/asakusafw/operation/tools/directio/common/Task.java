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
package com.asakusafw.operation.tools.directio.common;

import java.util.concurrent.CancellationException;

/**
 * An abstract super interface of tasks.
 * @since 0.10.0
 */
@FunctionalInterface
public interface Task {

    /**
     * Executes this task.
     * @param context the task context
     */
    void execute(Context context);

    /**
     * The task context.
     * @since 0.10.0
     */
    @FunctionalInterface
    interface Context {

        /**
         * Submits the given task.
         * @param task the task
         * @return a waiting object for task completion
         */
        Wait submit(Task task);
    }

    /**
     * A waiting object for task completion.
     * @since 0.10.0
     */
    @FunctionalInterface
    interface Wait {

        /**
         * Waits for the task completion.
         * @throws CancellationException if the task was cancelled
         */
        void forDone();
    }
}
