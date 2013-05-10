/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.io.OutputStream;

/**
 * Receives phase execution's progress.
 * @since 0.2.3
 */
public abstract class PhaseMonitor implements ExecutionMonitor {

    /**
     * An empty implementation.
     */
    public static final PhaseMonitor NULL = new PhaseMonitor() {

        @Override
        public void progressed(double deltaSize) {
            return;
        }

        @Override
        public void setProgress(double workedSize) throws IOException {
            return;
        }

        @Override
        public void open(double taskSize) {
            return;
        }

        @Override
        public void reportJobStatus(String jobId, JobStatus status, Throwable cause) {
            return;
        }

        @Override
        public void close() {
            return;
        }
    };

    @Override
    public void checkCancelled() throws InterruptedException {
        if (Thread.interrupted() || isCancelRequested()) {
            throw new InterruptedException();
        }
    }

    /**
     * Returns whether this phase execution has been requested cancel.
     * Default implementation always returns {@code false}.
     * @return {@code true} to cancel requested, {@code false} otherwise
     */
    protected boolean isCancelRequested() {
        return false;
    }

    @Override
    public OutputStream getOutput() throws IOException {
        return System.out;
    }

    /**
     * Creates a new child task monitor with the specified task size.
     * @param jobId target job ID
     * @param childTaskSize task size in this monitor to assign to the child monitor
     * @return the created monitor
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExecutionMonitor createJobMonitor(String jobId, double childTaskSize) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        return new JobMonitor(this, jobId, childTaskSize);
    }

    /**
     * Reports job status about target job ID.
     * @param jobId target job ID
     * @param status the job status
     * @param cause occurred exception (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @throws IOException if failed to notify this event
     */
    public abstract void reportJobStatus(
            String jobId,
            JobStatus status,
            Throwable cause) throws IOException;

    /**
     * Notifies the job was opened.
     * The default implementation does nothing.
     * @param jobId target job ID
     * @throws IOException if failed to notify this event
     */
    protected void onJobMonitorOpened(String jobId) throws IOException {
        return;
    }

    /**
     * Returns an output stream for jobs.
     * The default implementation returns the {@link #getOutput() this.getOutput()}.
     * @param jobId target job ID
     * @return the target stream
     * @throws IOException if failed to obtain output
     */
    protected OutputStream getJobOutput(String jobId) throws IOException {
        return getOutput();
    }

    /**
     * Notifies the job was closed.
     * The default implementation does nothing.
     * @param jobId target job ID
     * @throws IOException if failed to notify this event
     */
    protected void onJobMonitorClosed(String jobId) throws IOException {
        return;
    }

    /**
     * Job statuses.
     * @since 0.2.3
     */
    public enum JobStatus {

        /**
         * Job was succeeded.
         */
        SUCCESS,

        /**
         * Job was cancelled.
         */
        CANCELLED,

        /**
         * Job was failed.
         */
        FAILED,
    }
}
