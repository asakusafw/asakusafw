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
package com.asakusafw.runtime.io.sequencefile;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link SequenceFileUtil}.
 */
public class SequenceFileUtilTest {

    /**
     * A temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private LocalFileSystem fs;

    private Path workingDirectory;

    private Configuration conf;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        conf = new Configuration();
        fs = FileSystem.getLocal(conf);
        workingDirectory = fs.getWorkingDirectory();
        fs.setWorkingDirectory(new Path(folder.getRoot().getAbsoluteFile().toURI()));
    }

    /**
     * Cleanups the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (fs != null && workingDirectory != null) {
            fs.setWorkingDirectory(workingDirectory);
        }
    }

    /**
     * Reads a sequence file.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        Path path = new Path("testing");

        Text key = new Text();
        Text value = new Text();
        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass())) {
            key.set("Hello");
            value.set("World");
            writer.append(key, value);
        }
        key.clear();
        value.clear();

        FileStatus status = fs.getFileStatus(path);
        try (InputStream in = new FileInputStream(fs.pathToFile(path));
                SequenceFile.Reader reader = SequenceFileUtil.openReader(in, status, conf)) {
            assertThat(reader.next(key, value), is(true));
            assertThat(key.toString(), is("Hello"));
            assertThat(value.toString(), is("World"));
            assertThat(reader.next(key, value), is(false));
        }
    }

    /**
     * Reads a sequence file.
     * @throws Exception if failed
     */
    @Test
    public void read_new() throws Exception {
        Path path = new Path("testing");

        Text key = new Text();
        Text value = new Text();
        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass())) {
            key.set("Hello");
            value.set("World");
            writer.append(key, value);
        }
        key.clear();
        value.clear();

        FileStatus status = fs.getFileStatus(path);
        try (InputStream in = new FileInputStream(fs.pathToFile(path));
                SequenceFile.Reader reader = SequenceFileUtil.openReader(in, status.getLen(), conf)) {
            assertThat(reader.next(key, value), is(true));
            assertThat(key.toString(), is("Hello"));
            assertThat(value.toString(), is("World"));
            assertThat(reader.next(key, value), is(false));
        }
    }

    /**
     * Uses a large sequence file with original API.
     * @throws Exception if failed
     */
    @Test
    public void original_large() throws Exception {
        Path path = new Path("large");

        LongWritable key = new LongWritable();
        LongWritable value = new LongWritable();

        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass())) {
            for (long i = 0; i < 300000; i++) {
                key.set(i);
                value.set(i + 1);
                writer.append(key, value);
            }
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            for (long i = 0; i < 300000; i++) {
                assertThat(reader.next(key, value), is(true));
                assertThat(key.get(), is(i));
                assertThat(value.get(), is(i + 1));
            }
            assertThat(reader.next(key, value), is(false));
        }
    }

    /**
     * Reads a large sequence file.
     * @throws Exception if failed
     */
    @Test
    public void read_large() throws Exception {
        Path path = new Path("large");

        LongWritable key = new LongWritable();
        LongWritable value = new LongWritable();

        try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), value.getClass())) {
            for (long i = 0; i < 300000; i++) {
                key.set(i);
                value.set(i + 1);
                writer.append(key, value);
            }
        }

        FileStatus status = fs.getFileStatus(path);
        try (InputStream in = new FileInputStream(fs.pathToFile(path));
                SequenceFile.Reader reader = SequenceFileUtil.openReader(in, status, conf)) {
            for (long i = 0; i < 300000; i++) {
                assertThat(reader.next(key, value), is(true));
                assertThat(key.get(), is(i));
                assertThat(value.get(), is(i + 1));
            }
            assertThat(reader.next(key, value), is(false));
        }
    }

    /**
     * Creates a sequence file.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        Path path = new Path("testing");

        Text key = new Text();
        Text value = new Text();
        try (OutputStream out = new FileOutputStream(fs.pathToFile(path));
                SequenceFile.Writer writer = SequenceFileUtil.openWriter(
                        new BufferedOutputStream(out), conf, key.getClass(), value.getClass(), null)) {
            key.set("Hello");
            value.set("World");
            writer.append(key, value);
        }
        key.clear();
        value.clear();

        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            assertThat(reader.next(key, value), is(true));
            assertThat(key.toString(), is("Hello"));
            assertThat(value.toString(), is("World"));
            assertThat(reader.next(key, value), is(false));
        }
    }

    /**
     * Creates a large sequence file.
     * @throws Exception if failed
     */
    @Test
    public void write_large() throws Exception {
        Path path = new Path("testing");

        LongWritable key = new LongWritable();
        LongWritable value = new LongWritable();
        try (OutputStream out = new FileOutputStream(fs.pathToFile(path));
                SequenceFile.Writer writer = SequenceFileUtil.openWriter(
                        new BufferedOutputStream(out), conf, key.getClass(), value.getClass(), null)) {
            for (long i = 0; i < 300000; i++) {
                key.set(i);
                value.set(i + 1);
                writer.append(key, value);
            }
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            for (long i = 0; i < 300000; i++) {
                assertThat(reader.next(key, value), is(true));
                assertThat(key.get(), is(i));
                assertThat(value.get(), is(i + 1));
            }
            assertThat(reader.next(key, value), is(false));
        }
    }

    /**
     * Creates a compressed sequence file.
     * @throws Exception if failed
     */
    @Test
    public void write_compressed() throws Exception {
        DefaultCodec codec = new DefaultCodec();
        codec.setConf(conf);

        Path path = new Path("testing");

        LongWritable key = new LongWritable();
        LongWritable value = new LongWritable();
        try (OutputStream out = new FileOutputStream(fs.pathToFile(path));
                SequenceFile.Writer writer = SequenceFileUtil.openWriter(
                        new BufferedOutputStream(out), conf, key.getClass(), value.getClass(), codec);) {
            for (long i = 0; i < 300000; i++) {
                key.set(i);
                value.set(i + 1);
                writer.append(key, value);
            }
        }

        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
            for (long i = 0; i < 300000; i++) {
                assertThat(reader.next(key, value), is(true));
                assertThat(key.get(), is(i));
                assertThat(value.get(), is(i + 1));
            }
            assertThat(reader.next(key, value), is(false));
        }
    }
}
