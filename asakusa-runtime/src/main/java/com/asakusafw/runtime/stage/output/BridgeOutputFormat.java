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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.JobStatus.State;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.asakusafw.runtime.stage.temporary.TemporaryOutputFormat;

/**
 * A bridge implementation for Hadoop {@link OutputFormat}.
 * @since 0.2.5
 */
public final class BridgeOutputFormat extends OutputFormat<Object, Object> {

    static final Log LOG = LogFactory.getLog(BridgeOutputFormat.class);

    private OutputCommitter outputCommitter;

    private final FileOutputFormat<Object, Object> dummyFileOutputFormat = new EmptyFileOutputFormat();

    private final TemporaryOutputFormat<Object> temporaryOutputFormat = new TemporaryOutputFormat<Object>();

    @Override
    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        if (isFileOutputEnabled(context)) {
            dummyFileOutputFormat.checkOutputSpecs(context);
        }
        if (isTemporaryOutputEnabled(context)) {
            temporaryOutputFormat.checkOutputSpecs(context);
        }
    }

    @Override
    public RecordWriter<Object, Object> getRecordWriter(
            TaskAttemptContext context) throws IOException, InterruptedException {
        return dummyFileOutputFormat.getRecordWriter(context);
    }

    @Override
    public OutputCommitter getOutputCommitter(
            TaskAttemptContext context) throws IOException, InterruptedException {
        synchronized (this) {
            if (outputCommitter == null) {
                outputCommitter = createOutputCommitter(context);
            }
            return outputCommitter;
        }
    }

    private OutputCommitter createOutputCommitter(TaskAttemptContext context) throws IOException {
        assert context != null;
        Set<OutputCommitter> components = new LinkedHashSet<OutputCommitter>();
        BridgeOutputCommitter bridge = new BridgeOutputCommitter();
        components.add(bridge);
        if (isFileOutputEnabled(context)) {
            OutputCommitter committer = dummyFileOutputFormat.getOutputCommitter(context);
            if (components.contains(committer) == false) {
                components.add(committer);
            }
        }
        if (isTemporaryOutputEnabled(context)) {
            FileOutputCommitter committer = temporaryOutputFormat.getOutputCommitter(context);
            if (components.contains(committer) == false) {
                components.add(committer);
            }
        }
        return new CombinedOutputCommitter(new ArrayList<OutputCommitter>(components));
    }

    private boolean isFileOutputEnabled(JobContext context) {
        assert context != null;
        return FileOutputFormat.getOutputPath(context) != null;
    }

    private boolean isTemporaryOutputEnabled(JobContext context) {
        assert context != null;
        return TemporaryOutputFormat.getOutputPath(context) != null;
    }

    private static final class CombinedOutputCommitter extends OutputCommitter {

        private final List<OutputCommitter> components;

        CombinedOutputCommitter(List<OutputCommitter> components) {
            assert components != null;
            this.components = components;
        }

        @Override
        public void setupJob(JobContext jobContext) throws IOException {
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    component.setupJob(jobContext);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to set up job (JobID={0})",
                            jobContext.getJobID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public void commitJob(JobContext jobContext) throws IOException {
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    component.commitJob(jobContext);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to  (JobID={0})",
                            jobContext.getJobID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public void abortJob(JobContext jobContext, State state) throws IOException {
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    component.abortJob(jobContext, state);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to  (JobID={0})",
                            jobContext.getJobID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public void setupTask(TaskAttemptContext taskContext) throws IOException {
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    component.setupTask(taskContext);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to  (JobID={0}, TaskAttemptID={1})",
                            taskContext.getJobID(),
                            taskContext.getTaskAttemptID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
            boolean results = false;
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    results |= component.needsTaskCommit(taskContext);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to judge whether task should be committed (JobID={0}, TaskAttemptID={1})",
                            taskContext.getJobID(),
                            taskContext.getTaskAttemptID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
            return results;
        }

        @Override
        public void commitTask(TaskAttemptContext taskContext) throws IOException {
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    component.commitTask(taskContext);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to commit task (JobID={0}, TaskAttemptID={1})",
                            taskContext.getJobID(),
                            taskContext.getTaskAttemptID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public void abortTask(TaskAttemptContext taskContext) throws IOException {
            IOException exception = null;
            for (OutputCommitter component : components) {
                try {
                    component.abortTask(taskContext);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to abort task (JobID={0}, TaskAttemptID={1})",
                            taskContext.getJobID(),
                            taskContext.getTaskAttemptID()), e);
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
    }
}
