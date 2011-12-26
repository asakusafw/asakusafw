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

import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.TaskAttemptContext;

/**
 * An {@link org.apache.hadoop.mapred.OutputCommitter OutputCommitter (old API)} which does nothing.
 * @since 0.2.5
 */
public class OldNullOutputCommitter extends org.apache.hadoop.mapred.OutputCommitter {

    @Override
    public void setupJob(JobContext jobContext) throws IOException {
        return;
    }

    @Override
    public void setupTask(TaskAttemptContext taskContext) throws IOException {
        return;
    }

    @Override
    public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
        return false;
    }

    @Override
    public void commitTask(TaskAttemptContext taskContext) throws IOException {
        return;
    }

    @Override
    public void abortTask(TaskAttemptContext taskContext) throws IOException {
        return;
    }
}
