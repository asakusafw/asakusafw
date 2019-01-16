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
package com.asakusafw.runtime.mapreduce.simple;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.serializer.SerializationFactory;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.JobStatus.State;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.counters.GenericCounter;
import org.apache.hadoop.mapreduce.lib.map.WrappedMapper;
import org.apache.hadoop.mapreduce.lib.reduce.WrappedReducer;
import org.apache.hadoop.mapreduce.task.MapContextImpl;
import org.apache.hadoop.mapreduce.task.ReduceContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.Progress;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.stage.JobRunner;

/**
 * An implementation of {@link JobRunner} using simplified map-reduce engine.
 * @since 0.7.1
 */
public class SimpleJobRunner implements JobRunner {

    static final Log LOG = LogFactory.getLog(SimpleJobRunner.class);

    private static final String KEY_PREFIX = "com.asakusafw.mapreduce."; //$NON-NLS-1$

    /**
     * Hadoop property key of shuffle buffer size.
     */
    public static final String KEY_BUFFER_SIZE = KEY_PREFIX + "shuffle.buffer"; //$NON-NLS-1$

    /**
     * Hadoop property key of shuffle temporary directory.
     */
    public static final String KEY_TEMPORARY_LOCATION = KEY_PREFIX + "shuffle.tempdir"; //$NON-NLS-1$

    /**
     * Hadoop property key of whether block file compression is enabled or not.
     */
    public static final String KEY_COMPRESS_BLOCK = KEY_PREFIX + "shuffle.compress"; //$NON-NLS-1$

    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024 * 1024;

    private static final int MIN_BUFFER_SIZE = 2 * 1024 * 1024;

    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;

    private static final boolean DEFAULT_COMPRESS_BLOCK = false;

    private static final String DUMMY_JOBTRACKER_ID = "asakusafw";

    @Override
    public boolean run(Job job) throws InterruptedException {
        job.setJobID(new JobID(DUMMY_JOBTRACKER_ID, new Random().nextInt(Integer.MAX_VALUE)));
        LOG.info(MessageFormat.format(
                "starting job using {0}: {1} ({2})",
                this,
                job.getJobID(),
                job.getJobName()));
        try {
            runJob(job);
            return true;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "exception was occurred while executing job: {0} ({1})",
                    job.getJobID(),
                    job.getJobName()), e);
            return false;
        }
    }

    private void runJob(Job job) throws ClassNotFoundException, IOException, InterruptedException {
        assert job.getJobID() != null;
        TaskID taskId = new TaskID(job.getJobID(), TaskType.MAP, 0);
        Configuration conf = job.getConfiguration();
        OutputFormat<?, ?> output = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
        OutputCommitter committer = output.getOutputCommitter(
                new TaskAttemptContextImpl(conf, new TaskAttemptID(taskId, 0)));
        boolean succeed = false;
        committer.setupJob(job);
        try {
            if (job.getNumReduceTasks() == 0) {
                runMap(job, null);
            } else {
                try (KeyValueSorter<?, ?> sorter = createSorter(job,
                        job.getMapOutputKeyClass(), job.getMapOutputValueClass())) {
                    runMap(job, sorter);
                    runReduce(job, sorter);
                }
            }
            committer.commitJob(job);
            succeed = true;
        } finally {
            if (succeed == false) {
                try {
                    committer.abortJob(job, State.FAILED);
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "error occurred while aborting job: {0} ({1})",
                            job.getJobID(),
                            job.getJobName()), e);
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void runMap(
            Job job,
            KeyValueSorter<?, ?> sorter) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = job.getConfiguration();
        InputFormat<?, ?> input = ReflectionUtils.newInstance(job.getInputFormatClass(), conf);
        List<InputSplit> splits = input.getSplits(job);
        int serial = 1;
        for (InputSplit split : splits) {
            TaskAttemptID id = new TaskAttemptID(new TaskID(job.getJobID(), TaskType.MAP, serial++), 0);
            Mapper<?, ?, ?, ?> mapper = ReflectionUtils.newInstance(job.getMapperClass(), conf);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "starting mapper: {0}@{1} ({2}bytes)", //$NON-NLS-1$
                        mapper.getClass().getName(),
                        id,
                        split.getLength()));
            }
            TaskAttemptContext context = new TaskAttemptContextImpl(conf, id);
            // we always obtain a new OutputFormat object / OutputFormat.getOutputCommiter() may be cached
            OutputFormat<?, ?> output = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
            OutputCommitter committer = output.getOutputCommitter(context);
            committer.setupTask(context);
            boolean succeed = false;
            try (RecordReader<?, ?> reader = input.createRecordReader(split, new TaskAttemptContextImpl(conf, id))) {
                RecordWriter<?, ?> writer;
                if (sorter != null) {
                    writer = new ShuffleWriter(sorter);
                } else {
                    writer = output.getRecordWriter(new TaskAttemptContextImpl(conf, id));
                }
                try {
                    Mapper.Context c = new WrappedMapper().getMapContext(new MapContextImpl<>(
                            conf, id,
                            reader, writer,
                            committer, new MockStatusReporter(),
                            split));
                    reader.initialize(split, c);
                    mapper.run(c);
                } finally {
                    writer.close(new TaskAttemptContextImpl(conf, id));
                }
                doCommitTask(context, committer);
                succeed = true;
            } finally {
                if (succeed == false) {
                    doAbortTask(context, committer);
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void runReduce(
            Job job,
            KeyValueSorter<?, ?> sorter) throws ClassNotFoundException, IOException, InterruptedException {
        Configuration conf = job.getConfiguration();
        OutputFormat<?, ?> output = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
        TaskAttemptID id = new TaskAttemptID(new TaskID(job.getJobID(), TaskType.REDUCE, 0), 0);
        Reducer<?, ?, ?, ?> reducer = ReflectionUtils.newInstance(job.getReducerClass(), conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "starting reducer: {0}@{1} ({2}records, {3}bytes)", //$NON-NLS-1$
                    reducer.getClass().getName(),
                    id,
                    sorter.getRecordCount(),
                    sorter.getSizeInBytes()));
        }
        TaskAttemptContext context = new TaskAttemptContextImpl(conf, id);
        OutputCommitter committer = output.getOutputCommitter(context);
        committer.setupTask(context);
        boolean succeed = false;
        try {
            ShuffleReader reader = new ShuffleReader(sorter, new Progress());
            try {
                RecordWriter<?, ?> writer = output.getRecordWriter(new TaskAttemptContextImpl(conf, id));
                try {
                    Reducer.Context c = new WrappedReducer().getReducerContext(new ReduceContextImpl<>(
                            conf, id, reader,
                            new GenericCounter(),
                            new GenericCounter(),
                            writer, committer, new MockStatusReporter(),
                            (RawComparator) job.getGroupingComparator(),
                            sorter.getKeyClass(), sorter.getValueClass()));
                    reducer.run(c);
                } finally {
                    writer.close(new TaskAttemptContextImpl(conf, id));
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "error occurred while reducer mapper input: {0} ({1})",
                            id,
                            job.getJobName()), e);
                }
            }
            doCommitTask(context, committer);
            succeed = true;
        } finally {
            if (succeed == false) {
                doAbortTask(context, committer);
            }
        }
    }

    private void doCommitTask(TaskAttemptContext context, OutputCommitter committer) throws IOException {
        if (committer.needsTaskCommit(context)) {
            committer.commitTask(context);
        }
    }

    private void doAbortTask(TaskAttemptContext context, OutputCommitter committer) {
        try {
            committer.abortTask(context);
        } catch (IOException e) {
            LOG.error(MessageFormat.format(
                    "error occurred while aborting task: {0} ({1})",
                    context.getTaskAttemptID(),
                    context.getJobName()), e);
        }
    }

    private <K, V> KeyValueSorter<?, ?> createSorter(Job job, Class<K> key, Class<V> value) {
        KeyValueSorter.Options options = getSorterOptions(job.getConfiguration());
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "shuffle buffer size: {1}bytes/page, {2}bytes/block, compression:{3} ({0})", //$NON-NLS-1$
                    job.getJobName(),
                    options.getPageSize(),
                    options.getBlockSize(),
                    options.isCompressBlock()));
        }
        return new KeyValueSorter<>(
                new SerializationFactory(job.getConfiguration()),
                key, value, job.getSortComparator(),
                options);
    }

    private KeyValueSorter.Options getSorterOptions(Configuration configuration) {
        long bufferSize = configuration.getLong(KEY_BUFFER_SIZE, -1);
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        } else {
            bufferSize = Math.max(MIN_BUFFER_SIZE, Math.min(MAX_BUFFER_SIZE, bufferSize));
        }
        File temporaryDirectory = null;
        String tempdirString = configuration.get(KEY_TEMPORARY_LOCATION);
        if (tempdirString != null) {
            temporaryDirectory = new File(tempdirString);
            if (temporaryDirectory.mkdirs() == false && temporaryDirectory.isDirectory() == false) {
                LOG.warn(MessageFormat.format(
                        "failed to prepare shuffle temporary directory: {0}={1}",
                        KEY_TEMPORARY_LOCATION,
                        temporaryDirectory));
            }
        }
        boolean compress = configuration.getBoolean(KEY_COMPRESS_BLOCK, DEFAULT_COMPRESS_BLOCK);
        KeyValueSorter.Options options = new KeyValueSorter.Options()
            .withBufferSize((int) bufferSize)
            .withTemporaryDirectory(temporaryDirectory)
            .withCompressBlock(compress);
        return options;
    }

    @Override
    public String toString() {
        return "Asakusa built-in job runner";
    }

    private static final class MockStatusReporter extends StatusReporter {

        MockStatusReporter() {
            return;
        }

        @Override
        public Counter getCounter(Enum<?> name) {
            return new GenericCounter();
        }

        @Override
        public Counter getCounter(String group, String name) {
            return new GenericCounter();
        }

        @Override
        public void progress() {
            return;
        }

        @Override
        public void setStatus(String status) {
            return;
        }

        @Override
        public float getProgress() {
            return 0;
        }
    }
}
