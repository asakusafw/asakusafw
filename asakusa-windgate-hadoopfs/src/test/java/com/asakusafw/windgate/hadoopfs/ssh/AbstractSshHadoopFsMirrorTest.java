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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.hadoopfs.ssh.FileList;
import com.asakusafw.windgate.hadoopfs.ssh.SshConnection;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;

/**
 * Test for {@link AbstractSshHadoopFsMirror}.
 */
public class AbstractSshHadoopFsMirrorTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final SshProfile profile = new SshProfile(
            "dummy", "get", "put", "delete", "user", "host", 0, "id", "pass", null);

    volatile String lastCommand;

    volatile File stdIn;

    volatile File stdOut;

    volatile int exit = -1;

    /**
     * Simple drain.
     * @throws Exception if failed
     */
    @Test
    public void drain() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            try {
                drain.put(new Text("Hello, world!"));
            } finally {
                drain.close();
            }
        } finally {
            resource.close();
        }

        assertThat(lastCommand, equalToIgnoringWhiteSpace("put"));
        Map<String, List<String>> results = read(stdIn);
        assertThat(results.size(), is(1));
        assertThat(results.get("testing"), is(Arrays.asList("Hello, world!")));
    }

    /**
     * Drain with parameterized path.
     * @throws Exception if failed
     */
    @Test
    public void drain_parameter() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(
                new Configuration(),
                profile,
                new ParameterList(Collections.singletonMap("var", "replacement")));
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing-${var}");
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            try {
                drain.put(new Text("Hello, world!"));
            } finally {
                drain.close();
            }
        } finally {
            resource.close();
        }

        assertThat(lastCommand, equalToIgnoringWhiteSpace("put"));
        Map<String, List<String>> results = read(stdIn);
        assertThat(results.size(), is(1));
        assertThat(results.get("testing-replacement"), is(Arrays.asList("Hello, world!")));
    }

    /**
     * Multiple values into drain.
     * @throws Exception if failed
     */
    @Test
    public void drain_multiple() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            try {
                drain.put(new Text("Hello1, world!"));
                drain.put(new Text("Hello2, world!"));
                drain.put(new Text("Hello3, world!"));
            } finally {
                drain.close();
            }
        } finally {
            resource.close();
        }

        Map<String, List<String>> results = read(stdIn);
        assertThat(results.size(), is(1));
        assertThat(results.get("testing"), is(Arrays.asList(
                "Hello1, world!",
                "Hello2, world!",
                "Hello3, world!")));
    }

    /**
     * Drain target unspecified.
     * @throws Exception if failed
     */
    @Test
    public void drain_nullpath() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", null);
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            drain.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Attempts to put empty files.
     * @throws Exception if failed
     */
    @Test
    public void drain_nopath() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "");
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            drain.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Attempts to put empty files.
     * @throws Exception if failed
     */
    @Test
    public void drain_invalid_parameter() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(
                new Configuration(),
                profile,
                new ParameterList(Collections.singletonMap("var", "replacement")));
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing-${INVALID}");
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            drain.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Exit code is not 0.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void drain_processfailed() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 1;

        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            DrainDriver<Text> drain = resource.createDrain(proc);
            try {
                drain.put(new Text("Hello, world!"));
            } finally {
                drain.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * Simple source.
     * @throws Exception if failed
     */
    @Test
    public void source() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello, world!");
            writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
            } finally {
                source.close();
            }
        } finally {
            resource.close();
        }
        Collections.sort(results);

        assertThat(lastCommand, equalToIgnoringWhiteSpace("get testing-1"));
        assertThat(results, is(Arrays.asList("Hello, world!")));
    }

    /**
     * Source with parameterized path.
     * @throws Exception if failed
     */
    @Test
    public void source_parameter() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-replacement", "Hello, world!");
            writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(
                new Configuration(),
                profile,
                new ParameterList(Collections.singletonMap("var", "replacement")));
        try {
            ProcessScript<Text> proc = p("p", "dummy", "testing-${var}", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
            } finally {
                source.close();
            }
        } finally {
            resource.close();
        }
        Collections.sort(results);

        assertThat(lastCommand, equalToIgnoringWhiteSpace("get testing-replacement"));
        assertThat(results, is(Arrays.asList("Hello, world!")));
    }

    /**
     * Multiple content source.
     * @throws Exception if failed
     */
    @Test
    public void source_multiple_values() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello1, world!", "Hello2, world!", "Hello3, world!");
            writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
            } finally {
                source.close();
            }
        } finally {
            resource.close();
        }
        Collections.sort(results);

        assertThat(lastCommand, equalToIgnoringWhiteSpace("get testing-1"));
        assertThat(results, is(Arrays.asList("Hello1, world!", "Hello2, world!", "Hello3, world!")));
    }

    /**
     * Multiple content source.
     * @throws Exception if failed
     */
    @Test
    public void source_multiple_files() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello1, world!");
            put(writer, "testing-2", "Hello2, world!");
            put(writer, "testing-3", "Hello3, world!");
            writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", " testing-1 testing-2  testing-3 ", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
            } finally {
                source.close();
            }
        } finally {
            resource.close();
        }
        Collections.sort(results);

        assertThat(lastCommand, equalToIgnoringWhiteSpace("get testing-1 testing-2 testing-3"));
        assertThat(results, is(Arrays.asList("Hello1, world!", "Hello2, world!", "Hello3, world!")));
    }

    /**
     * Attempts to read invalid contents.
     * @throws Exception if failed
     */
    @Test
    public void source_invalid_contents() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello, world!");
            // writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
                fail();
            } finally {
                source.close();
            }
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Source file not specified.
     * @throws Exception if failed
     */
    @Test
    public void source_nullpath() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello, world!");
            // writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", null, "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
                fail();
            } finally {
                source.close();
            }
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Source file not specified.
     * @throws Exception if failed
     */
    @Test
    public void source_emptypath() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello, world!");
            // writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", "", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
                fail();
            } finally {
                source.close();
            }
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Remote process failed.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void source_processfailed() throws Exception {
        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 1;

        FileOutputStream output = new FileOutputStream(stdOut);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello, world!");
            writer.close();
        } finally {
            output.close();
        }

        List<String> results = new ArrayList<String>();
        MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList());
        try {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            SourceDriver<Text> source = resource.createSource(proc);
            try {
                while (source.next()) {
                    results.add(source.get().toString());
                }
            } finally {
                source.close();
            }
        } finally {
            resource.close();
        }
    }

    private void put(FileList.Writer writer, String path, String... contents) throws IOException {
        Configuration conf = new Configuration();
        File temp = folder.newFile(path);
        FileSystem fs = FileSystem.getLocal(conf);
        try {
            SequenceFile.Writer sf = SequenceFile.createWriter(
                    fs,
                    conf,
                    new Path(temp.toURI()),
                    NullWritable.class,
                    Text.class);
            try {
                for (String content : contents) {
                    sf.append(NullWritable.get(), new Text(content));
                }
            } finally {
                sf.close();
            }
            FileStatus status = fs.getFileStatus(new Path(temp.toURI()));
            FSDataInputStream src = fs.open(status.getPath());
            try {
                OutputStream dst = writer.openNext(status);
                byte[] buf = new byte[256];
                while (true) {
                    int read = src.read(buf);
                    if (read < 0) {
                        break;
                    }
                    dst.write(buf, 0, read);
                }
                dst.close();
            } finally {
                src.close();
            }
        } finally {
            fs.close();
        }
    }

    private Map<String, List<String>> read(File file) throws IOException {
        List<File> files = new ArrayList<File>();
        FileInputStream in = new FileInputStream(file);
        try {
            FileList.Reader reader = FileList.createReader(in);
            while (reader.next()) {
                FileStatus status = reader.getCurrentFile();
                File entry = folder.newFile(status.getPath().getName());
                FileOutputStream dst = new FileOutputStream(entry);
                try {
                    InputStream src = reader.openContent();
                    byte[] buf = new byte[256];
                    while (true) {
                        int read = src.read(buf);
                        if (read < 0) {
                            break;
                        }
                        dst.write(buf, 0, read);
                    }
                } finally {
                    dst.close();
                }
                files.add(entry);
            }
        } finally {
            in.close();
        }

        Configuration conf = new Configuration();
        LocalFileSystem fs = FileSystem.getLocal(conf);
        try {
            Map<String, List<String>> results = new HashMap<String, List<String>>();
            Text text = new Text();
            for (File entry : files) {
                List<String> lines = new ArrayList<String>();
                results.put(entry.getName(), lines);
                SequenceFile.Reader sf = new SequenceFile.Reader(fs, new Path(entry.toURI()), conf);
                try {
                    while (sf.next(NullWritable.get(), text)) {
                        lines.add(text.toString());
                    }
                } finally {
                    sf.close();
                }
            }
            return results;
        } finally {
            fs.close();
        }
    }


    private GateScript script(ProcessScript<?>... processes) {
        return new GateScript(Arrays.asList(processes));
    }

    private ProcessScript<Text> p(String name,
            String sourceName, String sourceFile,
            String drainName, String drainFile) {
        return new ProcessScript<Text>(
                name, "default", Text.class,
                d(sourceName, sourceFile),
                d(drainName, drainFile));
    }

    private DriverScript d(String name, String file) {
        return new DriverScript(
                name,
                file == null ?
                        Collections.<String, String>emptyMap() :
                            Collections.singletonMap(FileProcess.FILE.key(), file));
    }

    private class MockSshHadoopFsMirror extends AbstractSshHadoopFsMirror {

        MockSshHadoopFsMirror(Configuration configuration, SshProfile profile, ParameterList arguments) {
            super(configuration, profile, arguments);
        }

        @Override
        protected SshConnection openConnection(SshProfile sshProfile, String command) throws IOException {
            lastCommand = command;
            return new SshConnection() {

                @Override
                public void connect() throws IOException {
                    return;
                }

                @Override
                public OutputStream openStandardInput() throws IOException {
                    return new FileOutputStream(stdIn);
                }

                @Override
                public InputStream openStandardOutput() throws IOException {
                    return new FileInputStream(stdOut);
                }

                @Override
                public void redirectStandardOutput(OutputStream output, boolean dontClose) {
                    return;
                }

                @Override
                public int waitForExit(long timeout) throws IOException, InterruptedException {
                    return exit;
                }

                @Override
                public void close() throws IOException {
                    return;
                }
            };
        }
    }
}
