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
package com.asakusafw.runtime.mapreduce.simple;

import static com.asakusafw.runtime.compatibility.JobCompatibility.*;

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
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.util.Progress;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.stage.JobRunner;

/**
 * An implementation of {@link JobRunner} using simplified map-reduce engine.
 * @since 0.7.1
 */
public class SimpleJobRunner implements JobRunner {

    static final Log LOG = LogFactory.getLog(SimpleJobRunner.class);

    private static final String KEY_PREFIX = "com.asakusafw.mapreduce.";

    /**
     * Hadoop property key of shuffle buffer size.
     */
    public static final String KEY_BUFFER_SIZE = KEY_PREFIX + "shuffle.buffer";

    /**
     * Hadoop property key of shuffle temporary directory.
     */
    public static final String KEY_TEMPORARY_LOCATION = KEY_PREFIX + "shuffle.tempdir";

    /**
     * Hadoop property key of whether block file compression is enabled or not.
     */
    public static final String KEY_COMPRESS_BLOCK = KEY_PREFIX + "shuffle.compress";

    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024 * 1024;

    private static final int MIN_BUFFER_SIZE = 2 * 1024 * 1024;

    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;

    private static final boolean DEFAULT_COMPRESS_BLOCK = false;

    @Override
    public boolean run(Job job) throws InterruptedException {
        JobID jobId = newJobId(new Random().nextInt(Integer.MAX_VALUE));
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "starting job using simplified map-reduce engine: {0} ({1})",
                    jobId,
                    job.getJobName()));
        }
        setJobId(job, jobId);
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
        TaskID taskId = newMapTaskId(job.getJobID(), 0);
        Configuration conf = job.getConfiguration();
        OutputFormat<?, ?> output = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
        OutputCommitter committer = output.getOutputCommitter(
                newTaskAttemptContext(conf, newTaskAttemptId(taskId, 0)));
        boolean succeed = false;
        committer.setupJob(job);
        try {
            if (job.getNumReduceTasks() == 0) {
                runMap(job, null);
            } else {
                KeyValueSorter<?, ?> sorter = createSorter(job,
                        job.getMapOutputKeyClass(), job.getMapOutputValueClass());
                try {
                    runMap(job, sorter);
                    runReduce(job, sorter);
                } finally {
                    try {
                        sorter.close();
                    } catch (IOException e) {
                        LOG.warn(MessageFormat.format(
                                "error occurred while closing sorter: {0} ({1})",
                                job.getJobID(),
                                job.getJobName()), e);
                    }
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
            TaskAttemptID id = newTaskAttemptId(newMapTaskId(job.getJobID(), serial++), 0);
            Mapper<?, ?, ?, ?> mapper = ReflectionUtils.newInstance(job.getMapperClass(), conf);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "starting mapper: {0}@{1} ({2}bytes)",
                        mapper.getClass().getName(),
                        id,
                        split.getLength()));
            }
            TaskAttemptContext context = newTaskAttemptContext(conf, id);
            // we always obtain a new OutputFormat object / OutputFormat.getOutputCommiter() may be cached
            OutputFormat<?, ?> output = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
            OutputCommitter committer = output.getOutputCommitter(context);
            committer.setupTask(context);
            boolean succeed = false;
            try {
                RecordReader<?, ?> reader = input.createRecordReader(split, newTaskAttemptContext(conf, id));
                try {
                    RecordWriter<?, ?> writer;
                    if (sorter != null) {
                        writer = new ShuffleWriter(sorter);
                    } else {
                        writer = output.getRecordWriter(newTaskAttemptContext(conf, id));
                    }
                    try {
                        Mapper.Context c = newMapperContext(
                                conf, id,
                                reader, writer,
                                committer, split);
                        reader.initialize(split, c);
                        mapper.run(c);
                    } finally {
                        writer.close(newTaskAttemptContext(conf, id));
                    }
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.warn(MessageFormat.format(
                                "error occurred while closing mapper input: {0} ({1})",
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
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void runReduce(
            Job job,
            KeyValueSorter<?, ?> sorter) throws ClassNotFoundException, IOException, InterruptedException {
        Configuration conf = job.getConfiguration();
        OutputFormat<?, ?> output = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
        TaskAttemptID id = newTaskAttemptId(newReduceTaskId(job.getJobID(), 1), 0);
        Reducer<?, ?, ?, ?> reducer = ReflectionUtils.newInstance(job.getReducerClass(), conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "starting reducer: {0}@{1} ({2}records, {3}bytes)",
                    reducer.getClass().getName(),
                    id,
                    sorter.getRecordCount(),
                    sorter.getSizeInBytes()));
        }
        TaskAttemptContext context = newTaskAttemptContext(conf, id);
        OutputCommitter committer = output.getOutputCommitter(context);
        committer.setupTask(context);
        boolean succeed = false;
        try {
            ShuffleReader reader = new ShuffleReader(sorter, new Progress());
            try {
                RecordWriter<?, ?> writer = output.getRecordWriter(newTaskAttemptContext(conf, id));
                try {
                    Reducer.Context c = newReducerContext(
                            conf, id,
                            reader, sorter.getKeyClass(), sorter.getValueClass(),
                            writer, committer,
                            (RawComparator) job.getGroupingComparator());
                    reducer.run(c);
                } finally {
                    writer.close(newTaskAttemptContext(conf, id));
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
                    "shuffle buffer size: {1}bytes/page, {2}bytes/block, compression:{3} ({0})",
                    job.getJobName(),
                    options.getPageSize(),
                    options.getBlockSize(),
                    options.isCompressBlock()));
        }
        return new KeyValueSorter<K, V>(
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
}
