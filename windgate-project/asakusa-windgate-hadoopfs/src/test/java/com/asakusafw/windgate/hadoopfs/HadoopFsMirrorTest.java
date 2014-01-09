/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * Test for {@link HadoopFsMirror}.
 */
public class HadoopFsMirrorTest {

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private Configuration conf;

    private Path working;

    private FileSystem fs;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        conf = new ConfigurationProvider().newInstance();
        working = new Path(temp.getRoot().toURI());
        fs = FileSystem.get(working.toUri(), conf);
        Assume.assumeThat(working.toString(), is(not(containsString(" "))));
    }

    /**
     * simple source.
     * @throws Exception if failed
     */
    @Test
    public void source() throws Exception {
        set("testing", "Hello, world!");

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target", "testing");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            try {
                driver.prepare();
                test(driver, "Hello, world!");
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * source with parameterized path.
     * @throws Exception if failed
     */
    @Test
    public void source_parameterized() throws Exception {
        set("testing", "Hello, world!");

        HadoopFsMirror resource = new HadoopFsMirror(
                conf,
                profile(),
                new ParameterList(Collections.singletonMap("var", "testing")));
        try {
            ProcessScript<Text> process = source("target", "${var}");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            try {
                driver.prepare();
                test(driver, "Hello, world!");
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * source with multiple values.
     * @throws Exception if failed
     */
    @Test
    public void source_multivalue() throws Exception {
        set("testing", "Hello1, world!", "Hello2, world!", "Hello3, world!");

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target", "testing");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            try {
                driver.prepare();
                test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * multiple source.
     * @throws Exception if failed
     */
    @Test
    public void source_multisource() throws Exception {
        set("testing1", "Hello1, world!");
        set("testing2", "Hello2, world!");
        set("testing3", "Hello3, world!");

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target", "testing1", "testing2", "testing3");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            try {
                driver.prepare();
                test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * multiple source.
     * @throws Exception if failed
     */
    @Test
    public void source_glob() throws Exception {
        set("testing-1", "Hello1, world!");
        set("testing-2", "Hello2, world!");
        set("testing-3", "Hello3, world!");

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target", "testing-*");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            try {
                driver.prepare();
                test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * source in simulation mode.
     * @throws Exception if failed
     */
    @Test
    public void source_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));

            ProcessScript<Text> process = source("target", "testing");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            try {
                assertThat(RuntimeContext.get().canExecute(driver), is(false));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    /**
     * Attempts to read missing source.
     * @throws Exception if failed
     */
    @Test
    public void source_missing() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target", "MISSING");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            driver.prepare();
            driver.get();
            driver.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * no sources.
     * @throws Exception if failed
     */
    @Test
    public void source_nosource() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            driver.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * source with invalid parameter.
     * @throws Exception if failed
     */
    @Test
    public void source_invalid_parameter() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = source("target", "${INVALID}");
            resource.prepare(script(process));

            SourceDriver<Text> driver = resource.createSource(process);
            driver.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * simple drain.
     * @throws Exception if failed
     */
    @Test
    public void drain() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = drain("target", "testing");
            resource.prepare(script(process));
            DrainDriver<Text> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new Text("Hello, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }

        test("testing", "Hello, world!");
    }

    /**
     * drain with parameterized path.
     * @throws Exception if failed
     */
    @Test
    public void drain_parameterized() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(
                conf,
                profile(),
                new ParameterList(Collections.singletonMap("var", "testing")));
        try {
            ProcessScript<Text> process = drain("target", "${var}");
            resource.prepare(script(process));

            DrainDriver<Text> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new Text("Hello, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }

        test("testing", "Hello, world!");
    }

    /**
     * drain with multiplevalue.
     * @throws Exception if failed
     */
    @Test
    public void drain_multivalue() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = drain("target", "testing");
            resource.prepare(script(process));

            DrainDriver<Text> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new Text("Hello1, world!"));
                driver.put(new Text("Hello2, world!"));
                driver.put(new Text("Hello3, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }

        test("testing", "Hello1, world!", "Hello2, world!", "Hello3, world!");
    }

    /**
     * drain with multiplevalue.
     * @throws Exception if failed
     */
    @Test
    public void drain_multisource() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = drain("target", "testing1", "testing2", "testing3");
            resource.prepare(script(process));

            DrainDriver<Text> driver = resource.createDrain(process);
            driver.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Attempts to create drain for conflict path.
     * @throws Exception if failed
     */
    @Test
    public void drain_conflict() throws Exception {
        fs.mkdirs(new Path(working, "CONFLICT"));

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = drain("target", "CONFLICT");
            resource.prepare(script(process));

            DrainDriver<Text> driver = resource.createDrain(process);
            driver.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * Attempts to create drain for conflict path.
     * @throws Exception if failed
     */
    @Test
    public void drain_invalid_parameter() throws Exception {
        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            ProcessScript<Text> process = drain("target", "${INVALID}");
            resource.prepare(script(process));

            DrainDriver<Text> driver = resource.createDrain(process);
            driver.close();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            resource.close();
        }
    }

    /**
     * drain in simulation mode.
     * @throws Exception if failed
     */
    @Test
    public void drain_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        HadoopFsMirror resource = new HadoopFsMirror(conf, profile(), new ParameterList());
        try {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            ProcessScript<Text> process = drain("target", "testing");
            resource.prepare(script(process));
            DrainDriver<Text> driver = resource.createDrain(process);
            try {
                assertThat(RuntimeContext.get().canExecute(driver), is(false));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
        try {
            FileStatus status = fs.getFileStatus(getPath("testing"));
            assertThat(status, is(nullValue()));
        } catch (IOException e) {
            // ok.
        }
    }

    private HadoopFsProfile profile() {
        return new HadoopFsProfile("target", working, null);
    }

    private GateScript script(ProcessScript<?>... processes) {
        return new GateScript("testing", Arrays.asList(processes));
    }

    private ProcessScript<Text> source(String resource, String... files) {
        StringBuilder buf = new StringBuilder();
        for (String file : files) {
            buf.append(file);
            buf.append(" ");
        }
        return new ProcessScript<Text>(
                "testing", "default", Text.class,
                d(resource, buf.toString().trim()),
                new DriverScript("DUMMY", Collections.<String, String>emptyMap()));
    }

    private ProcessScript<Text> drain(String resource, String... files) {
        StringBuilder buf = new StringBuilder();
        for (String file : files) {
            buf.append(file);
            buf.append(" ");
        }
        return new ProcessScript<Text>(
                "testing", "default", Text.class,
                new DriverScript("DUMMY", Collections.<String, String>emptyMap()),
                d(resource, buf.toString().trim()));
    }

    private DriverScript d(String name, String file) {
        return new DriverScript(
                name,
                file == null ?
                        Collections.<String, String>emptyMap() :
                            Collections.singletonMap(FileProcess.FILE.key(), file));
    }

    private void test(SourceDriver<Text> source, String... expects) throws IOException {
        List<String> results = new ArrayList<String>();
        while (source.next()) {
            results.add(source.get().toString());
        }
        Arrays.sort(expects);
        Collections.sort(results);
        assertThat(results, is(Arrays.asList(expects)));
    }

    private void test(String path, String... expects) throws IOException {
        List<String> results = new ArrayList<String>();
        Path resolved = getPath(path);
        ModelInput<Text> input = TemporaryStorage.openInput(conf, Text.class, resolved);
        try {
            Text text = new Text();
            while (input.readTo(text)) {
                results.add(text.toString());
            }
        } finally {
            input.close();
        }
        assertThat(results, is(Arrays.asList(expects)));
    }

    private void set(String path, String... values) throws IOException {
        Path resolved = getPath(path);
        ModelOutput<Text> output = TemporaryStorage.openOutput(conf, Text.class, resolved);
        try {
            for (String string : values) {
                output.write(new Text(string));
            }
        } finally {
            output.close();
        }
    }

    private Path getPath(String path) {
        Path resolved = new Path(working, path);
        return resolved;
    }
}
