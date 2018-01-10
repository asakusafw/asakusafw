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
package com.asakusafw.windgate.stream.file;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;
import com.asakusafw.windgate.stream.StringBuilderSupport;

/**
 * Test for {@link FileResourceMirror}.
 */
public class FileResourceMirrorTest {

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

    /**
     * Temprary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * preparation.
     * @throws Exception if failed
     */
    @Test
    public void prepare() throws Exception {
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> a = process("a", driver("source"), dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driver("drain"));
            GateScript gate = script(a, b);
            resource.prepare(gate);
        }
    }

    /**
     * preparation.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void prepare_invalid_variable() throws Exception {
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> a = process("a", driver("${invalid_var}"), dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driver("drain"));
            GateScript gate = script(a, b);
            resource.prepare(gate);
            fail();
        }
    }

    /**
     * prepare failed on source is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void prepare_invalid_source() throws Exception {
        Map<String, String> conf = new HashMap<>();
        DriverScript driverScript = new DriverScript("file", conf);
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> a = process("a", driverScript, dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driver("drain"));
            GateScript gate = script(a, b);
            resource.prepare(gate);
            fail();
        }
    }

    /**
     * prepare failed on source is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void prepare_invalid_drain() throws Exception {
        Map<String, String> conf = new HashMap<>();
        DriverScript driverScript = new DriverScript("file", conf);
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> a = process("a", driver("source"), dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driverScript);
            GateScript gate = script(a, b);
            resource.prepare(gate);
            fail();
        }
    }

    /**
     * source.
     * @throws Exception if failed
     */
    @Test
    public void source() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello, world!");
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", driver(file.getName()), dummy());
            resource.prepare(script(process));
            try (SourceDriver<StringBuilder> driver = resource.createSource(process)) {
                driver.prepare();
                test(driver, "Hello, world!");
            }
        }
    }

    /**
     * source with parameter.
     * @throws Exception if failed
     */
    @Test
    public void source_parameterized() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello, world!");
        try (FileResourceMirror resource = new FileResourceMirror(
                profile(),
                new ParameterList(Collections.singletonMap("var", file.getName())))) {
            ProcessScript<StringBuilder> process = process("a", driver("${var}"), dummy());
            resource.prepare(script(process));
            try (SourceDriver<StringBuilder> driver = resource.createSource(process)) {
                driver.prepare();
                test(driver, "Hello, world!");
            }
        }
    }

    /**
     * source with multiple lines.
     * @throws Exception if failed
     */
    @Test
    public void source_multi() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");

        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", driver(file.getName()), dummy());
            resource.prepare(script(process));
            try (SourceDriver<StringBuilder> driver = resource.createSource(process)) {
                driver.prepare();
                test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
            }
        }
    }

    /**
     * source but file missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void source_invalid() throws Exception {
        File file = folder.newFile("file");
        Assume.assumeTrue(file.delete());

        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", driver(file.getName()), dummy());
            resource.prepare(script(process));
            try (SourceDriver<StringBuilder> driver = resource.createSource(process)) {
                driver.prepare();
                test(driver, "Hello, world!");
            }
        }
    }

    /**
     * source in simulated mode.
     * @throws Exception if failed
     */
    @Test
    public void source_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        File file = folder.newFile("file");
        file.delete();

        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            ProcessScript<StringBuilder> process = process("a", driver(file.getName()), dummy());
            resource.prepare(script(process));
            try (SourceDriver<StringBuilder> driver = resource.createSource(process)) {
                assertThat(RuntimeContext.get().canExecute(driver), is(false));
            }
        }
    }

    /**
     * drain.
     * @throws Exception if failed
     */
    @Test
    public void drain() throws Exception {
        File file = folder.newFile("file");
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file.getName()));
            resource.prepare(script(process));
            try (DrainDriver<StringBuilder> driver = resource.createDrain(process)) {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            }
        }
        test(file, "Hello, world!");
    }

    /**
     * drain with parameter.
     * @throws Exception if failed
     */
    @Test
    public void drain_parameterized() throws Exception {
        File file = folder.newFile("file");
        try (FileResourceMirror resource = new FileResourceMirror(
                profile(),
                new ParameterList(Collections.singletonMap("var", file.getName())))) {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver("${var}"));
            resource.prepare(script(process));
            try (DrainDriver<StringBuilder> driver = resource.createDrain(process)) {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            }
        }
        test(file, "Hello, world!");
    }

    /**
     * drain with missing parent folder.
     * @throws Exception if failed
     */
    @Test
    public void drain_create_parent() throws Exception {
        File parent = folder.newFolder("parent");
        Assume.assumeTrue(parent.delete());
        File file = new File(parent, "file");

        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver("parent/file"));
            resource.prepare(script(process));
            try (DrainDriver<StringBuilder> driver = resource.createDrain(process)) {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            }
        }
        test(file, "Hello, world!");
    }

    /**
     * drain with multiple lines.
     * @throws Exception if failed
     */
    @Test
    public void drain_multi() throws Exception {
        File file = folder.newFile("file");
        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file.getName()));
            resource.prepare(script(process));
            try (DrainDriver<StringBuilder> driver = resource.createDrain(process)) {
                driver.prepare();
                driver.put(new StringBuilder("Hello1, world!"));
                driver.put(new StringBuilder("Hello2, world!"));
                driver.put(new StringBuilder("Hello3, world!"));
            }
        }
        test(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");
    }

    /**
     * drain but cannot create.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void drain_invalid() throws Exception {
        File file = folder.newFolder("file");

        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file.getName()));
            resource.prepare(script(process));
            try (DrainDriver<StringBuilder> driver = resource.createDrain(process)) {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            }
        }
    }

    /**
     * drain in simulated.
     * @throws Exception if failed
     */
    @Test
    public void drain_sim() throws Exception {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        File file = folder.newFile("file");
        file.delete();

        try (FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList())) {
            assertThat(RuntimeContext.get().canExecute(resource), is(true));
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file.getName()));
            resource.prepare(script(process));
            try (DrainDriver<StringBuilder> driver = resource.createDrain(process)) {
                assertThat(RuntimeContext.get().canExecute(driver), is(false));
            }
        }
        assertThat(file.exists(), is(false));
    }

    private void put(File file, String... lines) throws IOException {
        try (PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8")) {
            for (String line : lines) {
                writer.println(line);
            }
        }
    }

    private void test(SourceDriver<StringBuilder> source, String... expected) throws IOException {
        List<String> actual = new ArrayList<>();
        while (source.next()) {
            StringBuilder pair = source.get();
            actual.add(pair.toString());
        }
        Collections.sort(actual);
        Arrays.sort(expected);
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private void test(File file, String... expected) throws IOException {
        List<String> actual = new ArrayList<>();
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            while (scanner.hasNextLine()) {
                actual.add(scanner.nextLine());
            }
        }
        Collections.sort(actual);
        Arrays.sort(expected);
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private GateScript script(ProcessScript<?>... processes) {
        return new GateScript("testing", Arrays.asList(processes));
    }

    private ProcessScript<StringBuilder> process(String name, DriverScript source, DriverScript drain) {
        return new ProcessScript<>(
                name,
                "dummy",
                StringBuilder.class,
                source,
                drain
        );
    }

    private DriverScript driver(String file) {
        Map<String, String> conf = new HashMap<>();
        conf.put(FileProcess.FILE.key(), file);
        conf.put(StreamProcess.STREAM_SUPPORT.key(), StringBuilderSupport.class.getName());
        return new DriverScript("file", conf);
    }

    private DriverScript dummy() {
        return new DriverScript("dummy", Collections.emptyMap());
    }

    private FileProfile profile() {
        return new FileProfile(
                "file",
                getClass().getClassLoader(),
                folder.getRoot());
    }
}
