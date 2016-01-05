/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.Job;

/**
 * An implementation of {@link JobExecutor} using a shared {@link ExecutorService}.
 * @since 0.2.3
 */
public class ThreadedJobExecutor implements JobExecutor {

    private final Executor executor;

    /**
     * Creates a new instance.
     * @param executor thread manager
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ThreadedJobExecutor(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null"); //$NON-NLS-1$
        }
        this.executor = executor;
    }

    @Override
    public Executing submit(
            ExecutionMonitor monitor,
            ExecutionContext context,
            Job job,
            BlockingQueue<Executing> doneQueue) throws InterruptedException, IOException {
        if (monitor == null) {
            throw new IllegalArgumentException("monitor must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        Executing executing = new Executing(monitor, context, job, doneQueue);
        executor.execute(executing);
        return executing;
    }
}
