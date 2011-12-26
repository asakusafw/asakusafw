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
package com.asakusafw.windgate.hadoopfs.ssh;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link WindGateHadoopGet}.
 */
public class WindGateHadoopGetTest {

    private static final Path PREFIX = new Path("target/testing/windgate");

    private Configuration conf;

    private FileSystem fs;

    private PrintStream stdout;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        conf = new Configuration();
        fs = FileSystem.get(conf);
        clear();
        stdout = System.out;
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (stdout != null) {
            System.setOut(stdout);
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
     * Gets a single file.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Path testing = new Path(PREFIX, "testing");
        put(testing, "Hello, world!");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        int result = new WindGateHadoopGet(conf).execute(testing.toString());
        assertThat(result, is(0));

        Map<String, String> contents = get(buffer.toByteArray());
        assertThat(contents.size(), is(1));
        assertThat(contents.get("testing"), is("Hello, world!"));
    }

    /**
     * Gets multiple files.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        Path path1 = new Path(PREFIX, "testing-1");
        Path path2 = new Path(PREFIX, "testing-2");
        Path path3 = new Path(PREFIX, "testing-3");
        put(path1, "Hello1, world!");
        put(path2, "Hello2, world!");
        put(path3, "Hello3, world!");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        int result = new WindGateHadoopGet(conf).execute(path1.toString(), path2.toString(), path3.toString());
        assertThat(result, is(0));

        Map<String, String> contents = get(buffer.toByteArray());
        assertThat(contents.size(), is(3));
        assertThat(contents.get("testing-1"), is("Hello1, world!"));
        assertThat(contents.get("testing-2"), is("Hello2, world!"));
        assertThat(contents.get("testing-3"), is("Hello3, world!"));
    }

    /**
     * Gets multiple files using glob.
     * @throws Exception if failed
     */
    @Test
    public void glob() throws Exception {
        Path path1 = new Path(PREFIX, "testing-1");
        Path path2 = new Path(PREFIX, "testing-2");
        Path path3 = new Path(PREFIX, "testing-3");
        put(path1, "Hello1, world!");
        put(path2, "Hello2, world!");
        put(path3, "Hello3, world!");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        int result = new WindGateHadoopGet(conf).execute(new Path(PREFIX, "testing-*").toString());
        assertThat(result, is(0));

        Map<String, String> contents = get(buffer.toByteArray());
        assertThat(contents.size(), is(3));
        assertThat(contents.get("testing-1"), is("Hello1, world!"));
        assertThat(contents.get("testing-2"), is("Hello2, world!"));
        assertThat(contents.get("testing-3"), is("Hello3, world!"));
    }

    /**
     * Attemts to get missing files.
     * @throws Exception if failed
     */
    @Test
    public void missing() throws Exception {
        Path testing = new Path(PREFIX, "testing");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        int result = new WindGateHadoopGet(conf).execute(testing.toString());
        assertThat(result, is(not(0)));
    }

    /**
     * Empty arguments.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        int result = new WindGateHadoopGet(conf).execute();
        assertThat(result, is(not(0)));
    }

    private void put(Path path, String string) throws IOException {
        FSDataOutputStream out = fs.create(path, true);
        try {
            out.write(string.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private Map<String, String> get(byte[] contents) throws IOException {
        FileList.Reader reader = FileList.createReader(new ByteArrayInputStream(contents));
        Map<String, String> results = new HashMap<String, String>();
        while (reader.next()) {
            InputStream f = reader.openContent();
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
                results.put(reader.getCurrentFile().getPath().getName(), result);
            } finally {
                f.close();
            }
        }
        return results;
    }
}
