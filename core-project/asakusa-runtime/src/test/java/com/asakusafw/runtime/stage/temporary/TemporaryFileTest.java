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
package com.asakusafw.runtime.stage.temporary;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link TemporaryFile}.
 */
public class TemporaryFileTest {

    /**
     * A temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        doIo(1);
    }

    /**
     * empty file.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        doIo(0);
    }

    /**
     * multiple records.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        doIo(3);
    }

    /**
     * Large file.
     * @throws Exception if failed
     */
    @Test
    public void large() throws Exception {
        doIo(110000000);
    }

    /**
     * Simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple_w_TemporaryStorage() throws Exception {
        doIo_w_TemporaryStorage(1);
    }

    /**
     * empty file.
     * @throws Exception if failed
     */
    @Test
    public void empty_w_TemporaryStorage() throws Exception {
        doIo_w_TemporaryStorage(0);
    }

    /**
     * multiple records.
     * @throws Exception if failed
     */
    @Test
    public void multiple_w_TemporaryStorage() throws Exception {
        doIo(3);
    }

    /**
     * Large file.
     * @throws Exception if failed
     */
    @Test
    public void large_w_TemporaryStorage() throws Exception {
        doIo_w_TemporaryStorage(110000000);
    }

    /**
     * Writes {@link NullWritable}s.
     * @throws Exception if failed
     */
    @Test
    public void null_entry() throws Exception {
        // eagerly initializes snappy
        Snappy.getNativeLibraryVersion();

        File file = folder.newFile();
        try (ModelOutput<NullWritable> out = new TemporaryFileOutput<>(
                new BufferedOutputStream(new FileOutputStream(file)),
                NullWritable.class.getName(),
                1024,
                256 * 1024)) {
            out.write(NullWritable.get());
            out.write(NullWritable.get());
            out.write(NullWritable.get());
        }

        try (TemporaryFileInput<NullWritable> in = new TemporaryFileInput<>(
                new BufferedInputStream(new FileInputStream(file)),
                0)) {
            assertThat(in.readTo(NullWritable.get()), is(true));
            assertThat(in.readTo(NullWritable.get()), is(true));
            assertThat(in.readTo(NullWritable.get()), is(true));
            assertThat(in.readTo(NullWritable.get()), is(false));
        }
    }

    private void doIo(int count) throws IOException {
        // eagerly initializes snappy
        Snappy.getNativeLibraryVersion();

        File file = folder.newFile();
        Text value = new Text("Hello, world!");

        long t0 = System.currentTimeMillis();
        try (ModelOutput<Text> out = new TemporaryFileOutput<>(
                new BufferedOutputStream(new FileOutputStream(file)),
                Text.class.getName(),
                530 * 1024,
                512 * 1024)) {
            for (int i = 0; i < count; i++) {
                out.write(value);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "WRITE: time={0}, rows={1}, size={2}",
                t1 - t0,
                count,
                file.length()));

        try (TemporaryFileInput<Text> in = new TemporaryFileInput<>(
                new BufferedInputStream(new FileInputStream(file)),
                0)) {
            Text result = new Text();
            assertThat(in.getDataTypeName(), is(Text.class.getName()));
            for (int i = 0; i < count; i++) {
                assertTrue(in.readTo(result));
                if (i == 0) {
                    assertThat(result, is(value));
                }
            }
            assertThat(in.readTo(result), is(false));
        }

        long t2 = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "READ: time={0}, rows={1}, size={2}",
                t2 - t1,
                count,
                file.length()));
    }

    private void doIo_w_TemporaryStorage(int count) throws IOException {
        File file = folder.newFile();
        Text value = new Text("Hello, world!");

        long t0 = System.currentTimeMillis();
        try (ModelOutput<Text> out = TemporaryStorage.openOutput(
                new Configuration(),
                Text.class,
                new Path(file.toURI()))) {
            for (int i = 0; i < count; i++) {
                out.write(value);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "WRITE: time={0}, rows={1}, size={2}",
                t1 - t0,
                count,
                file.length()));

        try (ModelInput<Text> in = TemporaryStorage.openInput(
                new Configuration(),
                Text.class,
                new Path(file.toURI()))) {
            Text result = new Text();
            for (int i = 0; i < count; i++) {
                assertTrue(in.readTo(result));
                if (i == 0) {
                    assertThat(result, is(value));
                }
            }
            assertThat(in.readTo(result), is(false));
        }

        long t2 = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "READ: time={0}, rows={1}, size={2}",
                t2 - t1,
                count,
                file.length()));
    }
}
