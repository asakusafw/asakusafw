/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link SequenceFileFormat}.
 */
public class SequenceFileFormatTest {

    /**
     * Creates a new instance.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private Configuration conf;

    private MockFormat format;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        this.conf = new Configuration(true);
        this.format = new MockFormat();
        format.setConf(conf);
    }

    /**
     * Test for input.
     * @throws Exception if failed
     */
    @Test
    public void input() throws Exception {
        final int count = 10000;
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class);
        try {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                k.set(i);
                v.set("Hello, world at " + i);
                writer.append(k, v);
            }
        } finally {
            writer.close();
        }

        ModelInput<StringOption> in = format.createInput(
                StringOption.class,
                fs,
                path,
                0,
                fs.getFileStatus(path).getLen(),
                new Counter());
        try {
            StringOption value = new StringOption();
            for (int i = 0; i < count; i++) {
                String answer = "Hello, world at " + i;
                assertThat(answer, in.readTo(value), is(true));
                assertThat(value.getAsString(), is(answer));
            }
            assertThat("eof", in.readTo(value), is(false));
        } finally {
            in.close();
        }
    }

    /**
     * Test for input.
     * @throws Exception if failed
     */
    @Test
    public void input_fragment() throws Exception {
        final int count = 30000;
        Random rand = new Random();
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class);
        try {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                k.set(i);
                v.set("Hello, world at " + i);
                writer.append(k, v);
            }
        } finally {
            writer.close();
        }

        long fileLen = fs.getFileStatus(path).getLen();
        StringOption value = new StringOption();
        for (int attempt = 0; attempt < 5; attempt++) {
            int index = 0;
            long offset = 0;
            while (offset < fileLen) {
                long length = SequenceFile.SYNC_INTERVAL * (rand.nextInt(10) + 2);
                length = Math.min(length, fileLen - offset);
                ModelInput<StringOption> in = format.createInput(
                        StringOption.class,
                        fs,
                        path,
                        offset,
                        length,
                        new Counter());
                try {
                    while (in.readTo(value)) {
                        String answer = "Hello, world at " + index;
                        assertThat(value.getAsString(), is(answer));
                        index++;
                    }
                    assertThat("eof", in.readTo(value), is(false));
                } finally {
                    in.close();
                }
                offset += length;
            }
            assertThat(index, is(count));
        }
    }

    /**
     * Test for input.
     * @throws Exception if failed
     */
    @Test
    public void input_largerecord() throws Exception {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            buf.append("Hello, world!");
        }
        Text record = new Text(buf.toString());

        final int count = 5;
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class);
        try {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                k.set(i);
                v.set(record);
                writer.append(k, v);
            }
        } finally {
            writer.close();
        }

        long fileLen = fs.getFileStatus(path).getLen();
        StringOption value = new StringOption();
        int index = 0;
        long offset = 0;
        while (offset < fileLen) {
            System.out.println(index);
            long length = SequenceFile.SYNC_INTERVAL * 2;
            length = Math.min(length, fileLen - offset);
            ModelInput<StringOption> in = format.createInput(
                    StringOption.class,
                    fs,
                    path,
                    offset,
                    length,
                    new Counter());
            try {
                while (in.readTo(value)) {
                    assertThat(value.get(), is(record));
                    index++;
                }
                assertThat("eof", in.readTo(value), is(false));
            } finally {
                in.close();
            }
            offset += length;
        }
        assertThat(index, is(count));
    }

    /**
     * Test for input empty file.
     * @throws Exception if failed
     */
    @Test
    public void input_empty() throws Exception {
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        fs.create(path).close();
        ModelInput<StringOption> in = format.createInput(
                StringOption.class,
                fs,
                path,
                0,
                fs.getFileStatus(path).getLen(),
                new Counter());
        try {
            assertThat("eof", in.readTo(new StringOption()), is(false));
        } finally {
            in.close();
        }
    }

    /**
     * Test for input invalid file.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void input_invalid() throws Exception {
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        FSDataOutputStream output = fs.create(path);
        try {
            output.writeUTF("Hello, world!");
        } finally {
            output.close();
        }
        ModelInput<StringOption> in = format.createInput(
                StringOption.class,
                fs,
                path,
                0,
                fs.getFileStatus(path).getLen(),
                new Counter());
        in.close();
    }

    /**
     * Test method for output.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void output() throws Exception {
        final int count = 10000;
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        ModelOutput<StringOption> out = format.createOutput(StringOption.class, fs, path, new Counter());
        try {
            StringOption value = new StringOption();
            for (int i = 0; i < count; i++) {
                value.modify("Hello, world at " + i);
                out.write(value);
            }
        } finally {
            out.close();
        }

        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        try {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                String answer = "Hello, world at " + i;
                assertThat(answer, reader.next(k, v), is(true));
                assertThat(answer, k.get(), is(1L));
                assertThat(answer, v.toString(), is(answer));
            }
            assertThat("eof", reader.next(k), is(false));
        } finally {
            reader.close();
        }
    }

    private static class MockFormat extends SequenceFileFormat<LongWritable, Text, StringOption> {

        MockFormat() {
            return;
        }

        @Override
        public Class<StringOption> getSupportedType() {
            return StringOption.class;
        }

        @Override
        protected LongWritable createKeyObject() {
            return new LongWritable();
        }

        @Override
        protected Text createValueObject() {
            return new Text();
        }

        @Override
        public long getPreferredFragmentSize() throws IOException, InterruptedException {
            return SequenceFile.SYNC_INTERVAL * 10;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void copyToModel(LongWritable key, Text value, StringOption model) throws IOException {
            model.modify(value);
        }

        @Override
        protected void copyFromModel(StringOption model, LongWritable key, Text value) throws IOException {
            key.set(1);
            value.set(model.get());
        }
    }
}
