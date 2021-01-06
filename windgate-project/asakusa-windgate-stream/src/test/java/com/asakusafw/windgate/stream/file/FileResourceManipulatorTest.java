/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;
import com.asakusafw.windgate.stream.StringBuilderSupport;

/**
 * Test for {@link FileResourceManipulator}.
 */
public class FileResourceManipulatorTest {

    /**
     * Temprary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test method for {@link FileResourceManipulator#cleanupSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void cleanupSource() throws Exception {
        File file = folder.newFile("file");
        ProcessScript<StringBuilder> process = process("testing", driver(file.getName()), dummy());
        FileResourceManipulator manipulator = new FileResourceManipulator(profile(), new ParameterList());

        assertThat(file.exists(), is(true));
        manipulator.cleanupSource(process);
        assertThat(file.exists(), is(false));
        manipulator.cleanupSource(process);
        // ok.
    }

    /**
     * Test method for {@link FileResourceManipulator#cleanupDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void cleanupDrain() throws Exception {
        File file = folder.newFile("file");
        ProcessScript<StringBuilder> process = process("testing", dummy(), driver(file.getName()));
        FileResourceManipulator manipulator = new FileResourceManipulator(profile(), new ParameterList());

        assertThat(file.exists(), is(true));
        manipulator.cleanupDrain(process);
        assertThat(file.exists(), is(false));
        manipulator.cleanupDrain(process);
        // ok.
    }

    /**
     * Test method for {@link FileResourceManipulator#createSourceForSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createSourceForSource() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");

        ProcessScript<StringBuilder> process = process("testing", driver(file.getName()), dummy());
        FileResourceManipulator manipulator = new FileResourceManipulator(profile(), new ParameterList());

        try (SourceDriver<StringBuilder> driver = manipulator.createSourceForSource(process)) {
            driver.prepare();
            test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
        }
    }

    /**
     * Test method for {@link FileResourceManipulator#createDrainForSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createDrainForSource() throws Exception {
        File file = folder.newFile("file");
        ProcessScript<StringBuilder> process = process("testing", driver(file.getName()), dummy());
        FileResourceManipulator manipulator = new FileResourceManipulator(profile(), new ParameterList());

        try (DrainDriver<StringBuilder> driver = manipulator.createDrainForSource(process)) {
            driver.prepare();
            driver.put(new StringBuilder("Hello1, world!"));
            driver.put(new StringBuilder("Hello2, world!"));
            driver.put(new StringBuilder("Hello3, world!"));
        }

        test(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");
    }

    /**
     * Test method for {@link FileResourceManipulator#createSourceForDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createSourceForDrain() throws Exception {
        File file = folder.newFile("file");
        put(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");

        ProcessScript<StringBuilder> process = process("testing", dummy(), driver(file.getName()));
        FileResourceManipulator manipulator = new FileResourceManipulator(profile(), new ParameterList());

        try (SourceDriver<StringBuilder> driver = manipulator.createSourceForDrain(process)) {
            driver.prepare();
            test(driver, "Hello1, world!", "Hello2, world!", "Hello3, world!");
        }
    }

    /**
     * Test method for {@link FileResourceManipulator#createDrainForDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createDrainForDrain() throws Exception {
        File file = folder.newFile("file");
        ProcessScript<StringBuilder> process = process("testing", dummy(), driver(file.getName()));
        FileResourceManipulator manipulator = new FileResourceManipulator(profile(), new ParameterList());

        try (DrainDriver<StringBuilder> driver = manipulator.createDrainForDrain(process)) {
            driver.prepare();
            driver.put(new StringBuilder("Hello1, world!"));
            driver.put(new StringBuilder("Hello2, world!"));
            driver.put(new StringBuilder("Hello3, world!"));
        }

        test(file, "Hello1, world!", "Hello2, world!", "Hello3, world!");
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
