/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.flowlog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.PhaseMonitor;

/**
 * An implementation of {@link PhaseMonitor} to save log for each jobflow.
 * @since 0.2.6
 */
public class FlowLogger extends PhaseMonitor {

    static final Logger LOG = LoggerFactory.getLogger(FlowLogger.class);

    private final ExecutionContext context;

    private final String label;

    private final double stepUnit;

    private double totalTaskSize;

    private double workedTaskSize;

    private int workedStep = 0;

    private boolean opened;

    private boolean closed;

    private final File file;

    private final File escapeFile;

    private PrintWriter writer;

    private final boolean teeLog;

    private final boolean reportJob;

    private final boolean deleteOnCleanup;

    private JobStatus worstStatus;

    private Throwable occurredException;

    private final DateFormat dateFormat;

    private final Charset encoding;

    private final boolean deleteOnSetup;

    /**
     * Creates a new instance.
     * @param context current context
     * @param profile the profile for this logger
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowLogger(ExecutionContext context, FlowLoggerProfile profile) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.label = MessageFormat.format("{0}|{1}|{3}@{2}",
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase());
        if (profile.getStepUnit() <= 0) {
            this.stepUnit = Double.MAX_VALUE;
        } else {
            this.stepUnit = Math.max(profile.getStepUnit(), 0.01) - 0.0000000000001;
        }
        this.file = profile.getLogFile(context);
        this.escapeFile = profile.getEscapeFile(context);
        this.encoding = profile.getEncoding();
        this.dateFormat = profile.getDateFormat();
        this.teeLog = profile.isTeeLog();
        this.reportJob = profile.isReportJob();
        this.deleteOnSetup = profile.isDeleteOnSetup();
        this.deleteOnCleanup = profile.isDeleteOnCleanup();
        this.worstStatus = JobStatus.SUCCESS;
        this.occurredException = null;
    }

    @Override
    public synchronized void open(double taskSize) throws IOException {
        if (opened) {
            throw new IllegalStateException(MessageFormat.format(
                    "Monitor is already opened: {0}",
                    label));
        }
        opened = true;
        File parent = file.getParentFile();
        if (parent != null && parent.mkdirs() == false) {
            if (parent.isDirectory() == false) {
                LOG.warn(MessageFormat.format(
                        "Failed to create a parent directory for flow logger : {1} ({0})",
                        label,
                        parent));
            }
        }
        boolean keepLogs = deleteOnSetup == false || context.getPhase() != ExecutionPhase.SETUP;
        if (keepLogs == false && escapeFile.exists()) {
            // TODO logging
            LOG.info(MessageFormat.format(
                    "Deleting the last escaped log: {1} ({0})",
                    label,
                    escapeFile.getAbsolutePath()));
            if (escapeFile.delete() == false && escapeFile.exists()) {
                // TODO logging
                LOG.info(MessageFormat.format(
                        "Failed to delete the last escaped log: {1} ({0})",
                        label,
                        escapeFile.getAbsolutePath()));
            }
        }
        OutputStream output = new FileOutputStream(file, keepLogs);
        boolean succeed = false;
        try {
            Writer w = new OutputStreamWriter(output, encoding);
            this.writer = new PrintWriter(new BufferedWriter(w));
            succeed = true;
        } finally {
            if (succeed == false) {
                output.close();
            }
        }
        this.totalTaskSize = taskSize;
        record(MessageFormat.format(
                "START {1} ({0})",
                label,
                context.getPhase()));
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
        if (reportJob) {
            record(MessageFormat.format(
                    "START JOB - {1}/{0}",
                    label,
                    jobId));
        }
    }

    @Override
    protected void onJobMonitorClosed(String jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        if (reportJob) {
            record(MessageFormat.format(
                    "END JOB - {1}/{0}",
                    label,
                    jobId));
        }
    }

    @Override
    public void reportJobStatus(String jobId, JobStatus status, Throwable cause) throws IOException {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        if (reportJob || status != JobStatus.SUCCESS) {
            record(MessageFormat.format(
                    "{2} JOB - {1}/{0}",
                    label,
                    jobId,
                    status));
        }
        if (cause != null) {
            record(cause);
        }
        if (status.compareTo(worstStatus) > 0) {
            worstStatus = status;
        }
        if (cause != null && occurredException == null) {
            occurredException = cause;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            if (writer != null) {
                writer.close();
                writer = null;
            }
            return;
        }
        closed = true;
        set(totalTaskSize);
        record(MessageFormat.format(
                "{1} {2} ({0})",
                label,
                worstStatus,
                context.getPhase()));
        if (occurredException != null) {
            record(occurredException);
        }
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (context.getPhase() == ExecutionPhase.CLEANUP && worstStatus == JobStatus.SUCCESS) {
            if (deleteOnCleanup) {
                // TODO logging
                LOG.info(MessageFormat.format(
                        "Deleting a suceeded flow log: {1} ({0})",
                        label,
                        file.getAbsolutePath()));
                if (file.delete() == false) {
                    // TODO logging
                    LOG.warn(MessageFormat.format(
                            "Failed to delete flow log: {0}",
                            file.getAbsolutePath()));
                }
            } else {
                File parent = escapeFile.getParentFile();
                if (parent.mkdirs() == false) {
                    if (parent.isDirectory() == false) {
                        // TODO logging
                        LOG.warn(MessageFormat.format(
                                "Failed to create a parent directory for flow logger : {1} ({0})",
                                label,
                                parent));
                    }
                }
                // TODO logging
                LOG.info(MessageFormat.format(
                        "Escaping a suceeded flow log: {1} -> {2} ({0})",
                        label,
                        file.getAbsolutePath(),
                        escapeFile.getAbsolutePath()));
                file.renameTo(escapeFile);
            }
        }
    }

    private void set(double workedSize) {
        double normalized = Math.max(0, Math.min(totalTaskSize, workedSize));
        double relative = normalized / totalTaskSize;
        int step = (int) Math.floor(relative / stepUnit);
        if (step != workedStep && closed == false) {
            record(MessageFormat.format(
                    "STEP [{1}%] - ({0})",
                    label,
                    String.format("%.02f", relative * 100)));
        }
        this.workedTaskSize = normalized;
        this.workedStep = step;
    }

    private synchronized void record(String message) {
        assert message != null;
        writer.printf("%s %s%n", now(), message);
        writer.flush();
        if (teeLog) {
            LOG.info(message);
        }
    }

    private synchronized void record(Throwable exception) {
        assert exception != null;
        writer.println(MessageFormat.format(
                "{0} Exception occured in {1}",
                now(),
                label));
        exception.printStackTrace(writer);
        writer.flush();
        if (teeLog) {
            LOG.error(MessageFormat.format(
                    "Exception occured in {0}",
                    label), exception);
        }
    }

    private String now() {
        return dateFormat.format(new Date());
    }
}
