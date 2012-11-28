/**
 * Copyright 2012 Asakusa Framework Team.
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.Task;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.util.Progressable;

/**
 * Compatibility for job APIs.
 * @since 0.5.0
 */
public final class JobCompatibility {

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
        return new Job(conf);
    }

    /**
     * Creates a new dummy {@link TaskAttemptContext}.
     * @param conf current configuration
     * @param id target task ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptContext newTaskAttemptContext(Configuration conf, TaskAttemptID id) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        return new TaskAttemptContext(conf, id);
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
            final Progressable progressable) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (progressable == null) {
            return newTaskAttemptContext(conf, id);
        }
        return new TaskAttemptContext(conf, id) {
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
        return new JobID("dummyjob", 0);
    }

    /**
     * Creates a new dummy task ID.
     * @param jobId parent job ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskID newTaskId(JobID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        return new TaskID(jobId, true, 0);
    }

    /**
     * Creates a new dummy task attempt ID.
     * @param taskId parent task ID
     * @return the created ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static TaskAttemptID newTaskAttemptId(TaskID taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId must not be null"); //$NON-NLS-1$
        }
        return new TaskAttemptID(taskId, 0);
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
        if (context.getTaskAttemptID().isMap()) {
            return context.getCounter(Task.Counter.MAP_OUTPUT_RECORDS);
        } else {
            return context.getCounter(Task.Counter.REDUCE_OUTPUT_RECORDS);
        }
    }

    /**
     * A stub class of {@link TaskInputOutputContextStub}.
     * @param <KEYIN> the input key type
     * @param <VALUEIN> the input value type
     * @param <KEYOUT> the output key type
     * @param <VALUEOUT> the output value type
     * @since 0.5.0
     */
    public abstract static class TaskInputOutputContextStub<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
            extends TaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

        /**
         * Creates a new dummy instance.
         */
        public TaskInputOutputContextStub() {
            super(
                    new Configuration(false),
                    newTaskAttemptId(newTaskId(newJobId())),
                    null,
                    null,
                    new MockStatusReporter());
        }

        /**
         * Creates a new instance.
         * @param conf current configuration
         * @param taskId target task ID
         * @param output target record writer
         * @param committer target committer
         * @param reporter target reporter
         */
        protected TaskInputOutputContextStub(
                Configuration conf,
                TaskAttemptID taskId,
                RecordWriter<KEYOUT, VALUEOUT> output,
                OutputCommitter committer,
                StatusReporter reporter) {
            super(conf, taskId, output, committer, reporter);
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            return false;
        }

        @Override
        public KEYIN getCurrentKey() throws IOException, InterruptedException {
            return null;
        }

        @Override
        public VALUEIN getCurrentValue() throws IOException, InterruptedException {
            return null;
        }
    }

    private static final class MockStatusReporter extends StatusReporter implements StatusReporterInterfaceExtension {

        MockStatusReporter() {
            return;
        }

        @Override
        public Counter getCounter(Enum<?> name) {
            return getCounter(name.getDeclaringClass().getName(), name.name());
        }

        @Override
        public Counter getCounter(String group, String name) {
            return new Counter() {
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

    // NOTE: Compatibility between Hadoop 1.0.x and 1.1.0
    private interface StatusReporterInterfaceExtension {
        float getProgress();
    }
}
