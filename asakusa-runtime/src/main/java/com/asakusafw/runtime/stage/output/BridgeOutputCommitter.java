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

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A bridge implementation for Hadoop {@link OutputCommitter}.
 * @since 0.2.5
 */
public final class BridgeOutputCommitter extends OutputCommitter {

    @Override
    public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
        return true;
    }

    @Override
    public void setupTask(TaskAttemptContext taskContext) throws IOException {
        // FIXME bridge
    }

    @Override
    public void commitTask(TaskAttemptContext taskContext) throws IOException {
        // FIXME bridge
    }

    @Override
    public void abortTask(TaskAttemptContext taskContext) throws IOException {
        // FIXME bridge
    }

    @Override
    public void setupJob(JobContext jobContext) throws IOException {
        return;
    }
}
