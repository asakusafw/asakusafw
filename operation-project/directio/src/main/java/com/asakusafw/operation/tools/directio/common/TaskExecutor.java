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
package com.asakusafw.operation.tools.directio.common;

import java.util.concurrent.ForkJoinTask;

/**
 * Executes {@link Task}.
 * @since 0.10.0
 */
public enum TaskExecutor implements Task.Context {

    /**
     * Executes tasks on the current thread.
     */
    DEFAULT {
        @Override
        public Task.Wait submit(Task task) {
            return () -> task.execute(this);
        }
    },

    /**
     * Executes tasks on the system fork-join pool.
     */
    FORK_JOIN {
        @Override
        public Task.Wait submit(Task task) {
            ForkJoinTask<?> wrapped = ForkJoinTask.adapt(() -> task.execute(this)).fork();
            return () -> wrapped.join();
        }
    },
}
