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
package com.asakusafw.runtime.stage.input;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A bridge implementation for Hadoop {@link InputFormat}.
 * @since 0.2.5
 */
public final class BridgeInputFormat extends InputFormat<NullWritable, Writable> {

    @Override
    @Deprecated
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    /**
     * Computes and returns splits for the specified inputs.
     * @param context current job context
     * @param input target input list
     * @return the computed splits
     * @throws IOException if failed to compute splits
     * @throws InterruptedException if interrupted while computing inputs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<InputSplit> getSplits(
            JobContext context,
            List<StageInputDriver.Input> input) throws IOException, InterruptedException {
        // FIXME bridge
        throw new UnsupportedOperationException();
    }

    @Override
    public RecordReader<NullWritable, Writable> createRecordReader(
            InputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        // FIXME connect to nakamise
        throw new UnsupportedOperationException();
    }
}
