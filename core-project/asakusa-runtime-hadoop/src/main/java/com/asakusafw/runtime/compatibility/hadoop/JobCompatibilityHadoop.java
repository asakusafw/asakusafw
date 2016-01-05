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
package com.asakusafw.runtime.compatibility.hadoop;

import java.io.IOException;

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

/**
 * Compatibility for job APIs.
 * Clients should not use this class directly.
 * @since 0.7.4
 */
public abstract class JobCompatibilityHadoop implements CompatibilityHadoop {

    private static final String NAME_DUMMY_JOB = "asakusafw"; //$NON-NLS-1$

    /**
     * Creates a new dummy job ID.
     * @return the created ID
     */
    public JobID newJobId() {
        return newJobId(0);
    }

    /**
     * Creates a new job ID.
     * @param id the local ID
     * @return the created ID
     */
    public JobID newJobId(int id) {
        return new JobID(NAME_DUMMY_JOB, id);
    }

    /**
     * Creates a new job.
     * @param conf current configuration
     * @return the created job
     * @throws IOException if failed to create a job client
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract Job newJob(Configuration conf) throws IOException;

    /**
     * Creates a new dummy {@link TaskAttemptContext}.
     * @param conf current configuration
     * @param id target task ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TaskAttemptContext newTaskAttemptContext(Configuration conf, TaskAttemptID id) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        return newTaskAttemptContext(conf, id, null);
    }

    /**
     * Creates a new dummy {@link TaskAttemptContext}.
     * @param conf current configuration
     * @param id target task ID
     * @param progressable delegated progressable object
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract TaskAttemptContext newTaskAttemptContext(
            Configuration conf, TaskAttemptID id, Progressable progressable);

    /**
     * Creates a new dummy task ID.
     * @param jobId parent job ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract TaskID newTaskId(JobID jobId);

    /**
     * Creates a new map task ID.
     * @param jobId parent job ID
     * @param id the local ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract TaskID newMapTaskId(JobID jobId, int id);

    /**
     * Creates a new reduce task ID.
     * @param jobId parent job ID
     * @param id the local ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract TaskID newReduceTaskId(JobID jobId, int id);

    /**
     * Creates a new dummy task attempt ID.
     * @param taskId parent task ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TaskAttemptID newTaskAttemptId(TaskID taskId) {
        return newTaskAttemptId(taskId, 0);
    }

    /**
     * Creates a new task attempt ID.
     * @param taskId parent task ID
     * @param id the local ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract TaskAttemptID newTaskAttemptId(TaskID taskId, int id);

    /**
     * Sets the job ID to the job object.
     * @param job the job object
     * @param id the job ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract void setJobId(Job job, JobID id);

    /**
     * Returns an task output record counter for the current context.
     * @param context the current context
     * @return corresponded record counter
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract Counter getTaskOutputRecordCounter(TaskInputOutputContext<?, ?, ?, ?> context);

    /**
     * Returns whether the current job is on the local mode or not.
     * @param context the current context
     * @return {@code true} if the target job is running on the local mode, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract boolean isLocalMode(JobContext context);

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
     */
    public abstract <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context newMapperContext(
            Configuration configuration,
            TaskAttemptID id,
            RecordReader<KEYIN, VALUEIN> reader,
            RecordWriter<KEYOUT, VALUEOUT> writer,
            OutputCommitter committer,
            InputSplit split) throws IOException, InterruptedException;

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
     */
    public abstract <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context newReducerContext(
            Configuration configuration,
            TaskAttemptID id,
            RawKeyValueIterator reader,
            Class<KEYIN> inputKeyClass, Class<VALUEIN> inputValueClass,
            RecordWriter<KEYOUT, VALUEOUT> writer,
            OutputCommitter committer,
            RawComparator<KEYIN> comparator) throws IOException, InterruptedException;

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
    public abstract <KEYIN, VALUEIN, KEYOUT, VALUEOUT> TaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    newTaskOutputContext(
            Configuration configuration, TaskAttemptID id,
            KeyValueConsumer<? super KEYOUT, ? super VALUEOUT> consumer);
}