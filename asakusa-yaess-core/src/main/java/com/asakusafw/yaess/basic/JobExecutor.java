/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.Job;

/**
 * An abstract super interface of {@link Job} executor.
 * @since 0.2.3
 */
public interface JobExecutor {

    /**
     * Submits a job.
     * @param monitor an execution monitor
     * @param context current context
     * @param job target job
     * @param doneQueue the queue where the done tasks are put
     * @return an execution object related to the target job
     * @throws InterruptedException if submittion is interrupted
     * @throws IOException if failed to submit the target job
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    Executing submit(
            ExecutionMonitor monitor,
            ExecutionContext context,
            Job job,
            BlockingQueue<Executing> doneQueue) throws InterruptedException, IOException;

    /**
     * An abstraction of job execution.
     * @since 0.2.3
     */
    public final class Executing extends FutureTask<Void> {

        static final Logger LOG = LoggerFactory.getLogger(JobExecutor.class);

        private final Job job;

        private final BlockingQueue<Executing> doneQueue;

        /**
         * Creates a new instance.
         * @param monitor execution monitor
         * @param context current context
         * @param job target job
         * @param doneQueue a queue which is notified on job is done (nullable)
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Executing(
                ExecutionMonitor monitor,
                ExecutionContext context,
                Job job,
                BlockingQueue<Executing> doneQueue) {
            super(build(monitor, context, job));
            this.job = job;
            this.doneQueue = doneQueue;
        }

        private static Callable<Void> build(
                final ExecutionMonitor monitor,
                final ExecutionContext context,
                final Job job) {
            if (monitor == null) {
                throw new IllegalArgumentException("monitor must not be null"); //$NON-NLS-1$
            }
            if (context == null) {
                throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
            }
            if (job == null) {
                throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
            }
            return new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    LOG.debug("Starting job: {} in {}", job.getId(), Thread.currentThread().getName());
                    job.execute(monitor, context);
                    LOG.debug("Completing job: {} in {}", job.getId(), Thread.currentThread().getName());
                    return null;
                }
            };
        }

        /**
         * Returns the target job.
         * @return the job
         */
        public Job getJob() {
            return job;
        }

        @Override
        protected void done() {
            if (doneQueue != null) {
                doneQueue.add(this);
            }
        }
    }
}
