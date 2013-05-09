/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.io.InterruptedIOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.TaskAttemptContext;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.JobStatus.State;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.util.Progressable;

import com.asakusafw.runtime.compatibility.JobCompatibility;

/**
 * Bridge implementation between
 * {@link org.apache.hadoop.mapred.OutputCommitter OutputCommitter (old API)}
 * and {@link StageOutputFormat#getOutputCommitter(org.apache.hadoop.mapreduce.TaskAttemptContext)}.
 * @since 0.2.5
 */
public class LegacyBridgeOutputCommitter extends org.apache.hadoop.mapred.OutputCommitter {

    static final Log LOG = LogFactory.getLog(LegacyBridgeOutputCommitter.class);

    private final StageOutputFormat format = new StageOutputFormat();

    @Override
    public void setupJob(JobContext jobContext) throws IOException {
        org.apache.hadoop.mapreduce.TaskAttemptContext taskContext = toTaskAttemptContext(jobContext);
        committer(taskContext).setupJob(taskContext);
    }

    @Override
    public void abortJob(JobContext jobContext, int status) throws IOException {
        JobStatus.State state = convert(status);
        org.apache.hadoop.mapreduce.TaskAttemptContext taskContext = toTaskAttemptContext(jobContext);
        committer(taskContext).abortJob(taskContext, state);
    }

    private State convert(int status) {
        for (JobStatus.State each : JobStatus.State.values()) {
            if (each.getValue() == status) {
                return each;
            }
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void cleanupJob(JobContext jobContext) throws IOException {
        org.apache.hadoop.mapreduce.TaskAttemptContext taskContext = toTaskAttemptContext(jobContext);
        committer(taskContext).cleanupJob(taskContext);
    }

    @Override
    public void commitJob(JobContext jobContext) throws IOException {
        org.apache.hadoop.mapreduce.TaskAttemptContext taskContext = toTaskAttemptContext(jobContext);
        committer(taskContext).commitJob(taskContext);
    }

    @Override
    public void setupTask(TaskAttemptContext taskContext) throws IOException {
        committer(taskContext).setupTask(taskContext);
    }

    @Override
    public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
        return committer(taskContext).needsTaskCommit(taskContext);
    }

    @Override
    public void commitTask(TaskAttemptContext taskContext) throws IOException {
        committer(taskContext).commitTask(taskContext);
    }

    @Override
    public void abortTask(TaskAttemptContext taskContext) throws IOException {
        committer(taskContext).abortTask(taskContext);
    }

    private OutputCommitter committer(org.apache.hadoop.mapreduce.TaskAttemptContext taskContext) throws IOException {
        try {
            return format.getOutputCommitter(taskContext);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException().initCause(e);
        }
    }

    private org.apache.hadoop.mapreduce.TaskAttemptContext toTaskAttemptContext(JobContext jobContext) {
        assert jobContext != null;
        final Progressable progressable = jobContext.getProgressible();
        if (progressable == null) {
            LOG.warn(MessageFormat.format(
                    "JobContext has no progressable object: {0}",
                    jobContext.getClass().getName()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Progressable object is found (jobId={0}, object={1})",
                    jobContext.getJobID(),
                    progressable));
        }
        TaskAttemptID id = JobCompatibility.newTaskAttemptId(JobCompatibility.newTaskId(jobContext.getJobID()));
        return JobCompatibility.newTaskAttemptContext(jobContext.getConfiguration(), id, progressable);
    }
}
