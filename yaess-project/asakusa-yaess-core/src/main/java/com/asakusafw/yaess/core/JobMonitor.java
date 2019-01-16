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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Receives job's progress.
 * @since 0.2.3
 */
public class JobMonitor implements ExecutionMonitor {

    private final PhaseMonitor parent;

    private final String jobId;

    private final double taskSizeInParent;

    private double currentTaskSize;

    private double currentProgress;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param parent the parent monitor
     * @param jobId target job ID
     * @param taskSizeInParent total task size in the parent for this monitor
     * @throws IllegalArgumentException if {@code parent} is {@code null}
     */
    public JobMonitor(PhaseMonitor parent, String jobId, double taskSizeInParent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent must not be null"); //$NON-NLS-1$
        }
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        this.parent = parent;
        this.jobId = jobId;
        this.taskSizeInParent = taskSizeInParent;
    }

    @Override
    public void checkCancelled() throws InterruptedException {
        parent.checkCancelled();
    }

    /**
     * Begins a task and notify this event to the corresponding receiver.
     * This method can be invoked once for each object.
     * @param taskSize the total task size
     * @throws IOException if failed to notify this event
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @Override
    public synchronized void open(double taskSize) throws IOException {
        if (taskSize <= 0) {
            throw new IllegalArgumentException("taskSize must be > 0"); //$NON-NLS-1$
        }
        this.currentTaskSize = taskSize;
        parent.onJobMonitorOpened(jobId);
    }

    @Override
    public synchronized void progressed(double size) throws IOException {
        double nextProgress = currentProgress + size;
        changeCurrentProgress(nextProgress);
    }

    @Override
    public synchronized void setProgress(double workedSize) throws IOException {
        changeCurrentProgress(workedSize);
    }

    @Override
    public OutputStream getOutput() throws IOException {
        return parent.getJobOutput(jobId);
    }

    @Override
    public synchronized void close() throws IOException {
        changeCurrentProgress(currentTaskSize);
        parent.onJobMonitorClosed(jobId);
    }

    private void changeCurrentProgress(double nextProgress) throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        if (taskSizeInParent == 0) {
            return;
        }
        double normalized = Math.max(0, Math.min(currentTaskSize, nextProgress));
        assert 0 <= normalized && normalized <= currentTaskSize;
        double delta = normalized - currentProgress;
        double deltaInParent = delta / taskSizeInParent;
        currentProgress = normalized;
        if (deltaInParent != 0) {
            parent.progressed(deltaInParent);
        }
    }
}
