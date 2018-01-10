/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.hadoop.BlockInfo;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryFile;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link TemporaryInputFormat}.
 */
public class TemporaryInputFormatTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple case for computing splits.
     */
    @Test
    public void splits_simple() {
        BlockMap blocks = blocks("testing", m(10));
        List<FileSplit> splits = TemporaryInputFormat.computeSplits(new Path("testing"), blocks, m(64));

        assertThat(splits, hasSize(1));
        FileSplit s0 = find(splits, 0);
        assertThat(s0.getLength(), is(m(10)));
    }

    /**
     * computing splits with already aligned blocks.
     */
    @Test
    public void splits_aligned() {
        BlockMap blocks = blocks("testing", TemporaryFile.BLOCK_SIZE, TemporaryFile.BLOCK_SIZE);
        List<FileSplit> splits = TemporaryInputFormat.computeSplits(new Path("testing"), blocks, m(64));

        assertThat(splits, hasSize(2));

        FileSplit s0 = find(splits, 0);
        assertThat(s0.getLength(), is((long) TemporaryFile.BLOCK_SIZE));

        FileSplit s1 = find(splits, TemporaryFile.BLOCK_SIZE);
        assertThat(s1.getLength(), is((long) TemporaryFile.BLOCK_SIZE));
    }

    /**
     * computing splits without unaligned blocks.
     */
    @Test
    public void splits_unaligned() {
        BlockMap blocks = blocks("testing", TemporaryFile.BLOCK_SIZE - 10, TemporaryFile.BLOCK_SIZE);
        List<FileSplit> splits = TemporaryInputFormat.computeSplits(new Path("testing"), blocks, m(128));

        assertThat(splits, hasSize(2));

        FileSplit s0 = find(splits, 0);
        assertThat(s0.getLength(), is((long) TemporaryFile.BLOCK_SIZE));

        FileSplit s1 = find(splits, TemporaryFile.BLOCK_SIZE);
        assertThat(s1.getLength(), is((long) TemporaryFile.BLOCK_SIZE - 10));
    }

    /**
     * computing splits with aligned blocks plus.
     */
    @Test
    public void splits_aligned_rest() {
        BlockMap blocks = blocks("testing", TemporaryFile.BLOCK_SIZE, TemporaryFile.BLOCK_SIZE + 10);
        List<FileSplit> splits = TemporaryInputFormat.computeSplits(new Path("testing"), blocks, m(64));

        assertThat(splits, hasSize(2));

        FileSplit s0 = find(splits, 0);
        assertThat(s0.getLength(), is((long) TemporaryFile.BLOCK_SIZE));

        FileSplit s1 = find(splits, TemporaryFile.BLOCK_SIZE);
        assertThat(s1.getLength(), is((long) TemporaryFile.BLOCK_SIZE + 10));
    }

    /**
     * computing splits with forcibly splitting.
     */
    @Test
    public void splits_force() {
        BlockMap blocks = blocks("testing", TemporaryFile.BLOCK_SIZE * 10);
        List<FileSplit> splits = TemporaryInputFormat.computeSplits(
                new Path("testing"), blocks, TemporaryFile.BLOCK_SIZE + 1);

        assertThat(splits, hasSize(5));

        FileSplit s0 = find(splits, TemporaryFile.BLOCK_SIZE * 0);
        assertThat(s0.getLength(), is((long) TemporaryFile.BLOCK_SIZE * 2));
        FileSplit s1 = find(splits, TemporaryFile.BLOCK_SIZE * 2);
        assertThat(s1.getLength(), is((long) TemporaryFile.BLOCK_SIZE * 2));
        FileSplit s2 = find(splits, TemporaryFile.BLOCK_SIZE * 4);
        assertThat(s2.getLength(), is((long) TemporaryFile.BLOCK_SIZE * 2));
        FileSplit s3 = find(splits, TemporaryFile.BLOCK_SIZE * 6);
        assertThat(s3.getLength(), is((long) TemporaryFile.BLOCK_SIZE * 2));
        FileSplit s4 = find(splits, TemporaryFile.BLOCK_SIZE * 8);
        assertThat(s4.getLength(), is((long) TemporaryFile.BLOCK_SIZE * 2));
    }

    /**
     * computing splits w/ suppress.
     */
    @Test
    public void splits_suppress() {
        BlockMap blocks = blocks("testing", TemporaryFile.BLOCK_SIZE * 10);
        List<FileSplit> splits = TemporaryInputFormat.computeSplits(new Path("testing"), blocks, 0);

        assertThat(splits, hasSize(1));
        FileSplit s0 = find(splits, 0);
        assertThat(s0.getLength(), is((long) TemporaryFile.BLOCK_SIZE * 10));
    }

    /**
     * Simple case for record readers.
     * @throws Exception if failed
     */
    @Test
    public void reader_simple() throws Exception {
        Configuration conf = new ConfigurationProvider().newInstance();
        FileStatus stat = write(conf, 1);
        try (RecordReader<NullWritable, Text> reader = TemporaryInputFormat.createRecordReader()) {
            reader.initialize(
                    new FileSplit(stat.getPath(), 0, stat.getLen(), null),
                    new TaskAttemptContextImpl(conf, new TaskAttemptID()));

            assertThat(reader.nextKeyValue(), is(true));
            assertThat(reader.getCurrentValue(), is(new Text("Hello, world!")));

            assertThat(reader.nextKeyValue(), is(false));
            assertThat((double) reader.getProgress(), closeTo(1.0, 0.01));
        }
    }

    private FileStatus write(Configuration conf, int count) throws IOException {
        Path path = new Path(folder.newFile().toURI());
        try (ModelOutput<Text> output = TemporaryStorage.openOutput(conf, Text.class, path)) {
            Text buffer = new Text("Hello, world!");
            for (int i = 0; i < count; i++) {
                output.write(buffer);
            }
        }
        return path.getFileSystem(conf).getFileStatus(path);
    }

    private long m(long value) {
        return value * 1024 * 1024;
    }

    private FileSplit find(List<FileSplit> splits, long start) {
        for (FileSplit split : splits) {
            if (split.getStart() == start) {
                return split;
            }
        }
        throw new AssertionError(start);
    }

    private BlockMap blocks(String path, long... blockSizes) {
        List<BlockInfo> blockList = new ArrayList<>();
        long totalSize = 0;
        for (long blockSize : blockSizes) {
            long next = totalSize + blockSize;
            blockList.add(new BlockInfo(totalSize, next, null));
            totalSize = next;
        }
        return BlockMap.create(path, totalSize, blockList, false);
    }
}
