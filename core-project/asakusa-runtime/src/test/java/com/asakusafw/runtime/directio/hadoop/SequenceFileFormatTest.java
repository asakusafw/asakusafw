/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
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
        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class)) {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                k.set(i);
                v.set("Hello, world at " + i);
                writer.append(k, v);
            }
        }

        try (ModelInput<StringOption> in = format.createInput(
                StringOption.class,
                fs,
                path,
                0,
                fs.getFileStatus(path).getLen(),
                new Counter())) {
            StringOption value = new StringOption();
            for (int i = 0; i < count; i++) {
                String answer = "Hello, world at " + i;
                assertThat(answer, in.readTo(value), is(true));
                assertThat(value.getAsString(), is(answer));
            }
            assertThat("eof", in.readTo(value), is(false));
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
        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class)) {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                k.set(i);
                v.set("Hello, world at " + i);
                writer.append(k, v);
            }
        }

        long fileLen = fs.getFileStatus(path).getLen();
        StringOption value = new StringOption();
        for (int attempt = 0; attempt < 5; attempt++) {
            int index = 0;
            long offset = 0;
            while (offset < fileLen) {
                long length = SequenceFile.SYNC_INTERVAL * (rand.nextInt(10) + 2);
                length = Math.min(length, fileLen - offset);
                try (ModelInput<StringOption> in = format.createInput(
                        StringOption.class,
                        fs,
                        path,
                        offset,
                        length,
                        new Counter())) {
                    while (in.readTo(value)) {
                        String answer = "Hello, world at " + index;
                        assertThat(value.getAsString(), is(answer));
                        index++;
                    }
                    assertThat("eof", in.readTo(value), is(false));
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
        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, LongWritable.class, Text.class)) {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                k.set(i);
                v.set(record);
                writer.append(k, v);
            }
        }

        long fileLen = fs.getFileStatus(path).getLen();
        StringOption value = new StringOption();
        int index = 0;
        long offset = 0;
        while (offset < fileLen) {
            long length = SequenceFile.SYNC_INTERVAL * 2;
            length = Math.min(length, fileLen - offset);
            try (ModelInput<StringOption> in = format.createInput(
                    StringOption.class,
                    fs,
                    path,
                    offset,
                    length,
                    new Counter())) {
                while (in.readTo(value)) {
                    assertThat(value.get(), is(record));
                    index++;
                }
                assertThat("eof", in.readTo(value), is(false));
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
        try (ModelInput<StringOption> in = format.createInput(
                StringOption.class,
                fs,
                path,
                0,
                fs.getFileStatus(path).getLen(),
                new Counter())) {
            assertThat("eof", in.readTo(new StringOption()), is(false));
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
        try (FSDataOutputStream output = fs.create(path)) {
            output.writeUTF("Hello, world!");
        }
        try (ModelInput<StringOption> in = format.createInput(
                StringOption.class,
                fs,
                path,
                0,
                fs.getFileStatus(path).getLen(),
                new Counter())) {
            // do nothing
        }
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
        try (ModelOutput<StringOption> out = format.createOutput(StringOption.class, fs, path, new Counter())) {
            StringOption value = new StringOption();
            for (int i = 0; i < count; i++) {
                value.modify("Hello, world at " + i);
                out.write(value);
            }
        }
        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            LongWritable k = new LongWritable();
            Text v = new Text();
            for (int i = 0; i < count; i++) {
                String answer = "Hello, world at " + i;
                assertThat(answer, reader.next(k, v), is(true));
                assertThat(answer, k.get(), is(1L));
                assertThat(answer, v.toString(), is(answer));
            }
            assertThat("eof", reader.next(k), is(false));
        }
    }

    /**
     * compressed output.
     * @throws Exception if failed
     */
    @Test
    public void output_compressed() throws Exception {
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        try (ModelOutput<StringOption> out = format.codec(new DefaultCodec())
                .createOutput(StringOption.class, fs, path, new Counter())) {
            out.write(new StringOption("Hello, world!"));
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            assertThat(reader.getCompressionCodec(), instanceOf(DefaultCodec.class));
        }
    }

    /**
     * compressed output.
     * @throws Exception if failed
     */
    @Test
    public void output_no_compressed() throws Exception {
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing.gz").toURI());
        try (ModelOutput<StringOption> out = format.codec(null)
                .createOutput(StringOption.class, fs, path, new Counter())) {
            out.write(new StringOption("Hello, world!"));
        }
        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            assertThat(reader.getCompressionCodec(), is(nullValue()));
        }
    }

    /**
     * compressed output.
     * @throws Exception if failed
     */
    @Test
    public void output_compressed_conf() throws Exception {
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        format.getConf().set(SequenceFileFormat.KEY_COMPRESSION_CODEC, DefaultCodec.class.getName());
        try (ModelOutput<StringOption> out = format.createOutput(StringOption.class, fs, path, new Counter())) {
            out.write(new StringOption("Hello, world!"));
        }
        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            assertThat(reader.getCompressionCodec(), instanceOf(DefaultCodec.class));
        }
    }

    /**
     * invalid compressed output.
     * @throws Exception if failed
     */
    @Test
    public void output_compressed_invalid() throws Exception {
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path path = new Path(folder.newFile("testing").toURI());
        format.getConf().set(SequenceFileFormat.KEY_COMPRESSION_CODEC, "__INVALID__");
        try (ModelOutput<StringOption> out = format.createOutput(StringOption.class, fs, path, new Counter())) {
            out.write(new StringOption("Hello, world!"));
        }
        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            assertThat(reader.getCompressionCodec(), is(nullValue()));
        }
    }

    private static class MockFormat extends SequenceFileFormat<LongWritable, Text, StringOption> {

        private CompressionCodec codec;

        private boolean codecSet;

        MockFormat() {
            return;
        }

        MockFormat codec(CompressionCodec c) {
            this.codecSet = true;
            this.codec = c;
            return this;
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

        @Override
        public CompressionCodec getCompressionCodec(Path path) throws IOException, InterruptedException {
            if (codecSet == false) {
                return super.getCompressionCodec(path);
            }
            return codec;
        }
    }
}
