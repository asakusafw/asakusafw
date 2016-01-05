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
package com.asakusafw.runtime.compatibility.hadoop1;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapred.Task;
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
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.util.Progressable;

import com.asakusafw.runtime.compatibility.hadoop.JobCompatibilityHadoop;
import com.asakusafw.runtime.compatibility.hadoop.KeyValueConsumer;

/**
 * Compatibility for job APIs (Hadoop {@code 1.x}).
 * Clients should not use this class directly.
 * @since 0.7.4
 */
public final class JobCompatibilityHadoop1 extends JobCompatibilityHadoop {

    static final Log LOG = LogFactory.getLog(JobCompatibilityHadoop1.class);

    private static final String KEY_JOBTRACKER_ADDRESS = "mapred.job.tracker"; //$NON-NLS-1$

    private static final Method SET_JOB_ID;
    static {
        Method method = null;
        for (Class<?> aClass = Job.class; aClass != null; aClass = aClass.getSuperclass()) {
            try {
                method = aClass.getDeclaredMethod("setJobID", JobID.class); //$NON-NLS-1$
                method.setAccessible(true);
                break;
            } catch (Exception e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(MessageFormat.format(
                            "failed to detect {0}.setJobID() method", //$NON-NLS-1$
                            aClass.getName()), e);
                }
            }
        }
        if (method != null) {
            SET_JOB_ID = method;
        } else {
            SET_JOB_ID = null;
        }
    }

    @Override
    public Job newJob(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        return new Job(conf);
    }

    @Override
    public TaskAttemptContext newTaskAttemptContext(
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
            return new TaskAttemptContext(conf, id);
        }
        return new TaskAttemptContext(conf, id) {
            @Override
            public void progress() {
                progressable.progress();
                super.progress();
            }
        };
    }

    @Override
    public TaskID newTaskId(JobID jobId) {
        return newMapTaskId(jobId, 0);
    }

    @Override
    public TaskID newMapTaskId(JobID jobId, int id) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        return new TaskID(jobId, true, id);
    }

    @Override
    public TaskID newReduceTaskId(JobID jobId, int id) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null"); //$NON-NLS-1$
        }
        return new TaskID(jobId, false, id);
    }

    @Override
    public TaskAttemptID newTaskAttemptId(TaskID taskId, int id) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId must not be null"); //$NON-NLS-1$
        }
        return new TaskAttemptID(taskId, id);
    }

    @Override
    public void setJobId(Job job, JobID id) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (SET_JOB_ID != null) {
            try {
                SET_JOB_ID.invoke(job, id);
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(e);
            } catch (InvocationTargetException e) {
                throw new UnsupportedOperationException(e.getCause());
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Counter getTaskOutputRecordCounter(TaskInputOutputContext<?, ?, ?, ?> context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (context.getTaskAttemptID().isMap()) {
            return context.getCounter(Task.Counter.MAP_OUTPUT_RECORDS);
        } else {
            return context.getCounter(Task.Counter.REDUCE_OUTPUT_RECORDS);
        }
    }

    @Override
    public boolean isLocalMode(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        String mode = getJobMode(context);
        if (mode == null) {
            LOG.warn(MessageFormat.format(
                    "Inconsistent configuration: the default value of \"{0}\" is not set",
                    KEY_JOBTRACKER_ADDRESS));
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Detecting local mode configuraiton: {0}={1}", //$NON-NLS-1$
                    KEY_JOBTRACKER_ADDRESS, mode));
        }
        return isRemoteJob(mode) == false;
    }

    private static String getJobMode(JobContext context) {
        return context.getConfiguration().get(KEY_JOBTRACKER_ADDRESS);
    }

    private static boolean isRemoteJob(String modeName) {
        return modeName != null && modeName.equals("local") == false; //$NON-NLS-1$
    }

    @Override
    public <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context newMapperContext(
            Configuration configuration,
            TaskAttemptID id,
            RecordReader<KEYIN, VALUEIN> reader,
            RecordWriter<KEYOUT, VALUEOUT> writer,
            OutputCommitter committer,
            InputSplit split) throws IOException, InterruptedException {
        Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> mapper = new Mapper<>();
        return mapper.new Context(
                configuration,
                id,
                reader,  writer, committer,
                new MockStatusReporter(),
                split);
    }

    @Override
    public <KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>.Context newReducerContext(
            Configuration configuration,
            TaskAttemptID id,
            RawKeyValueIterator reader,
            Class<KEYIN> inputKeyClass, Class<VALUEIN> inputValueClass,
            RecordWriter<KEYOUT, VALUEOUT> writer,
            OutputCommitter committer,
            RawComparator<KEYIN> comparator) throws IOException, InterruptedException {
        Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> reducer = new Reducer<>();
        StatusReporter reporter = new MockStatusReporter();
        return reducer.new Context(
                configuration,
                id,
                reader,
                reporter.getCounter("asakusafw", "inputKey"), //$NON-NLS-1$ //$NON-NLS-2$
                reporter.getCounter("asakusafw", "inputValue"), //$NON-NLS-1$ //$NON-NLS-2$
                writer, committer, reporter, comparator,
                inputKeyClass, inputValueClass);
    }

    @Override
    public <KEYIN, VALUEIN, KEYOUT, VALUEOUT> TaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
    newTaskOutputContext(
            Configuration configuration, TaskAttemptID id,
            KeyValueConsumer<? super KEYOUT, ? super VALUEOUT> consumer) {
        return new MockTaskInputOutputContext<>(configuration, id, consumer);
    }

    private static class MockTaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
            extends TaskInputOutputContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

        private final KeyValueConsumer<? super KEYOUT, ? super VALUEOUT> consumer;

        public MockTaskInputOutputContext(
                Configuration conf, TaskAttemptID taskId,
                KeyValueConsumer<? super KEYOUT, ? super VALUEOUT> consumer) {
            super(conf, taskId, null, null, new MockStatusReporter());
            this.consumer = consumer;
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

        @Override
        public void write(KEYOUT key, VALUEOUT value) {
            consumer.consume(key, value);
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
