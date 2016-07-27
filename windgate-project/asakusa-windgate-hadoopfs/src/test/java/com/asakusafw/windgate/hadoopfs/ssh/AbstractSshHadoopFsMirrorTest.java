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
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;
import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * Test for {@link AbstractSshHadoopFsMirror}.
 */
public class AbstractSshHadoopFsMirrorTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final SshProfile profile;
    {
        Map<String, String> emptyMap = Collections.emptyMap();
        profile = new SshProfile("dummy", "dummy", "user", "host", 0, "id", "pass", emptyMap) {
            @Override
            public String getGetCommand() {
                return "get";
            }
            @Override
            public String getPutCommand() {
                return "put";
            }
            @Override
            public String getDeleteCommand() {
                return "delete";
            }
        };
    }

    volatile List<String> lastCommand;

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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                driver.prepare();
                driver.put(new Text("Hello, world!"));
            }
        }

        assertThat(lastCommand, is(Arrays.asList("put")));
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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(
                new Configuration(),
                profile,
                new ParameterList(Collections.singletonMap("var", "replacement")))) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing-${var}");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                driver.prepare();
                driver.put(new Text("Hello, world!"));
            }
        }

        assertThat(lastCommand, is(Arrays.asList("put")));
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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                driver.prepare();
                driver.put(new Text("Hello1, world!"));
                driver.put(new Text("Hello2, world!"));
                driver.put(new Text("Hello3, world!"));
            }
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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", null);
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                // do nothing
            }
            fail();
        } catch (IOException e) {
            // ok.
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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                // do nothing
            }
            fail();
        } catch (IOException e) {
            // ok.
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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(
                new Configuration(),
                profile,
                new ParameterList(Collections.singletonMap("var", "replacement")))) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing-${INVALID}");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                // do nothing
            }
            fail();
        } catch (IOException e) {
            // ok.
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

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc);) {
                driver.prepare();
                driver.put(new Text("Hello, world!"));
            }
        }
    }

    /**
     * drain in simulation mode.
     * driver must be executed as normal mode.
     * @throws Exception if failed
     */
    @Test
    public void drain_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            ProcessScript<Text> proc = p("p", "INVALID", "", "dummy", "testing");
            resource.prepare(script(proc));
            try (DrainDriver<Text> driver = resource.createDrain(proc)) {
                assertThat(RuntimeContext.get().canExecute(driver), is(true));
                driver.prepare();
                driver.put(new Text("Hello, world!"));
            }
        }

        assertThat(lastCommand, is(Arrays.asList("put")));
        Map<String, List<String>> results = read(stdIn);
        assertThat(results.size(), is(1));
        assertThat(results.get("testing"), is(Arrays.asList("Hello, world!")));
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
            }
        }
        Collections.sort(results);

        assertThat(lastCommand, is(Arrays.asList("get", "testing-1")));
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-replacement", "Hello, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(
                new Configuration(),
                profile,
                new ParameterList(Collections.singletonMap("var", "replacement")))) {
            ProcessScript<Text> proc = p("p", "dummy", "testing-${var}", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
            }
        }
        Collections.sort(results);

        assertThat(lastCommand, is(Arrays.asList("get", "testing-replacement")));
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello1, world!", "Hello2, world!", "Hello3, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
            }
        }
        Collections.sort(results);

        assertThat(lastCommand, is(Arrays.asList("get", "testing-1")));
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello1, world!");
            put(writer, "testing-2", "Hello2, world!");
            put(writer, "testing-3", "Hello3, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", " testing-1 testing-2  testing-3 ", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
            }
        }
        Collections.sort(results);


        assertThat(lastCommand, is(Arrays.asList("get", "testing-1", "testing-2", "testing-3")));
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

        try (FileOutputStream output = new FileOutputStream(stdOut)) {
            FileList.Writer writer = FileList.createWriter(output);
            put(writer, "testing-1", "Hello, world!");
            // FileList.Writer is not closed
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
                fail();
            }
        } catch (IOException e) {
            // ok.
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", null, "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
                fail();
            }
        } catch (IOException e) {
            // ok.
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", "", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
                fail();
            }
        } catch (IOException e) {
            // ok.
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

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
            }
        }
    }

    /**
     * source in simulated mode.
     * the driver must be execute as production mode.
     * @throws Exception if failed
     */
    @Test
    public void source_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        stdIn = folder.newFile("stdin");
        stdOut = folder.newFile("stdout");
        exit = 0;

        try (FileOutputStream output = new FileOutputStream(stdOut);
                FileList.Writer writer = FileList.createWriter(output)) {
            put(writer, "testing-1", "Hello, world!");
        }

        List<String> results = new ArrayList<>();
        try (MockSshHadoopFsMirror resource = new MockSshHadoopFsMirror(new Configuration(), profile, new ParameterList())) {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            ProcessScript<Text> proc = p("p", "dummy", "testing-1", "INVALID", "");
            resource.prepare(script(proc));
            try (SourceDriver<Text> driver = resource.createSource(proc)) {
                driver.prepare();
                assertThat(RuntimeContext.get().canExecute(driver), is(true));
                while (driver.next()) {
                    results.add(driver.get().toString());
                }
            }
        }
        Collections.sort(results);

        assertThat(lastCommand, is(Arrays.asList("get", "testing-1")));
        assertThat(results, is(Arrays.asList("Hello, world!")));
    }

    private void put(FileList.Writer writer, String path, String... contents) throws IOException {
        Configuration conf = new Configuration();
        File temp = folder.newFile(path);
        FileSystem fs = FileSystem.getLocal(conf);
        try (ModelOutput<Text> output = TemporaryStorage.openOutput(conf, Text.class, new Path(temp.toURI()))) {
            for (String content : contents) {
                output.write(new Text(content));
            }
        }
        FileStatus status = fs.getFileStatus(new Path(temp.toURI()));
        try (FSDataInputStream src = fs.open(status.getPath());
                OutputStream dst = writer.openNext(status.getPath())) {
            byte[] buf = new byte[256];
            while (true) {
                int read = src.read(buf);
                if (read < 0) {
                    break;
                }
                dst.write(buf, 0, read);
            }
        }
    }

    private Map<String, List<String>> read(File file) throws IOException {
        List<File> files = new ArrayList<>();
        try (FileInputStream in = new FileInputStream(file);
                FileList.Reader reader = FileList.createReader(in)) {
            while (reader.next()) {
                Path path = reader.getCurrentPath();
                File entry = folder.newFile(path.getName());
                try (FileOutputStream dst = new FileOutputStream(entry);
                        InputStream src = reader.openContent()) {
                    byte[] buf = new byte[256];
                    while (true) {
                        int read = src.read(buf);
                        if (read < 0) {
                            break;
                        }
                        dst.write(buf, 0, read);
                    }
                }
                files.add(entry);
            }
        }

        Configuration conf = new Configuration();
        Map<String, List<String>> results = new HashMap<>();
        Text text = new Text();
        for (File entry : files) {
            List<String> lines = new ArrayList<>();
            results.put(entry.getName(), lines);
            try (ModelInput<Text> input = TemporaryStorage.openInput(conf, Text.class, new Path(entry.toURI()))) {
                while (input.readTo(text)) {
                    lines.add(text.toString());
                }
            }
        }
        return results;
    }


    private GateScript script(ProcessScript<?>... processes) {
        return new GateScript("testing", Arrays.asList(processes));
    }

    private ProcessScript<Text> p(String name,
            String sourceName, String sourceFile,
            String drainName, String drainFile) {
        return new ProcessScript<>(
                name, "default", Text.class,
                d(sourceName, sourceFile),
                d(drainName, drainFile));
    }

    private DriverScript d(String name, String file) {
        return new DriverScript(
                name,
                file == null ?
                        Collections.emptyMap() :
                            Collections.singletonMap(FileProcess.FILE.key(), file));
    }

    @SimulationSupport
    private class MockSshHadoopFsMirror extends AbstractSshHadoopFsMirror {

        MockSshHadoopFsMirror(Configuration configuration, SshProfile profile, ParameterList arguments) {
            super(configuration, profile, arguments);
        }

        @Override
        protected SshConnection openConnection(SshProfile sshProfile, List<String> command) throws IOException {
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
