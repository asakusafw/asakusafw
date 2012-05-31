/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.util.Set;

/**
 * Represents an atomic job for YAESS.
 * @since 0.2.3
 * @version 0.2.6
 */
public abstract class Job {

    static final YaessLogger YSLOG = new YaessCoreLogger(Job.class);

    /**
     * Executes this job.
     * @param monitor a progress monitor for this job
     * @param context current execution context
     * @throws InterruptedException if this execution is interrupted
     * @throws IOException if failed to execute this job
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.6
     */
    public final void launch(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        if (monitor == null) {
            throw new IllegalArgumentException("monitor must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        YSLOG.info("I04000",
                context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase(),
                getJobLabel(), getServiceLabel());
        long start = System.currentTimeMillis();
        try {
            execute(monitor, context);
            YSLOG.info("I04001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase(),
                    getJobLabel(), getServiceLabel());
        } catch (IOException e) {
            YSLOG.error(e, "E04001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase(),
                    getJobLabel(), getServiceLabel());
            throw e;
        } catch (InterruptedException e) {
            YSLOG.warn(e, "W04001",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase(),
                    getJobLabel(), getServiceLabel());
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            YSLOG.info("I04999",
                    context.getBatchId(), context.getFlowId(), context.getExecutionId(), context.getPhase(),
                    getJobLabel(), getServiceLabel(), end - start);
        }
    }

    /**
     * Executes this job.
     * @param monitor a progress monitor for this job
     * @param context current execution context
     * @throws InterruptedException if this execution is interrupted
     * @throws IOException if failed to execute this job
     */
    protected abstract void execute(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException;

    /**
     * Returns the target job label.
     * @return the target job label
     */
    public abstract String getJobLabel();

    /**
     * Returns the service label which actually executes this job.
     * @return the service label
     */
    public abstract String getServiceLabel();

    /**
     * Returns the ID of this job for other succeeding jobs.
     * @return ID of this job
     */
    public abstract String getId();

    /**
     * Returns job IDs which blocks executing this job.
     * Any schedulers can execute this job only if all blocker jobs are completed.
     * @return the blocker job IDs
     */
    public abstract Set<String> getBlockerIds();

    /**
     * Returns the resource ID which this job requires in execution.
     * @param context current execution context
     * @return the resource ID required in this job
     * @throws InterruptedException if this execution is interrupted
     * @throws IOException if failed to execute this job
     */
    public abstract String getResourceId(ExecutionContext context) throws InterruptedException, IOException;
}
