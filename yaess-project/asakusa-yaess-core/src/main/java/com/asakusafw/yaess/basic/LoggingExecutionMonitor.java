/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.PhaseMonitor;

/**
 * An implementation of {@link ExecutionMonitor} using default logger.
 * @since 0.2.3
 */
public class LoggingExecutionMonitor extends PhaseMonitor {

    static final Logger LOG = LoggerFactory.getLogger(LoggingExecutionMonitor.class);

    private final String label;

    private final double stepUnit;

    private double totalTaskSize;

    private double workedTaskSize;

    private int workedStep = 0;

    private boolean opened;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param context current context
     * @param stepUnit report unit for each progress size (0.0~1.0)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public LoggingExecutionMonitor(ExecutionContext context, double stepUnit) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.label = MessageFormat.format("{0}|{1}|{3}@{2}",
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase());
        if (stepUnit <= 0) {
            this.stepUnit = Double.MAX_VALUE;
        } else {
            this.stepUnit = Math.max(stepUnit, 0.01) - 0.0000000000001;
        }
    }

    @Override
    public synchronized void open(double taskSize) {
        if (opened) {
            throw new IllegalStateException(MessageFormat.format(
                    "Monitor is already opened: {0}",
                    label));
        }
        opened = true;
        this.totalTaskSize = taskSize;
        LOG.info(MessageFormat.format(
                "START PHASE - {0}",
                label));
    }

    @Override
    public synchronized void progressed(double deltaSize) {
        set(workedTaskSize + deltaSize);
    }

    @Override
    public synchronized void setProgress(double workedSize) {
        set(workedSize);
    }

    @Override
    protected void onJobMonitorOpened(String jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        LOG.info(MessageFormat.format(
                "START JOB - {1}/{0}",
                label,
                jobId));
    }

    @Override
    protected void onJobMonitorClosed(String jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        LOG.info(MessageFormat.format(
                "END JOB - {1}/{0}",
                label,
                jobId));
    }

    @Override
    public void reportJobStatus(String jobId, JobStatus status, Throwable cause) throws IOException {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        LOG.info(MessageFormat.format(
                "{2} JOB - {1}/{0}",
                label,
                jobId,
                status));
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        set(totalTaskSize);
        LOG.info(MessageFormat.format(
                "END PHASE - {0}",
                label));
    }

    private void set(double workedSize) {
        double normalized = Math.max(0, Math.min(totalTaskSize, workedSize));
        double relative = normalized / totalTaskSize;
        int step = (int) Math.floor(relative / stepUnit);
        if (step != workedStep && closed == false) {
            LOG.info(MessageFormat.format(
                    "STEP - {0} [{1}%]",
                    label,
                    String.format("%.02f", relative * 100)));
        }
        this.workedTaskSize = normalized;
        this.workedStep = step;
    }
}
