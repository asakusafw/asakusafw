/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * A structured profile for {@link FlowLoggerProvider}.
 * @since 0.2.6
 */
public class FlowLoggerProfile {

    static final String KEY_DIRECTORY = "directory";

    static final String KEY_ENCODING = "encoding";

    static final String KEY_STEP_UNIT = "stepUnit";

    static final String KEY_DATE_FORMAT = "dateFormat";

    static final String KEY_REPORT_JOB = "reportJob";

    static final String KEY_DELETE_ON_SETUP = "deleteOnSetup";

    static final String KEY_DELETE_ON_CLEANUP = "deleteOnCleanup";

    static final String DEFAULT_ENCODING = "UTF-8";

    static final String DEFAULT_STEP_UNIT = "0.00";

    static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static final String DEFAULT_REPORT_JOB = "true";

    static final String DEFAULT_DELETE_ON_SETUP = "true";

    static final String DEFAULT_DELETE_ON_CLEANUP = "true";

    private final File directory;

    private final Charset encoding;

    private final DateFormat dateFormat;

    private final double stepUnit;

    private final boolean reportJob;

    private final boolean deleteOnSetup;

    private final boolean deleteOnCleanup;

    FlowLoggerProfile(
            File directory,
            Charset encoding,
            DateFormat dateFormat,
            double stepUnit,
            boolean reportJob,
            boolean deleteOnSetup,
            boolean deleteOnCleanup) {
        if (directory == null) {
            throw new IllegalArgumentException("directory must not be null"); //$NON-NLS-1$
        }
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null"); //$NON-NLS-1$
        }
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat must not be null"); //$NON-NLS-1$
        }
        this.directory = directory;
        this.encoding = encoding;
        this.dateFormat = dateFormat;
        this.stepUnit = stepUnit;
        this.reportJob = reportJob;
        this.deleteOnSetup = deleteOnSetup;
        this.deleteOnCleanup = deleteOnCleanup;
    }

    /**
     * Returns the base directory of log output target.
     * @return the output base directory
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Returns the log output encoding.
     * @return the log output encoding
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * Returns the log date format.
     * @return the log date format
     */
    public DateFormat getDateFormat() {
        return (DateFormat) dateFormat.clone();
    }

    /**
     * Returns the step unit.
     * @return the step unit
     */
    public double getStepUnit() {
        return stepUnit;
    }

    /**
     * Returns whether report job status.
     * @return {@code true} to report job status, otherwise {@code false}
     */
    public boolean isReportJob() {
        return reportJob;
    }

    /**
     * Returns whether delete logs before cleanup phase was started.
     * @return {@code true} to delete logs, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean isDeleteOnSetup() {
        return deleteOnSetup;
    }

    /**
     * Returns whether delete logs after cleanup phase was succeeded.
     * @return {@code true} to delete logs, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean isDeleteOnCleanup() {
        return deleteOnCleanup;
    }

    /**
     * Returns abstract path for the target log file.
     * @param context current context
     * @return target log file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File getLogFile(ExecutionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return getFile(context, "logs");
    }

    /**
     * Returns abstract path for the escaped log file.
     * @param context current context
     * @return escaped log file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File getEscapeFile(ExecutionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return getFile(context, "cleanup");
    }

    private File getFile(ExecutionContext context, String dirName) {
        assert context != null;
        assert dirName != null;
        File base = new File(getDirectory(), context.getBatchId());
        File ongoing = new File(base, dirName);
        File log = new File(ongoing, context.getFlowId());
        return log;
    }

    /**
     * Converts the generic profile into the structured profile about {@link FlowLogger}.
     * @param profile the target profile
     * @return the converted profile
     * @throws IllegalArgumentException if some parameters were invalid
     */
    public static FlowLoggerProfile convert(ServiceProfile<?> profile) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        Map<String, String> copy = new TreeMap<String, String>(profile.getConfiguration());
        String dirString = extract(profile, copy, KEY_DIRECTORY, null);
        String encString = extract(profile, copy, KEY_ENCODING, DEFAULT_ENCODING);
        String dfString = extract(profile, copy, KEY_DATE_FORMAT, DEFAULT_DATE_FORMAT);
        double stepUnit = extractDouble(profile, copy, KEY_STEP_UNIT, DEFAULT_STEP_UNIT);
        boolean reportJob = extractBoolean(profile, copy, KEY_REPORT_JOB, DEFAULT_REPORT_JOB);
        boolean deleteOnSetup = extractBoolean(profile, copy, KEY_DELETE_ON_SETUP, DEFAULT_DELETE_ON_SETUP);
        boolean deleteOnCleanup = extractBoolean(profile, copy, KEY_DELETE_ON_CLEANUP, DEFAULT_DELETE_ON_CLEANUP);
        if (copy.isEmpty() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unknown profile parameters in {0} ({1})",
                    profile.getPrefix(),
                    copy));
        }
        File file = new File(dirString);
        Charset encoding;
        try {
            encoding = Charset.forName(encString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid charset in {0}.{1} ({2})",
                    profile.getPrefix(),
                    KEY_ENCODING,
                    encString), e);
        }
        DateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(dfString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid date format in {0}.{1} ({2})",
                    profile.getPrefix(),
                    KEY_DATE_FORMAT,
                    dfString), e);
        }
        return new FlowLoggerProfile(
                file, encoding, dateFormat,
                stepUnit,
                reportJob,
                deleteOnSetup, deleteOnCleanup);
    }

    private static String extract(
            ServiceProfile<?> profile,
            Map<String, String> copy,
            String key,
            String defaultValue) {
        String value = profile.normalize(key, copy.remove(key), defaultValue == null, true);
        if (value == null) {
            assert defaultValue != null;
            return defaultValue;
        }
        return value;
    }

    private static double extractDouble(
            ServiceProfile<?> profile,
            Map<String, String> copy,
            String key,
            String defaultValue) {
        String value = extract(profile, copy, key, defaultValue);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0}.{1} must be a floating point number ({2})",
                    profile.getPrefix(),
                    key,
                    value), e);
        }
    }

    private static boolean extractBoolean(
            ServiceProfile<?> profile,
            Map<String, String> copy,
            String key,
            String defaultValue) {
        String value = extract(profile, copy, key, defaultValue);
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0}.{1} must be a boolean value ({2})",
                    profile.getPrefix(),
                    key,
                    value));
        }
    }
}
