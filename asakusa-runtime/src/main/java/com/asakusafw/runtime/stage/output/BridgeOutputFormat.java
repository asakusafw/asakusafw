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
package com.asakusafw.runtime.stage.output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus.State;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.StageOutput;

/**
 * A bridge implementation for Hadoop {@link OutputFormat}.
 * @since 0.2.5
 */
public final class BridgeOutputFormat extends OutputFormat<Object, Object> {

    static final Log LOG = LogFactory.getLog(BridgeOutputFormat.class);

    private static final Charset ASCII = Charset.forName("ASCII");

    private static final long SERIAL_VERSION = 1;

    private static final String KEY = "com.asakusafw.output.bridge";

    /**
     * The key name of system directory for this format.
     */
    public static final String KEY_SYSTEM_DIR = "com.asakusafw.output.system.dir";

    static final String DEFAULT_SYSTEM_DIR = "_directio";

    static final String COMMIT_MARK_DIR = "commits";

    private OutputCommitter outputCommitter;

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
        List<OutputSpec> specs = new ArrayList<OutputSpec>();
        for (StageOutput output : outputList) {
            OutputSpec spec = new OutputSpec(output.getName());
            specs.add(spec);
        }
        save(context.getConfiguration(), specs);
    }

    private static void save(Configuration conf, List<OutputSpec> specs) {
        assert conf != null;
        assert specs != null;
        try {
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(new GZIPOutputStream(new Base64OutputStream(sink)));
            WritableUtils.writeVLong(output, SERIAL_VERSION);
            WritableUtils.writeVInt(output, specs.size());
            for (OutputSpec spec: specs) {
                WritableUtils.writeString(output, spec.basePath);
            }
            output.close();
            conf.set(KEY, new String(sink.toByteArray(), ASCII));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<OutputSpec> getSpecs(JobContext context) {
        assert context != null;
        String encoded = context.getConfiguration().getRaw(KEY);
        if (encoded == null) {
            return Collections.emptyList();
        }
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
            List<OutputSpec> results = new ArrayList<OutputSpec>();
            int specCount = WritableUtils.readVInt(input);
            for (int specIndex = 0; specIndex < specCount; specIndex++) {
                String basePath = WritableUtils.readString(input);
                results.add(new OutputSpec(basePath));
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
     * Returns the commit mark directory.
     * @param conf the current configuration
     * @return target path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Path getCommitMarkDirectory(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        String working = conf.get(KEY_SYSTEM_DIR, DEFAULT_SYSTEM_DIR);
        String path = String.format("%s/%s", working, COMMIT_MARK_DIR);
        return new Path(path);
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
                        spec.basePath));
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
        return getOutputCommitter((JobContext) context);
    }

    OutputCommitter getOutputCommitter(JobContext context) throws IOException {
        synchronized (this) {
            if (outputCommitter == null) {
                outputCommitter = createOutputCommitter(context);
            }
            return outputCommitter;
        }
    }

    private OutputCommitter createOutputCommitter(JobContext context) throws IOException {
        assert context != null;
        DirectDataSourceRepository repository = getDataSourceRepository(context);
        List<OutputSpec> specs = getSpecs(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Creating output commiter: {0}",
                    specs));
        }
        return new BridgeOutputCommitter(repository, specs);
    }

    private static final class OutputSpec {

        final String basePath;

        OutputSpec(String basePath) {
            assert basePath != null;
            this.basePath = basePath;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Output(path={0})",
                    basePath);
        }
    }

    private static final class BridgeOutputCommitter extends OutputCommitter {

        private final DirectDataSourceRepository repository;

        private final Map<String, String> outputMap;

        BridgeOutputCommitter(
                DirectDataSourceRepository repository,
                List<OutputSpec> outputList) throws IOException {
            assert repository != null;
            assert outputList != null;
            this.repository = repository;
            this.outputMap = createMap(repository, outputList);
        }

        private static Map<String, String> createMap(
                DirectDataSourceRepository repo,
                List<OutputSpec> specs) throws IOException {
            assert repo != null;
            assert specs != null;
            Map<String, String> results = new TreeMap<String, String>();
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
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "Start directio task setup: job={0}, task={1}",
                        taskContext.getJobID(),
                        taskContext.getTaskAttemptID()));
            }
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                OutputAttemptContext context = HadoopDataSourceUtil.createContext(taskContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.setupAttemptOutput(context);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while setup attempt: {0}, {1} (path={2})",
                            context.getTransactionId(),
                            context.getAttemptId(),
                            containerPath)).initCause(e);
                }
            }
        }

        @Override
        public void commitTask(TaskAttemptContext taskContext) throws IOException {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "Start directio task commit: job={0}, task={1}",
                        taskContext.getJobID(),
                        taskContext.getTaskAttemptID()));
            }
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                OutputAttemptContext context = HadoopDataSourceUtil.createContext(taskContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.commitAttemptOutput(context);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while commit attempt: {0}, {1} (path={2})",
                            context.getTransactionId(),
                            context.getAttemptId(),
                            containerPath)).initCause(e);
                }
            }
            doCleanupTask(taskContext);
        }

        @Override
        public void abortTask(TaskAttemptContext taskContext) throws IOException {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "Start directio task abort: job={0}, task={1}",
                        taskContext.getJobID(),
                        taskContext.getTaskAttemptID()));
            }
            doCleanupTask(taskContext);
        }

        private void doCleanupTask(TaskAttemptContext taskContext) throws IOException {
            assert taskContext != null;
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                OutputAttemptContext context = HadoopDataSourceUtil.createContext(taskContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.cleanupAttemptOutput(context);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while cleanup attempt: {0}, {1} (path={2})",
                            context.getTransactionId(),
                            context.getAttemptId(),
                            containerPath)).initCause(e);
                }
            }
        }

        @Override
        public void setupJob(JobContext jobContext) throws IOException {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "Start directio job setup: job={0}",
                        jobContext.getJobID()));
            }
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                OutputTransactionContext context = HadoopDataSourceUtil.createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.setupTransactionOutput(context);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while setup transaction: {0}, (path={1})",
                            context.getTransactionId(),
                            containerPath)).initCause(e);
                }
            }
        }

        @Override
        public void commitJob(JobContext jobContext) throws IOException {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "Start directio job commit: job={0}",
                        jobContext.getJobID()));
            }
            setCommitted(jobContext, true);
            doCleanupJob(jobContext);
        }

        private void setCommitted(JobContext jobContext, boolean value) throws IOException {
            Path commitMark = getCommitMarkPath(jobContext);
            FileSystem fs = commitMark.getFileSystem(jobContext.getConfiguration());
            if (value) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(MessageFormat.format(
                            "Creating commit mark: job={0}, path={1}",
                            jobContext.getJobID(),
                            fs.makeQualified(commitMark)));
                }
                fs.create(commitMark, false).close();
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info(MessageFormat.format(
                            "Deleting commit mark: job={0}, path={1}",
                            jobContext.getJobID(),
                            fs.makeQualified(commitMark)));
                }
                fs.delete(commitMark, false);
            }
        }

        private boolean isCommitted(JobContext jobContext) throws IOException {
            Path commitMark = getCommitMarkPath(jobContext);
            FileSystem fs = commitMark.getFileSystem(jobContext.getConfiguration());
            return fs.exists(commitMark);
        }

        @Override
        public void abortJob(JobContext jobContext, State state) throws IOException {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "Start directio job abort: job={0}, state={1}",
                        jobContext.getJobID(),
                        state));
            }
            if (state == State.FAILED) {
                doCleanupJob(jobContext);
            }
        }

        private void doCleanupJob(JobContext jobContext) throws IOException {
            if (isCommitted(jobContext)) {
                rollforward(jobContext);
            }
            cleanup(jobContext);
        }

        private void rollforward(JobContext jobContext) throws IOException {
            assert jobContext != null;
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                OutputTransactionContext context = HadoopDataSourceUtil.createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.commitTransactionOutput(context);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while commit transaction: {0}, (path={1})",
                            context.getTransactionId(),
                            containerPath)).initCause(e);
                }
            }
            setCommitted(jobContext, false);
        }

        private void cleanup(JobContext jobContext) throws IOException {
            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                String containerPath = entry.getKey();
                String id = entry.getValue();
                OutputTransactionContext context = HadoopDataSourceUtil.createContext(jobContext, id);
                try {
                    DirectDataSource repo = repository.getRelatedDataSource(containerPath);
                    repo.cleanupTransactionOutput(context);
                } catch (InterruptedException e) {
                    throw (IOException) new InterruptedIOException(MessageFormat.format(
                            "Interrupted while cleanup transaction: {0}, (path={1})",
                            context.getTransactionId(),
                            containerPath)).initCause(e);
                }
            }
        }

        private static Path getCommitMarkPath(JobContext context) {
            assert context != null;
            String executionId = context.getConfiguration().get(StageConstants.PROP_EXECUTION_ID);
            return new Path(
                    getCommitMarkDirectory(context.getConfiguration()),
                    String.format("commit-%s", executionId));
        }
    }
}
