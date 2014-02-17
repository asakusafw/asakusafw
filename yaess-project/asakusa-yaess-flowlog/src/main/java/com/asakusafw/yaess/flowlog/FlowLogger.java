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
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.YaessLogger;

/**
 * An implementation of {@link PhaseMonitor} to save log for each jobflow.
 * @since 0.2.6
 */
public class FlowLogger extends PhaseMonitor {

    static final YaessLogger YSLOG = new YaessFlowLogLogger(FlowLogger.class);

    static final Logger LOG = LoggerFactory.getLogger(FlowLogger.class);

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.asakusafw.yaess.flowlog.flowlog");

    private static final double MIN_STEP_UNIT = 0.01;

    private static final double DELTA_STEP_UNIT = 0.0000000000001;

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
            this.stepUnit = Math.max(profile.getStepUnit(), MIN_STEP_UNIT) - DELTA_STEP_UNIT;
        }
        this.file = profile.getLogFile(context);
        this.escapeFile = profile.getEscapeFile(context);
        this.encoding = profile.getEncoding();
        this.dateFormat = profile.getDateFormat();
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
        prepareParentDirectory(file);
        boolean keepLogs = deleteOnSetup == false || context.getPhase() != ExecutionPhase.SETUP;
        if (keepLogs == false) {
            cleanEscapedLog();
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
        record(Level.INFO, Target.PHASE, Trigger.START);
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
        record(Level.INFO, Target.JOB, Trigger.START, jobId);
    }

    @Override
    protected void onJobMonitorClosed(String jobId) {
        return;
    }

    @Override
    public void reportJobStatus(String jobId, JobStatus status, Throwable cause) throws IOException {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        record(cause, toLevel(status), Target.JOB, Trigger.FINISH, jobId, status);
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
        record(occurredException, toLevel(worstStatus), Target.PHASE, Trigger.FINISH, worstStatus);
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (context.getPhase() == ExecutionPhase.CLEANUP && worstStatus == JobStatus.SUCCESS) {
            if (deleteOnCleanup) {
                cleanCurrentLog();
            } else {
                cleanEscapedLog();
                escapeCurrentLog();
            }
        }
    }

    private void prepareParentDirectory(File f) {
        assert f != null;
        File parent = f.getParentFile();
        if (parent.mkdirs() == false) {
            if (parent.isDirectory() == false) {
                YSLOG.warn("W01001",
                        label,
                        parent.getAbsolutePath());
            }
        }
    }

    private void cleanCurrentLog() {
        if (file.exists()) {
            YSLOG.info("I01002",
                    label,
                    file.getAbsolutePath());
            if (file.delete() == false && file.exists()) {
                YSLOG.warn("W01003",
                        label,
                        file.getAbsolutePath());
            }
        }
    }

    private void cleanEscapedLog() {
        if (escapeFile.exists()) {
            YSLOG.info("I01001",
                    label,
                    escapeFile.getAbsolutePath());
            if (escapeFile.delete() == false && escapeFile.exists()) {
                YSLOG.warn("W01002",
                        label,
                        escapeFile.getAbsolutePath());
            }
        }
    }

    private void escapeCurrentLog() {
        YSLOG.info("I01003",
                label,
                file.getAbsolutePath(),
                escapeFile.getAbsolutePath());
        prepareParentDirectory(escapeFile);
        if (file.renameTo(escapeFile) == false) {
            YSLOG.warn("W01004",
                    label,
                    file.getAbsolutePath(),
                    escapeFile.getAbsolutePath());
        }
    }

    private void set(double workedSize) {
        double normalized = Math.max(0, Math.min(totalTaskSize, workedSize));
        double relative = normalized / totalTaskSize;
        int step = (int) Math.floor(relative / stepUnit);
        if (step != workedStep && closed == false) {
            record(Level.INFO, Target.PHASE, Trigger.STEP, String.format("%.02f", relative * 100));
        }
        this.workedTaskSize = normalized;
        this.workedStep = step;
    }

    private Level toLevel(JobStatus status) {
        assert status != null;
        switch (status) {
        case SUCCESS:
            return Level.INFO;
        case FAILED:
            return Level.ERROR;
        case CANCELLED:
            return Level.ERROR;
        default:
            throw new AssertionError(status);
        }
    }

    private void record(Level level, Target target, Trigger trigger, Object... arguments) {
        record(null, level, target, trigger, arguments);
    }

    private synchronized void record(Throwable t, Level level, Target target, Trigger trigger, Object... arguments) {
        if (target == Target.JOB && reportJob == false) {
            return;
        }
        String pattern = BUNDLE.getString(target.name() + trigger.name());
        String message = MessageFormat.format(pattern, arguments);
        String record = MessageFormat.format(
                "{0} [{1}:{2}-{3}-{4}] {5} (batchId={6}, flowId={7}, executionId={8}, phase={9})",
                now(),
                level,
                trigger,
                context.getPhase().name(),
                target,
                message,
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase());
        record(t, record);
    }

    private synchronized void record(Throwable exception, String message) {
        LOG.debug(message, exception);
        writer.println(message);
        if (exception != null) {
            exception.printStackTrace(writer);
        }
        writer.flush();
    }

    private String now() {
        return dateFormat.format(new Date());
    }

    private enum Level {

        INFO,

        WARN,

        ERROR,
    }

    private enum Target {

        PHASE,

        JOB,
    }

    private enum Trigger {

        START,

        STEP,

        FINISH,
    }
}
