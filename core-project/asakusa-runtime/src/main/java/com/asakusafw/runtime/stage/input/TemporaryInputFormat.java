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
package com.asakusafw.runtime.stage.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.BlockInfo;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.temporary.TemporaryFile;
import com.asakusafw.runtime.stage.temporary.TemporaryFileInput;

/**
 * A temporary input format.
 * @param <T> data type
 * @since 0.2.5
 * @version 0.7.0
 */
public final class TemporaryInputFormat<T> extends InputFormat<NullWritable, T> {

    static final Log LOG = LogFactory.getLog(TemporaryInputFormat.class);

    static final String KEY_DEFAULT_SPLIT_SIZE = "com.asakusafw.stage.input.temporary.blockSize"; //$NON-NLS-1$

    static final long DEFAULT_SPLIT_SIZE = 128L * 1024 * 1024;

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        return getSplits(context.getConfiguration(), getInputPaths(context));
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
        List<Path> paths = new ArrayList<>();
        for (StageInput input : inputList) {
            paths.add(new Path(input.getPathString()));
        }
        return getSplits(context.getConfiguration(), paths);
    }

    private List<InputSplit> getSplits(Configuration configuration, List<Path> paths) throws IOException {
        long splitSize = configuration.getLong(KEY_DEFAULT_SPLIT_SIZE, DEFAULT_SPLIT_SIZE);
        List<InputSplit> results = new ArrayList<>();
        for (Path path : paths) {
            FileSystem fs = path.getFileSystem(configuration);
            FileStatus[] statuses = fs.globStatus(path);
            if (statuses == null) {
                continue;
            }
            for (FileStatus status : statuses) {
                BlockMap blockMap = BlockMap.create(
                        status.getPath().toString(),
                        status.getLen(),
                        BlockMap.computeBlocks(fs, status),
                        false);
                results.addAll(computeSplits(status.getPath(), blockMap, splitSize));
            }
        }
        return results;
    }

    /**
     * Compute input splits for the target file.
     * @param path the target file path
     * @param blockMap the file block map
     * @param splitSize the expected split size, or {@code <= 0} to prevent splits
     * @return the computed input splits for the file
     */
    static List<FileSplit> computeSplits(Path path, BlockMap blockMap, long splitSize) {
        long align = splitSize;
        if (splitSize > 0) {
            long remain = splitSize % TemporaryFile.BLOCK_SIZE;
            if (remain != 0) {
                align += TemporaryFile.BLOCK_SIZE - remain;
            }
        }
        long size = blockMap.getFileSize();
        long start = 0;
        List<FileSplit> results = new ArrayList<>();
        for (BlockInfo block : blockMap.getBlocks()) {
            assert start % TemporaryFile.BLOCK_SIZE == 0;
            long end = block.getEnd();
            if (end < start) {
                continue;
            }
            long remain = end % TemporaryFile.BLOCK_SIZE;
            if (remain != 0) {
                end = Math.min(size, end + (TemporaryFile.BLOCK_SIZE - remain));
            }
            results.addAll(createSplits(path, blockMap, start, end, align));
            start = end;
        }
        return results;
    }

    private static List<FileSplit> createSplits(
            Path path, BlockMap blockMap, long start, long end, long splitSize) {
        if (start >= end) {
            return Collections.emptyList();
        }
        if (splitSize <= 0) {
            FileSplit split = getSplit(blockMap, path, start, end);
            return Collections.singletonList(split);
        }
        long threashold = (long) (splitSize * 1.2);
        List<FileSplit> results = new ArrayList<>();
        long current = start;
        while (current < end) {
            long next;
            if (end - current < threashold) {
                next = end;
            } else {
                next = current + splitSize;
            }
            FileSplit split = getSplit(blockMap, path, current, next);
            results.add(split);
            current = next;
        }
        return results;
    }

    private static FileSplit getSplit(BlockMap blockMap, Path path, long start, long end) {
        DirectInputFragment f = blockMap.get(start, end);
        List<String> owners = f.getOwnerNodeNames();
        FileSplit split = new FileSplit(
                path, start, end - start,
                owners.toArray(new String[owners.size()]));
        return split;
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

    /**
     * Returns input paths.
     * @param context current job
     * @return the input paths, or an empty list if they are not set
     * @throws IOException if failed to resolve paths
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.0
     */
    public static List<Path> getInputPaths(JobContext context) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        Path[] paths = FileInputFormat.getInputPaths(context);
        if (paths == null || paths.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(paths);
    }

    @Override
    public RecordReader<NullWritable, T> createRecordReader(
            InputSplit split,
            TaskAttemptContext context) throws IOException, InterruptedException {
        FileSplit s = (FileSplit) split;
        assert s.getStart() % TemporaryFile.BLOCK_SIZE == 0;
        assert s.getStart() > 0 || s.getLength() > 0;
        return createRecordReader();
    }

    /**
     * Create a record reader for this input format.
     * @param <T> the value type
     * @return the record reader
     */
    @SuppressWarnings("unchecked")
    static <T> RecordReader<NullWritable, T> createRecordReader() {
        return (RecordReader<NullWritable, T>) new Reader<>();
    }

    private static final class Reader<T extends Writable> extends RecordReader<NullWritable, T> {

        private long size;

        private TemporaryFileInput<T> input;

        private T value;

        Reader() {
            return;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
            FileSplit s = (FileSplit) split;
            this.size = s.getLength();
            Path path = s.getPath();
            FileSystem fs = path.getFileSystem(context.getConfiguration());
            int blocks = computeBlocks(s);
            FSDataInputStream stream = fs.open(path);
            boolean succeed = false;
            try {
                if (s.getStart() != 0) {
                    assert s.getStart() % TemporaryFile.BLOCK_SIZE == 0;
                    stream.seek(s.getStart());
                }
                this.input = (TemporaryFileInput<T>) new TemporaryFileInput<>(stream, blocks);
                Class<?> aClass = context.getConfiguration().getClassByName(input.getDataTypeName());
                this.value = (T) ReflectionUtils.newInstance(aClass, context.getConfiguration());
                succeed = true;
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            } finally {
                if (succeed == false) {
                    stream.close();
                }
            }
        }

        private int computeBlocks(FileSplit s) {
            long length = s.getLength() + TemporaryFile.BLOCK_SIZE - 1;
            return (int) (length / TemporaryFile.BLOCK_SIZE);
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            return input.readTo(value);
        }

        @Override
        public NullWritable getCurrentKey() throws IOException, InterruptedException {
            return NullWritable.get();
        }

        @Override
        public T getCurrentValue() throws IOException, InterruptedException {
            return value;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            long current = input.getCurrentBlock() * (long) TemporaryFile.BLOCK_SIZE;
            current += input.getPositionInBlock();
            return (float) current / size;
        }

        @Override
        public void close() throws IOException {
            if (input != null) {
                input.close();
            }
        }
    }
}
