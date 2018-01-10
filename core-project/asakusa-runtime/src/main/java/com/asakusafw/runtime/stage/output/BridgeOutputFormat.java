/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus.State;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.util.Progressable;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceConstants;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.directio.hadoop.ProgressableCounter;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.util.VariableTable;

/**
 * A bridge implementation for Hadoop {@link OutputFormat}.
 * @since 0.2.5
 * @version 0.9.0
 */
public final class BridgeOutputFormat extends OutputFormat<Object, Object> {

    static final Log LOG = LogFactory.getLog(BridgeOutputFormat.class);

    private static final Charset ASCII = StandardCharsets.US_ASCII;

    private static final long SERIAL_VERSION = 1;

    private static final String KEY = "com.asakusafw.output.bridge"; //$NON-NLS-1$

    private final Map<TaskAttemptID, OutputCommitter> commiterCache = new WeakHashMap<>();

    /**
     * Returns whether this stage has an output corresponding this format.
     * @param context current context
     * @return {@code true} if such output exists, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static boolean hasOutput(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return context.getConfiguration().getRaw(KEY) != null;
    }

    /**
     * Sets current output information into the current context.
     * @param context current context
     * @param outputList output information to be set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void set(JobContext context, List<StageOutput> outputList) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (outputList == null) {
            throw new IllegalArgumentException("outputList must not be null"); //$NON-NLS-1$
        }
        List<OutputSpec> specs = new ArrayList<>();
        for (StageOutput output : outputList) {
            List<String> deletePatterns = getDeletePatterns(output);
            OutputSpec spec = new OutputSpec(output.getName(), deletePatterns);
            specs.add(spec);
        }
        save(context.getConfiguration(), specs);
    }

    private static List<String> getDeletePatterns(StageOutput output) {
        assert output != null;
        List<String> results = new ArrayList<>();
        for (Map.Entry<String, String> entry : output.getAttributes().entrySet()) {
            if (entry.getKey().startsWith(DirectDataSourceConstants.PREFIX_DELETE_PATTERN)) {
                String rawDeletePattern = entry.getValue();
                results.add(rawDeletePattern);
            }
        }
        return results;
    }

    private static void save(Configuration conf, List<OutputSpec> specs) {
        assert conf != null;
        assert specs != null;
        for (OutputSpec spec : specs) {
            if (spec.resolved) {
                throw new IllegalStateException();
            }
        }
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(new GZIPOutputStream(new Base64OutputStream(sink)))) {
            WritableUtils.writeVLong(output, SERIAL_VERSION);
            WritableUtils.writeVInt(output, specs.size());
            for (OutputSpec spec : specs) {
                WritableUtils.writeString(output, spec.basePath);
                WritableUtils.writeVInt(output, spec.deletePatterns.size());
                for (String pattern : spec.deletePatterns) {
                    WritableUtils.writeString(output, pattern);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        conf.set(KEY, new String(sink.toByteArray(), ASCII));
    }

    private static List<OutputSpec> getSpecs(JobContext context) {
        assert context != null;
        String encoded = context.getConfiguration().getRaw(KEY);
        if (encoded == null) {
            return Collections.emptyList();
        }
        VariableTable table = getVariableTable(context);
        try {
            ByteArrayInputStream source = new ByteArrayInputStream(encoded.getBytes(ASCII));
            DataInputStream input = new DataInputStream(new GZIPInputStream(new Base64InputStream(source)));
            long version = WritableUtils.readVLong(input);
            if (version != SERIAL_VERSION) {
                throw new IOException(MessageFormat.format(
                        "Invalid StageOutput version: framework={0}, saw={1}",
                        SERIAL_VERSION,
                        version));
            }
            List<OutputSpec> results = new ArrayList<>();
            int specCount = WritableUtils.readVInt(input);
            for (int specIndex = 0; specIndex < specCount; specIndex++) {
                String basePath = WritableUtils.readString(input);
                try {
                    basePath = table.parse(basePath);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Invalid basePath: {0}",
                            basePath), e);
                }
                int patternCount = WritableUtils.readVInt(input);
                List<String> patterns = new ArrayList<>();
                for (int patternIndex = 0; patternIndex < patternCount; patternIndex++) {
                    String pattern = WritableUtils.readString(input);
                    try {
                        pattern = table.parse(pattern);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalStateException(MessageFormat.format(
                                "Invalid delete pattern: {0}",
                                pattern), e);
                    }
                    patterns.add(pattern);
                }
                results.add(new OutputSpec(basePath, patterns, true));
            }
            return results;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DirectDataSourceRepository getDataSourceRepository(JobContext context) {
        assert context != null;
        return HadoopDataSourceUtil.loadRepository(context.getConfiguration());
    }

    /**
     * Creates output context from Hadoop context.
     * @param context current context in Hadoop
     * @param datasourceId datasource ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static OutputTransactionContext createContext(JobContext context, String datasourceId) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (datasourceId == null) {
            throw new IllegalArgumentException("datasourceId must not be null"); //$NON-NLS-1$
        }
        String transactionId = getTransactionId(context, datasourceId);
        return new OutputTransactionContext(transactionId, datasourceId, createCounter(context));
    }

    /**
     * Creates output context from Hadoop context.
     * @param context current context in Hadoop
     * @param datasourceId datasource ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static OutputAttemptContext createContext(TaskAttemptContext context, String datasourceId) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (datasourceId == null) {
            throw new IllegalArgumentException("datasourceId must not be null"); //$NON-NLS-1$
        }
        String transactionId = getTransactionId(context, datasourceId);
        String attemptId = getAttemptId(context, datasourceId);
        return new OutputAttemptContext(transactionId, attemptId, datasourceId, createCounter(context));
    }

    private static String getTransactionId(JobContext jobContext, String datasourceId) {
        assert jobContext != null;
        assert datasourceId != null;
        String executionId = jobContext.getConfiguration().get(StageConstants.PROP_EXECUTION_ID);
        if (executionId == null) {
            executionId = jobContext.getJobID().toString();
        }
        return getTransactionId(executionId);
    }

    private static String getTransactionId(String executionId) {
        return executionId;
    }

    private static String getAttemptId(TaskAttemptContext taskContext, String datasourceId) {
        assert taskContext != null;
        assert datasourceId != null;
        return taskContext.getTaskAttemptID().toString();
    }

    private static Counter createCounter(JobContext context) {
        assert context != null;
        if (context instanceof Progressable) {
            return new ProgressableCounter((Progressable) context);
        } else if (context instanceof org.apache.hadoop.mapred.JobContext) {
            return new ProgressableCounter(((org.apache.hadoop.mapred.JobContext) context).getProgressible());
        } else {
            return new Counter();
        }
    }

    @Override
    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        DirectDataSourceRepository repo = getDataSourceRepository(context);
        List<OutputSpec> specs = getSpecs(context);
        for (OutputSpec spec : specs) {
            try {
                repo.getContainerPath(spec.basePath);
            } catch (IOException e) {
                throw new IOException(MessageFormat.format(
                        "There are no corresponded data sources for the base path: {0}",
                        spec.basePath), e);
            }
            for (String pattern : spec.deletePatterns) {
                try {
                    FilePattern.compile(pattern);
                } catch (IllegalArgumentException e) {
                    throw new IOException(MessageFormat.format(
                            "Invalid delete pattern: {0}",
                            pattern), e);
                }
            }
        }
    }

    @Override
    public RecordWriter<Object, Object> getRecordWriter(
            TaskAttemptContext context) throws IOException, InterruptedException {
        return new EmptyFileOutputFormat().getRecordWriter(context);
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
        synchronized (this) {
            TaskAttemptID id = context.getTaskAttemptID();
            OutputCommitter committer = commiterCache.get(id);
            if (committer == null) {
                committer = createOutputCommitter(context);
            }
            commiterCache.put(id, committer);
            return committer;
        }
    }

    private OutputCommitter createOutputCommitter(JobContext context) throws IOException {
        assert context != null;
        DirectDataSourceRepository repository = getDataSourceRepository(context);
        List<OutputSpec> specs = getSpecs(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Creating output commiter: {0}", //$NON-NLS-1$
                    specs));
        }
        return new BridgeOutputCommitter(repository, specs);
    }

    static VariableTable getVariableTable(JobContext context) {
        assert context != null;
        String arguments = context.getConfiguration().get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, ""); //$NON-NLS-1$
        VariableTable variables = new VariableTable(VariableTable.RedefineStrategy.IGNORE);
        variables.defineVariables(arguments);
        return variables;
    }

    private static final class OutputSpec {

        final String basePath;

        final List<String> deletePatterns;

        final boolean resolved;

        OutputSpec(String basePath, List<String> deletePatterns) {
            this(basePath, deletePatterns, false);
        }

        OutputSpec(String basePath, List<String> deletePatterns, boolean resolved) {
            assert basePath != null;
            this.basePath = basePath;
            this.deletePatterns = deletePatterns;
            this.resolved = resolved;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Output(path={0}, delete={1})", //$NON-NLS-1$
                    basePath,
                    deletePatterns);
        }
    }

    private static final class BridgeOutputCommitter extends OutputCommitter {

        private final DirectDataSourceRepository repository;

        private final Map<String, String> outputMap;

        private final List<OutputSpec> outputSpecs;

        BridgeOutputCommitter(
                DirectDataSourceRepository repository,
                List<OutputSpec> outputList) throws IOException {
            assert repository != null;
            assert outputList != null;
            this.repository = repository;
            this.outputSpecs = outputList;
            this.outputMap = createMap(repository, outputList);
        }

        private static Map<String, String> createMap(
                DirectDataSourceRepository repo,
                List<OutputSpec> specs) throws IOException {
            assert repo != null;
            assert specs != null;
            Map<String, String> results = new TreeMap<>();
            for (OutputSpec spec : specs) {
                String containerPath = repo.getContainerPath(spec.basePath);
                String id = repo.getRelatedId(spec.basePath);
                results.put(containerPath, id);
            }
            return results;
        }

        @Override
        public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
            return outputMap.isEmpty() == false;
        }

        @Override
        public void setupTask(TaskAttemptContext taskContext) throws IOException {
            if (outputMap.isEmpty()) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "start Direct I/O output task: {1} ({0})", //$NON-NLS-1$
                        taskContext.getJobName(),
                        taskContext.getTaskAttemptID()));
            }
            long t0 = System.currentTimeMillis();
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "start Direct I/O output task setup for datasource: " //$NON-NLS-1$
                            + "datasource={0} ({2} ({1}))", //$NON-NLS-1$
                            id,
                            taskContext.getJobName(),
                            taskContext.getTaskAttemptID()));
                }
                OutputAttemptContext context = createContext(taskContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.setupAttemptOutput(context);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed Direct I/O output task setup: datasource={0} ({2} ({1}))",
                            id,
                            taskContext.getJobName(),
                            taskContext.getTaskAttemptID()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while setup attempt: {0}, {1} (path={2})",
                            context.getTransactionId(),
                            context.getAttemptId(),
                            containerPath)).initCause(e);
                }
                context.getCounter().add(1);
            }
            if (LOG.isDebugEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.debug(MessageFormat.format(
                        "finish Direct I/O output task setup: task={1} ({0}), elapsed={2}ms", //$NON-NLS-1$
                        taskContext.getJobName(),
                        taskContext.getTaskAttemptID(),
                        t1 - t0));
            }
        }

        @Override
        public void commitTask(TaskAttemptContext taskContext) throws IOException {
            if (outputMap.isEmpty()) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "start Direct I/O output task commit: {1} ({0})", //$NON-NLS-1$
                        taskContext.getJobName(),
                        taskContext.getTaskAttemptID()));
            }
            long t0 = System.currentTimeMillis();
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "start Direct I/O output task commit for datasource: " //$NON-NLS-1$
                            + "datasource={0} ({2} ({1}))", //$NON-NLS-1$
                            id,
                            taskContext.getJobName(),
                            taskContext.getTaskAttemptID()));
                }
                OutputAttemptContext context = createContext(taskContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.commitAttemptOutput(context);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed Direct I/O output task commit: datasource={0} ({2} ({1}))",
                            id,
                            taskContext.getJobName(),
                            taskContext.getTaskAttemptID()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while commit task attempt: {0}, {1} (path={2})",
                            context.getTransactionId(),
                            context.getAttemptId(),
                            containerPath)).initCause(e);
                } catch (RuntimeException e) {
                    LOG.fatal("TASK COMMIT FAILED", e);
                    throw e;
                }
                context.getCounter().add(1);
            }
            doCleanupTask(taskContext);
            if (LOG.isInfoEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.info(MessageFormat.format(
                        "staged Direct I/O output task: task={1} ({0}), elapsed={2}ms",
                        taskContext.getJobName(),
                        taskContext.getTaskAttemptID(),
                        t1 - t0));
            }
        }

        @Override
        public void abortTask(TaskAttemptContext taskContext) throws IOException {
            if (outputMap.isEmpty()) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Start Direct I/O output task abort: {1} ({0})", //$NON-NLS-1$
                        taskContext.getJobName(),
                        taskContext.getTaskAttemptID()));
            }
            long t0 = System.currentTimeMillis();
            doCleanupTask(taskContext);
            if (LOG.isInfoEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.info(MessageFormat.format(
                        "aborted Direct I/O output task: task={1} ({0}), elapsed={2}ms",
                        taskContext.getJobName(),
                        taskContext.getTaskAttemptID(),
                        t1 - t0));
            }
        }

        private void doCleanupTask(TaskAttemptContext taskContext) throws IOException {
            assert taskContext != null;
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Start directio task cleanup for datasource: datasource={0} ({2} ({1}))", //$NON-NLS-1$
                            id,
                            taskContext.getJobName(),
                            taskContext.getTaskAttemptID()));
                }
                OutputAttemptContext context = createContext(taskContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.cleanupAttemptOutput(context);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed directio task cleanup: datasource={0} ({2} ({1}))",
                            id,
                            taskContext.getJobName(),
                            taskContext.getTaskAttemptID()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while cleanup attempt: {0}, {1} (path={2})",
                            context.getTransactionId(),
                            context.getAttemptId(),
                            containerPath)).initCause(e);
                }
                context.getCounter().add(1);
            }
        }

        @Override
        public void setupJob(JobContext jobContext) throws IOException {
            if (outputMap.isEmpty()) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "start Direct I/O output job setup: {0} ({1})", //$NON-NLS-1$
                        jobContext.getJobID(),
                        jobContext.getJobName()));
            }
            long t0 = System.currentTimeMillis();
            cleanOutput(jobContext);
            setTransactionInfo(jobContext, true);
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Start Direct I/O output job setup: datasource={0} ({1} ({2}))", //$NON-NLS-1$
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()));
                }
                OutputTransactionContext context = createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.setupTransactionOutput(context);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed Direct I/O output job setup: datasource={0} ({1} ({2}))",
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while setup transaction: {0}, (path={1})",
                            context.getTransactionId(),
                            containerPath)).initCause(e);
                }
                context.getCounter().add(1);
            }
            if (LOG.isInfoEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.info(MessageFormat.format(
                        "initialized Direct I/O output: job={0} ({1}), elapsed={2}ms",
                        jobContext.getJobID(),
                        jobContext.getJobName(),
                        t1 - t0));
            }
        }

        private void cleanOutput(JobContext jobContext) throws IOException {
            assert jobContext != null;
            for (OutputSpec spec : outputSpecs) {
                if (spec.deletePatterns.isEmpty()) {
                    continue;
                }
                String id = repository.getRelatedId(spec.basePath);
                OutputTransactionContext context = createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(spec.basePath);
                    String basePath = repository.getComponentPath(spec.basePath);
                    for (String pattern : spec.deletePatterns) {
                        FilePattern resources = FilePattern.compile(pattern);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format(
                                    "Deleting output: datasource={0}, basePath={1}, pattern={2}", //$NON-NLS-1$
                                    id,
                                    basePath,
                                    pattern));
                        }
                        boolean succeed = repo.delete(basePath, resources, true, context.getCounter());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format(
                                    "Deleted output (succeed={3}): " //$NON-NLS-1$
                                    + "datasource={0}, basePath={1}, pattern={2}", //$NON-NLS-1$
                                    id,
                                    basePath,
                                    pattern,
                                    succeed));
                        }
                    }
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed directio job setup: datasource={0} ({1} ({2}))",
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while setup cleaning output: datasource={0} ({1} ({2}))",
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName())).initCause(e);
                }
            }
        }

        @Override
        public void commitJob(JobContext jobContext) throws IOException {
            if (outputMap.isEmpty()) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "start Direct I/O output job commit: {0} ({1})", //$NON-NLS-1$
                        jobContext.getJobID(),
                        jobContext.getJobName()));
            }
            long t0 = System.currentTimeMillis();
            setCommitted(jobContext, true);
            doCleanupJob(jobContext);
            if (LOG.isInfoEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.info(MessageFormat.format(
                        "committed Direct I/O output: job={0} ({1}), elapsed={2}ms",
                        jobContext.getJobID(),
                        jobContext.getJobName(),
                        t1 - t0));
            }
        }

        private void setTransactionInfo(JobContext jobContext, boolean value) throws IOException {
            Configuration conf = jobContext.getConfiguration();
            Path transactionInfo = getTransactionInfoPath(jobContext);
            FileSystem fs = transactionInfo.getFileSystem(conf);
            if (value) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Creating Direct I/O transaction info: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(transactionInfo)));
                }
                try (OutputStream output = new SafeOutputStream(fs.create(transactionInfo, false));
                        PrintWriter writer = new PrintWriter(
                                new OutputStreamWriter(output, HadoopDataSourceUtil.COMMENT_CHARSET))) {
                    writer.printf("      User Name: %s%n", //$NON-NLS-1$
                            conf.getRaw(StageConstants.PROP_USER));
                    writer.printf("       Batch ID: %s%n", //$NON-NLS-1$
                            conf.getRaw(StageConstants.PROP_BATCH_ID));
                    writer.printf("        Flow ID: %s%n", //$NON-NLS-1$
                            conf.getRaw(StageConstants.PROP_FLOW_ID));
                    writer.printf("   Execution ID: %s%n", //$NON-NLS-1$
                            conf.getRaw(StageConstants.PROP_EXECUTION_ID));
                    writer.printf("Batch Arguments: %s%n", //$NON-NLS-1$
                            conf.getRaw(StageConstants.PROP_ASAKUSA_BATCH_ARGS));
                    writer.printf("  Hadoop Job ID: %s%n", //$NON-NLS-1$
                            jobContext.getJobID());
                    writer.printf("Hadoop Job Name: %s%n", //$NON-NLS-1$
                            jobContext.getJobName());
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Finish creating Direct I/O transaction info: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(transactionInfo)));
                }
                if (LOG.isTraceEnabled()) {
                    try (FSDataInputStream input = fs.open(transactionInfo);
                            Scanner scanner = new Scanner(new InputStreamReader(
                                    input, HadoopDataSourceUtil.COMMENT_CHARSET))) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            LOG.trace(">> " + line); //$NON-NLS-1$
                        }
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Deleting Direct I/O transaction info: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(transactionInfo)));
                }
                fs.delete(transactionInfo, false);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Finish deleting Direct I/O transaction info: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(transactionInfo)));
                }
            }
        }

        private void setCommitted(JobContext jobContext, boolean value) throws IOException {
            Configuration conf = jobContext.getConfiguration();
            Path commitMark = getCommitMarkPath(jobContext);
            FileSystem fs = commitMark.getFileSystem(conf);
            if (value) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Creating Direct I/O commit mark: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(commitMark)));
                }
                fs.create(commitMark, false).close();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Finish creating Direct I/O commit mark: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(commitMark)));
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Deleting Direct I/O commit mark: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(commitMark)));
                }
                fs.delete(commitMark, false);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Finish deleting Direct I/O commit mark: job={0} ({1}), path={2}", //$NON-NLS-1$
                            jobContext.getJobID(),
                            jobContext.getJobName(),
                            fs.makeQualified(commitMark)));
                }
            }
        }

        private boolean isCommitted(JobContext jobContext) throws IOException {
            Path commitMark = getCommitMarkPath(jobContext);
            FileSystem fs = commitMark.getFileSystem(jobContext.getConfiguration());
            return fs.exists(commitMark);
        }

        @Override
        public void abortJob(JobContext jobContext, State state) throws IOException {
            if (outputMap.isEmpty()) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Start Direct I/O output job abort: job={0} ({1}), state={2}", //$NON-NLS-1$
                        jobContext.getJobID(),
                        jobContext.getJobName(),
                        state));
            }
            long t0 = System.currentTimeMillis();
            if (state == State.FAILED) {
                doCleanupJob(jobContext);
            }
            if (LOG.isInfoEnabled()) {
                long t1 = System.currentTimeMillis();
                LOG.info(MessageFormat.format(
                        "aborted Direct I/O output: job={0} ({1}), state={2}, elapsed={3}ms",
                        jobContext.getJobID(),
                        jobContext.getJobName(),
                        state,
                        t1 - t0));
            }
        }

        private void doCleanupJob(JobContext jobContext) throws IOException {
            if (isCommitted(jobContext)) {
                rollforward(jobContext);
            }
            cleanup(jobContext);
            setCommitted(jobContext, false);
            setTransactionInfo(jobContext, false);
        }

        private void rollforward(JobContext jobContext) throws IOException {
            assert jobContext != null;
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Start Direct I/O output job rollforward: datasource={0} ({1} ({2}))", //$NON-NLS-1$
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()));
                }
                OutputTransactionContext context = createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.commitTransactionOutput(context);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed Direct I/O output job rollforward: datasource={0} ({1} ({2}))",
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while commit transaction: {0}, (path={1})",
                            context.getTransactionId(),
                            containerPath)).initCause(e);
                }
                context.getCounter().add(1);
            }
        }

        private void cleanup(JobContext jobContext) throws IOException {
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Start Direct I/O output job cleanup: datasource={0} ({1} ({2}))", //$NON-NLS-1$
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()));
                }
                OutputTransactionContext context = createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.cleanupTransactionOutput(context);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "Failed Direct I/O output job cleanup: datasource={0} ({1} ({2}))",
                            id,
                            jobContext.getJobID(),
                            jobContext.getJobName()), e);
                    throw e;
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while cleanup transaction: {0}, (path={1})",
                            context.getTransactionId(),
                            containerPath)).initCause(e);
                }
                context.getCounter().add(1);
            }
        }

        private static Path getTransactionInfoPath(JobContext context) throws IOException {
            assert context != null;
            Configuration conf = context.getConfiguration();
            String executionId = conf.get(StageConstants.PROP_EXECUTION_ID);
            return HadoopDataSourceUtil.getTransactionInfoPath(conf, executionId);
        }

        private static Path getCommitMarkPath(JobContext context) throws IOException {
            assert context != null;
            Configuration conf = context.getConfiguration();
            String executionId = conf.get(StageConstants.PROP_EXECUTION_ID);
            return HadoopDataSourceUtil.getCommitMarkPath(conf, executionId);
        }
    }

    private static class SafeOutputStream extends OutputStream {

        private final OutputStream delegate;

        private final AtomicBoolean closed = new AtomicBoolean();

        SafeOutputStream(OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                delegate.close();
            }
        }
    }
}
