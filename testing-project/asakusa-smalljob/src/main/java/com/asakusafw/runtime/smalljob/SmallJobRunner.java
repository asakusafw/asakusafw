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
package com.asakusafw.runtime.smalljob;

import static com.asakusafw.runtime.compatibility.JobCompatibility.*;

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
 * An implementation of {@link JobRunner} for small jobs.
 * @since 0.7.1
 */
public class SmallJobRunner implements JobRunner {

    static final Log LOG = LogFactory.getLog(SmallJobRunner.class);

    private static final String KEY_BUFFER_SIZE_MB = "io.sort.mb";

    private static final int DEFAULT_BUFFER_SIZE = 20 * 1024 * 1024;

    private static final int MINIMUM_BUFFER_SIZE = 2 * 1024 * 1024;

    private static final int MAXIMUM_BUFFER_SIZE = 1 << 30;

    private static final float BUFFER_OVERHEAD_FACTOR = 0.2f;

    private static final float PAGE_PER_BLOCK = 5.0f;

    @Override
    public boolean run(Job job) throws IOException, InterruptedException, ClassNotFoundException {
        JobID jobId = newJobId(new Random().nextInt(Integer.MAX_VALUE));
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "starting job: {0}",
                    jobId));
        }
        setJobId(job, jobId);
        TaskID taskId = newMapTaskId(jobId, 0);
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
                KeyValueSorter<?, ?> sorter = createSorter(
                        job, job.getMapOutputKeyClass(), job.getMapOutputValueClass());
                try {
                    runMap(job, sorter);
                    runReduce(job, sorter);
                } finally {
                    sorter.close();
                }
            }
            succeed = true;
        } finally {
            if (succeed) {
                committer.commitJob(job);
            } else {
                committer.abortJob(job, State.FAILED);
            }
        }
        return true;
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
                    reader.close();
                }
                succeed = true;
            } finally {
                if (succeed) {
                    if (committer.needsTaskCommit(context)) {
                        committer.commitTask(context);
                    }
                } else {
                    committer.abortTask(context);
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
                    "starting reducer: {0}@{1} ({2}records)",
                    reducer.getClass().getName(),
                    id,
                    sorter.getRecordCount()));
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
                            reader,
                            sorter.getKeyClass(), sorter.getValueClass(),
                            writer,
                            committer,
                            (RawComparator) job.getGroupingComparator());
                    reducer.run(c);
                } finally {
                    writer.close(newTaskAttemptContext(conf, id));
                }
            } finally {
                reader.close();
            }
            succeed = true;
        } finally {
            if (succeed) {
                if (committer.needsTaskCommit(context)) {
                    committer.commitTask(context);
                }
            } else {
                committer.abortTask(context);
            }
        }
    }

    private <K, V> KeyValueSorter<?, ?> createSorter(Job job, Class<K> key, Class<V> value) {
        int bufferSize = getTotalBufferSize(job.getConfiguration());
        int pageSize = (int) (bufferSize / PAGE_PER_BLOCK);
        int blockSize = bufferSize - pageSize;
        return new KeyValueSorter<K, V>(
                new SerializationFactory(job.getConfiguration()),
                key, value, job.getSortComparator(),
                pageSize, blockSize);
    }

    private int getTotalBufferSize(Configuration configuration) {
        long bufferSize = configuration.getLong(KEY_BUFFER_SIZE_MB, -1) * 1024 * 1024;
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        bufferSize = Math.max(MINIMUM_BUFFER_SIZE, Math.min(MAXIMUM_BUFFER_SIZE, bufferSize));
        return (int) (bufferSize * (1.0 - BUFFER_OVERHEAD_FACTOR));
    }
}
