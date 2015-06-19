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
package com.asakusafw.windgate.hadoopfs.ssh;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.compatibility.FileSystemCompatibility;
import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;

/**
 * Test for {@link WindGateHadoopPut}.
 */
public class WindGateHadoopPutTest {

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

    private static final Path PREFIX = new Path("target/testing/windgate");

    private Configuration conf;

    private FileSystem fs;

    private InputStream stdin;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        conf = new Configuration();
        fs = FileSystem.get(conf);
        clear();
        stdin = System.in;
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (stdin != null) {
            System.setIn(stdin);
        }
        clear();
    }

    private void clear() throws IOException {
        if (fs == null) {
            return;
        }
        fs.delete(PREFIX, true);
    }

    /**
     * Puts a single file.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer);

        Path testing = new Path(PREFIX, "testing");
        put(writer, testing, "Hello, world!");

        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(buffer.toByteArray());
        int result = new WindGateHadoopPut(conf).execute(in);
        assertThat(result, is(0));

        Map<String, String> contents = get();
        assertThat(contents.size(), is(1));
        assertThat(contents.get("testing"), is("Hello, world!"));
    }

    /**
     * Puts multiple files.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer);

        Path testing1 = new Path(PREFIX, "testing-1");
        Path testing2 = new Path(PREFIX, "testing-2");
        Path testing3 = new Path(PREFIX, "testing-3");
        put(writer, testing1, "Hello1, world!");
        put(writer, testing2, "Hello2, world!");
        put(writer, testing3, "Hello3, world!");

        writer.close();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.toByteArray());
        int result = new WindGateHadoopPut(conf).execute(in);
        assertThat(result, is(0));

        Map<String, String> contents = get();
        assertThat(contents.size(), is(3));
        assertThat(contents.get("testing-1"), is("Hello1, world!"));
        assertThat(contents.get("testing-2"), is("Hello2, world!"));
        assertThat(contents.get("testing-3"), is("Hello3, world!"));
    }

    /**
     * Attemts to empty files.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer);

        writer.close();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.toByteArray());
        int result = new WindGateHadoopPut(conf).execute(in);
        assertThat(result, is(0));

        Map<String, String> contents = get();
        assertThat(contents.size(), is(0));
    }

    /**
     * Not empty arguments.
     * @throws Exception if failed
     */
    @Test
    public void arguments() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer);

        Path testing = new Path(PREFIX, "testing");
        put(writer, testing, "Hello, world!");

        writer.close();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.toByteArray());
        int result = new WindGateHadoopPut(conf).execute(in, testing.toString());
        assertThat(result, is(not(0)));
    }

    /**
     * Attempts to put using broken file list.
     * @throws Exception if failed
     */
    @Test
    public void broken() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer);

        Path testing = new Path(PREFIX, "testing");
        put(writer, testing, "Hello, world!");

        // writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(buffer.toByteArray());
        int result = new WindGateHadoopPut(conf).execute(in);
        assertThat(result, is(not(0)));
    }

    /**
     * Puts in simulation mode.
     * @throws Exception if failed
     */
    @Test
    public void simulated() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileList.Writer writer = FileList.createWriter(buffer);

        Path testing = new Path(PREFIX, "testing");
        put(writer, testing, "Hello, world!");

        writer.close();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.toByteArray());
        int result = new WindGateHadoopPut(conf).execute(in);
        assertThat(result, is(0));

        Map<String, String> contents = get();
        assertThat(contents.size(), is(0));
    }

    private void put(FileList.Writer writer, Path path, String string) throws IOException {
        OutputStream out = writer.openNext(path);
        try {
            out.write(string.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private Map<String, String> get() throws IOException {
        FileStatus[] files;
        try {
            files = fs.listStatus(PREFIX);
        } catch (FileNotFoundException e) {
            files = null;
        }
        if (files == null) {
            return Collections.emptyMap();
        }
        Map<String, String> results = new HashMap<String, String>();
        for (FileStatus status : files) {
            if (FileSystemCompatibility.isDirectory(status)) {
                continue;
            }
            InputStream f = fs.open(status.getPath());
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[256];
                while (true) {
                    int read = f.read(buf);
                    if (read < 0) {
                        break;
                    }
                    baos.write(buf, 0, read);
                }
                String result = new String(baos.toByteArray(), "UTF-8");
                results.put(status.getPath().getName(), result);
            } finally {
                f.close();
            }
        }
        return results;
    }
}
