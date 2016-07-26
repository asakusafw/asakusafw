/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.compatibility;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.MRConfig;
import org.apache.hadoop.mapreduce.MapContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.ReduceContext;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.counters.GenericCounter;
import org.apache.hadoop.mapreduce.lib.map.WrappedMapper;
import org.apache.hadoop.mapreduce.lib.reduce.WrappedReducer;
import org.apache.hadoop.mapreduce.task.MapContextImpl;
import org.apache.hadoop.mapreduce.task.ReduceContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.Progressable;

/**
 * Compatibility for job APIs.
 * @since 0.5.0
 * @version 0.7.4
 */
public final class JobCompatibility {

    static final Log LOG = LogFactory.getLog(JobCompatibility.class);

    private static final String NAME_DUMMY_JOB = "asakusafw"; //$NON-NLS-1$

    private JobCompatibility() {
        return;
    }

    /**
     * Creates a new job.
     * @param conf current configuration
     * @return the created job
     * @throws IOException if failed to create a job client
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Job newJob(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        return Job.getInstance(conf);
    }

    /**
     * Creates a new dummy {@link TaskAttemptContext}.
     * @param conf current configuration
     * @param id target task ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptContext newTaskAttemptContext(Configuration conf, TaskAttemptID id) {
        return new TaskAttemptContextImpl(conf, id);
    }

    /**
     * Creates a new dummy {@link TaskAttemptContext}.
     * @param conf current configuration
     * @param id target task ID
     * @param progressable delegated progressable object
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptContext newTaskAttemptContext(
            Configuration conf,
            TaskAttemptID id,
            Progressable progressable) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (progressable == null) {
            return new TaskAttemptContextImpl(conf, id);
        }
        return new TaskAttemptContextImpl(conf, id) {
            @Override
            public void progress() {
                progressable.progress();
                super.progress();
            }
        };
    }

    /**
     * Creates a new dummy job ID.
     * @return the created ID
     */
    public static JobID newJobId() {
        return newJobId(0);
    }

    /**
     * Creates a new job ID.
     * @param id the local ID
     * @return the created ID
     * @since 0.7.1
     */
    public static JobID newJobId(int id) {
        return new JobID(NAME_DUMMY_JOB, id);
    }

    /**
     * Creates a new dummy task ID.
     * @param jobId parent job ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskID newTaskId(JobID jobId) {
        return newMapTaskId(jobId, 0);
    }

    /**
     * Creates a new map task ID.
     * @param jobId parent job ID
     * @param id the local ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public static TaskID newMapTaskId(JobID jobId, int id) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        return new TaskID(jobId, TaskType.MAP, id);
    }

    /**
     * Creates a new reduce task ID.
     * @param jobId parent job ID
     * @param id the local ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public static TaskID newReduceTaskId(JobID jobId, int id) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        return new TaskID(jobId, TaskType.REDUCE, id);
    }

    /**
     * Creates a new dummy task attempt ID.
     * @param taskId parent task ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptID newTaskAttemptId(TaskID taskId) {
        return newTaskAttemptId(taskId, 0);
    }

    /**
     * Creates a new task attempt ID.
     * @param taskId parent task ID
     * @param id the local ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public static TaskAttemptID newTaskAttemptId(TaskID taskId, int id) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId must not be null"); //$NON-NLS-1$
        }
        return new TaskAttemptID(taskId, id);
    }

    /**
     * Sets the job ID to the job object.
     * @param job the job object
     * @param id the job ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public static void setJobId(Job job, JobID id) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        job.setJobID(id);
    }

    /**
     * Returns an task output record counter for the current context.
     * @param context the current context
     * @return corresponded record counter
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Counter getTaskOutputRecordCounter(TaskInputOutputContext<?, ?, ?, ?> context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (context.getTaskAttemptID().getTaskType() == TaskType.MAP) {
            return context.getCounter(TaskCounter.MAP_OUTPUT_RECORDS);
        } else {
            return context.getCounter(TaskCounter.REDUCE_OUTPUT_RECORDS);
        }
    }

    /**
     * Returns whether the current job is on the local mode or not.
     * @param context the current context
     * @return {@code true} if the target job is running on the local mode, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.6.2
     */
    public static boolean isLocalMode(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "{0}={1}", //$NON-NLS-1$
                    MRConfig.FRAMEWORK_NAME,
                    context.getConfiguration().get(MRConfig.FRAMEWORK_NAME)));
        }
        String name = context.getConfiguration().get(MRConfig.FRAMEWORK_NAME, MRConfig.LOCAL_FRAMEWORK_NAME);
        return name.equals(MRConfig.LOCAL_FRAMEWORK_NAME);
    }

    /**
     * Creates a {@link Mapper} context.
     * @param configuration the current configuration
     * @param id the current task ID
     * @param reader the input reader
     * @param writer the output writer
     * @param committer the output committer
     * @param split the input split
     * @param <KEYIN> input key type
     * @param <VALUEIN> input value type
     * @param <KEYOUT> output key type
     * @param <VALUEOUT> output value type
     * @return the created context
     * @throws IOException if failed to build a context by I/O error
     * @throws InterruptedException if interrupted while initializing context
     * @since 0.7.1
     */
    public static <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context newMapperContext(
            Configuration configuration,
            TaskAttemptID id,
            RecordReader<KEYIN, VALUEIN> reader,
            RecordWriter<KEYOUT, VALUEOUT> writer,
            OutputCommitter committer,
            InputSplit split) throws IOException, InterruptedException {
        MapContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context = new MapContextImpl<>(
                configuration, id, reader, writer, committer, new MockStatusReporter(), split);
        return new WrappedMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>().getMapContext(context);
    }

    /**
     * Creates a {@link Reducer} context.
     * @param configuration the current configuration
     * @param id the current task ID
     * @param reader the shuffle result iterator
     * @param inputKeyClass the input key class
     * @param inputValueClass the input value class
     * @param writer the output writer
     * @param committer the output committer
     * @param comparator the grouping comparator
     * @param <KEYIN> input key type
     * @param <VALUEIN> input value type
     * @param <KEYOUT> output key type
     * @param <VALUEOUT> output value type
     * @return the created context
     * @throws IOException if failed to build a context by I/O error
     * @throws InterruptedException if interrupted while initializing context
     * @since 0.7.1
     */
    public static <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context newReducerContext(
            Configuration configuration,
            TaskAttemptID id,
            RawKeyValueIterator reader,
            Class<KEYIN> inputKeyClass, Class<VALUEIN> inputValueClass,
            RecordWriter<KEYOUT, VALUEOUT> writer,
            OutputCommitter committer,
            RawComparator<KEYIN> comparator) throws IOException, InterruptedException {
        StatusReporter reporter = new MockStatusReporter();
        ReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context = new ReduceContextImpl<>(
                configuration, id, reader,
                reporter.getCounter("asakusafw", "inputKey"), //$NON-NLS-1$ //$NON-NLS-2$
                reporter.getCounter("asakusafw", "inputValue"), //$NON-NLS-1$ //$NON-NLS-2$
                writer, committer, reporter, comparator, inputKeyClass, inputValueClass);
        return new WrappedReducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>().getReducerContext(context);
    }

    private static final class MockStatusReporter extends StatusReporter {

        MockStatusReporter() {
            return;
        }

        @Override
        public Counter getCounter(Enum<?> name) {
            return getCounter(name.getDeclaringClass().getName(), name.name());
        }

        @Override
        public Counter getCounter(String group, String name) {
            return new GenericCounter() {
                // empty
            };
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
