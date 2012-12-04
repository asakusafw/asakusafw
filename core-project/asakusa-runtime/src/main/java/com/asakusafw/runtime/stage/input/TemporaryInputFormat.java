/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.stage.StageInput;

/**
 * A temporary input format.
 * @param <T> data type
 * @since 0.2.5
 */
public final class TemporaryInputFormat<T> extends InputFormat<NullWritable, T> {

    static final Log LOG = LogFactory.getLog(TemporaryInputFormat.class);

    private final FileInputFormat<NullWritable, T> bridge = new SequenceFileInputFormat<NullWritable, T>();

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        return bridge.getSplits(context);
    }

    /**
     * Computes and returns splits for the specified inputs.
     * @param context current job context
     * @param inputList target input list
     * @return the computed splits
     * @throws IOException if failed to compute splits
     * @throws InterruptedException if interrupted while computing inputs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<InputSplit> getSplits(
            JobContext context,
            List<StageInput> inputList) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (inputList == null) {
            throw new IllegalArgumentException("inputList must not be null"); //$NON-NLS-1$
        }
        List<Path> paths = new ArrayList<Path>();
        for (StageInput input : inputList) {
            paths.add(new Path(input.getPathString()));
        }
        Job job = JobCompatibility.newJob(context.getConfiguration());
        setInputPaths(job, paths);
        return bridge.getSplits(job);
    }

    /**
     * Configures input paths.
     * @param job current job
     * @param paths source paths
     * @throws IOException if failed to resolve paths
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void setInputPaths(Job job, List<Path> paths) throws IOException {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (paths == null) {
            throw new IllegalArgumentException("paths must not be null"); //$NON-NLS-1$
        }
        FileInputFormat.setInputPaths(job, paths.toArray(new Path[paths.size()]));
    }

    @Override
    public RecordReader<NullWritable, T> createRecordReader(
            InputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        return bridge.createRecordReader(split, context);
    }
}
