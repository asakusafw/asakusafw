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
        File source = folder.newFile("source");
        File drain = folder.newFile("drain");

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> a = process("a", driver(source), dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driver(drain));
            GateScript gate = script(a, b);
            resource.prepare(gate);
        } finally {
            resource.close();
        }
    }

    /**
     * preparation.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void prepare_invalid_variable() throws Exception {
        File drain = folder.newFile("drain");

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> a = process("a", driver("${invalid_var}"), dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driver(drain));
            GateScript gate = script(a, b);
            resource.prepare(gate);
            fail();
        } finally {
            resource.close();
        }
    }

    /**
     * prepare failed on source is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void prepare_invalid_source() throws Exception {
        File drain = folder.newFile("drain");

        Map<String, String> conf = new HashMap<String, String>();
        DriverScript driverScript = new DriverScript("file", conf);

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> a = process("a", driverScript, dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driver(drain));
            GateScript gate = script(a, b);
            resource.prepare(gate);
            fail();
        } finally {
            resource.close();
        }
    }

    /**
     * prepare failed on source is invalid.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void prepare_invalid_drain() throws Exception {
        File source = folder.newFile("source");

        Map<String, String> conf = new HashMap<String, String>();
        DriverScript driverScript = new DriverScript("file", conf);

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> a = process("a", driver(source), dummy());
            ProcessScript<StringBuilder> b = process("b", dummy(), driverScript);
            GateScript gate = script(a, b);
            resource.prepare(gate);
            fail();
        } finally {
            resource.close();
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

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", driver(file), dummy());
            resource.prepare(script(process));

            SourceDriver<StringBuilder> driver = resource.createSource(process);
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
     * source with parameter.
     * @throws Exception if failed
     */
    @Test
    public void source_parameterized() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello, world!");

        FileResourceMirror resource = new FileResourceMirror(
                profile(),
                new ParameterList(Collections.singletonMap("var", file.getAbsolutePath())));
        try {
            ProcessScript<StringBuilder> process = process("a", driver("${var}"), dummy());
            resource.prepare(script(process));

            SourceDriver<StringBuilder> driver = resource.createSource(process);
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
     * source with multiple lines.
     * @throws Exception if failed
     */
    @Test
    public void source_multi() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", driver(file), dummy());
            resource.prepare(script(process));

            SourceDriver<StringBuilder> driver = resource.createSource(process);
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
     * source but file missing.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void source_invalid() throws Exception {
        File file = folder.newFile("file");
        Assume.assumeTrue(file.delete());

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", driver(file), dummy());
            resource.prepare(script(process));

            SourceDriver<StringBuilder> driver = resource.createSource(process);
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
     * drain.
     * @throws Exception if failed
     */
    @Test
    public void drain() throws Exception {
        File file = folder.newFile("file");
        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file));
            resource.prepare(script(process));

            DrainDriver<StringBuilder> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
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
        FileResourceMirror resource = new FileResourceMirror(
                profile(),
                new ParameterList(Collections.singletonMap("var", file.getAbsolutePath())));
        try {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver("${var}"));
            resource.prepare(script(process));

            DrainDriver<StringBuilder> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
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

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file));
            resource.prepare(script(process));

            DrainDriver<StringBuilder> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
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
        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file));
            resource.prepare(script(process));

            DrainDriver<StringBuilder> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new StringBuilder("Hello1, world!"));
                driver.put(new StringBuilder("Hello2, world!"));
                driver.put(new StringBuilder("Hello3, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
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

        FileResourceMirror resource = new FileResourceMirror(profile(), new ParameterList());
        try {
            ProcessScript<StringBuilder> process = process("a", dummy(), driver(file));
            resource.prepare(script(process));

            DrainDriver<StringBuilder> driver = resource.createDrain(process);
            try {
                driver.prepare();
                driver.put(new StringBuilder("Hello, world!"));
            } finally {
                driver.close();
            }
        } finally {
            resource.close();
        }
    }

    private void put(File file, String... lines) throws IOException {
        PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
        try {
            for (String line : lines) {
                writer.println(line);
            }
        } finally {
            writer.close();
        }
    }

    private void test(SourceDriver<StringBuilder> source, String... expected) throws IOException {
        List<String> actual = new ArrayList<String>();
        while (source.next()) {
            StringBuilder pair = source.get();
            actual.add(pair.toString());
        }
        Collections.sort(actual);
        Arrays.sort(expected);
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private void test(File file, String... expected) throws IOException {
        List<String> actual = new ArrayList<String>();
        Scanner scanner = new Scanner(file, "UTF-8");
        while (scanner.hasNextLine()) {
            actual.add(scanner.nextLine());
        }
        Collections.sort(actual);
        Arrays.sort(expected);
        assertThat(actual, is(Arrays.asList(expected)));
    }

    private GateScript script(ProcessScript<?>... processes) {
        return new GateScript("testing", Arrays.<ProcessScript<?>>asList(processes));
    }

    private ProcessScript<StringBuilder> process(String name, DriverScript source, DriverScript drain) {
        return new ProcessScript<StringBuilder>(
                name,
                "dummy",
                StringBuilder.class,
                source,
                drain
        );
    }

    private DriverScript driver(File file) {
        return driver(file.getAbsolutePath());
    }

    private DriverScript driver(String file) {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(FileProcess.FILE.key(), file);
        conf.put(StreamProcess.STREAM_SUPPORT.key(), StringBuilderSupport.class.getName());
        return new DriverScript("file", conf);
    }

    private DriverScript dummy() {
        return new DriverScript("dummy", Collections.<String, String>emptyMap());
    }

    private FileProfile profile() {
        return new FileProfile(
                "file",
                getClass().getClassLoader(),
                null);
    }
}
