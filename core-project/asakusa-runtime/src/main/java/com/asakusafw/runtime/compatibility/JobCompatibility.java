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
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.util.Progressable;

import com.asakusafw.runtime.compatibility.hadoop.JobCompatibilityHadoop;
import com.asakusafw.runtime.compatibility.hadoop.KeyValueConsumer;

/**
 * Compatibility for job APIs.
 * @since 0.5.0
 * @version 0.7.4
 */
public final class JobCompatibility {

    static final Log LOG = LogFactory.getLog(JobCompatibility.class);

    private static final JobCompatibilityHadoop DELEGATE =
            CompatibilitySelector.getImplementation(JobCompatibilityHadoop.class);

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
        return DELEGATE.newJob(conf);
    }

    /**
     * Creates a new dummy {@link TaskAttemptContext}.
     * @param conf current configuration
     * @param id target task ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptContext newTaskAttemptContext(Configuration conf, TaskAttemptID id) {
        return DELEGATE.newTaskAttemptContext(conf, id);
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
        return DELEGATE.newTaskAttemptContext(conf, id, progressable);
    }

    /**
     * Creates a new dummy job ID.
     * @return the created ID
     */
    public static JobID newJobId() {
        return DELEGATE.newJobId();
    }

    /**
     * Creates a new job ID.
     * @param id the local ID
     * @return the created ID
     * @since 0.7.1
     */
    public static JobID newJobId(int id) {
        return DELEGATE.newJobId(id);
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
        return DELEGATE.newMapTaskId(jobId, id);
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
        return DELEGATE.newReduceTaskId(jobId, id);
    }

    /**
     * Creates a new dummy task attempt ID.
     * @param taskId parent task ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptID newTaskAttemptId(TaskID taskId) {
        return DELEGATE.newTaskAttemptId(taskId);
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
        return DELEGATE.newTaskAttemptId(taskId, id);
    }

    /**
     * Sets the job ID to the job object.
     * @param job the job object
     * @param id the job ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public static void setJobId(Job job, JobID id) {
        DELEGATE.setJobId(job, id);
    }

    /**
     * Returns an task output record counter for the current context.
     * @param context the current context
     * @return corresponded record counter
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Counter getTaskOutputRecordCounter(TaskInputOutputContext<?, ?, ?, ?> context) {
        return DELEGATE.getTaskOutputRecordCounter(context);
    }

    /**
     * Returns whether the current job is on the local mode or not.
     * @param context the current context
     * @return {@code true} if the target job is running on the local mode, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.6.2
     */
    public static boolean isLocalMode(JobContext context) {
        return DELEGATE.isLocalMode(context);
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
        return DELEGATE.newMapperContext(configuration, id, reader, writer, committer, split);
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
        return DELEGATE.newReducerContext(
                configuration, id, reader, inputKeyClass, inputValueClass, writer, committer, comparator);
    }

    /**
     * Creates a new {@link TaskInputOutputContext} (for testing only).
     * @param <KEYIN> the input key type
     * @param <VALUEIN> the input value type
     * @param <KEYOUT> the output key type
     * @param <VALUEOUT> the output value type
     * @param configuration the current configuration
     * @param id the task attempt ID
     * @param consumer the output consumer
     * @return the created object
     */
    public static <KEYIN, VALUEIN, KEYOUT, VALUEOUT> TaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    newTaskOutputContext(
            Configuration configuration, TaskAttemptID id,
            KeyValueConsumer<? super KEYOUT, ? super VALUEOUT> consumer) {
        return DELEGATE.newTaskOutputContext(configuration, id, consumer);
    }
}
