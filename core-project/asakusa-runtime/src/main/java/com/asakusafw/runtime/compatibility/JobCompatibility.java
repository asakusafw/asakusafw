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
package com.asakusafw.runtime.compatibility;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.MRConfig;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.counters.GenericCounter;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.mapreduce.task.TaskInputOutputContextImpl;
import org.apache.hadoop.util.Progressable;

/**
 * Compatibility for job APIs.
 * @since 0.5.0
 * @version 0.6.2
 */
public final class JobCompatibility {

    static final Log LOG = LogFactory.getLog(JobCompatibility.class);

    private static final String KEY_JOBTRACKER_ADDRESS = "mapred.job.tracker";

    private static final Constructor<TaskID> TASK_ID_MR2;
    static {
        Constructor<TaskID> ctor;
        try {
            ctor = TaskID.class.getConstructor(JobID.class, TaskType.class, int.class);
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled() && isMRv1() == false) {
                LOG.debug("Failed to detect constructor: TaskID(JobID, TaskType, int)", e);
            }
            ctor = null;
        } catch (Exception e) {
            LOG.warn("Failed to detect constructor: TaskID(JobID, TaskType, int)", e);
            ctor = null;
        }
        TASK_ID_MR2 = ctor;
    }

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
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
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
        if (TASK_ID_MR2 != null) {
            try {
                return TASK_ID_MR2.newInstance(jobId, TaskType.JOB_SETUP, 0);
            } catch (Exception e) {
                LOG.warn("Failed to invoke: TaskID(JobID, TaskType, int)", e);
                // fall-through...
            }
        }
        // NOTE: for Hadoop 2.x MR1
        @SuppressWarnings("deprecation")
        TaskID result = new TaskID(jobId, true, 0);
        return result;
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
        if (isMRv1()) {
            return isLocalModeMRv1(context);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "{0}={1}",
                    MRConfig.FRAMEWORK_NAME,
                    context.getConfiguration().get(MRConfig.FRAMEWORK_NAME)));
        }
        String name = context.getConfiguration().get(MRConfig.FRAMEWORK_NAME, MRConfig.LOCAL_FRAMEWORK_NAME);
        return name.equals(MRConfig.LOCAL_FRAMEWORK_NAME);
    }

    private static boolean isLocalModeMRv1(JobContext context) {
        assert context != null;
        String mode = getJobMode(context);
        if (mode == null) {
            LOG.warn(MessageFormat.format(
                    "Inconsistent configuration: the default value of \"{0}\" is not set",
                    KEY_JOBTRACKER_ADDRESS));
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Detecting local mode configuraiton: {0}={1}",
                    KEY_JOBTRACKER_ADDRESS, mode));
        }
        return isRemoteJob(mode) == false;
    }

    private static String getJobMode(JobContext context) {
        return context.getConfiguration().get(KEY_JOBTRACKER_ADDRESS);
    }

    private static boolean isRemoteJob(String modeName) {
        return modeName != null && modeName.equals("local") == false;
    }

    private static boolean isMRv1() {
        return CoreCompatibility.FrameworkVersion.get() == CoreCompatibility.FrameworkVersion.HADOOP_V2_MR1;
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
            extends TaskInputOutputContextImpl<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

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

    // NOTE: Compatibility between Hadoop 1.0.x and 1.1.0
    private interface StatusReporterInterfaceExtension {
        float getProgress();
    }
}
