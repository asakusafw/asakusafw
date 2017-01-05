/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.windgate.file.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * Test for {@link FileResourceMirror}.
 */
public class FileResourceMirrorTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test method for {@link FileResourceMirror#getName()}.
     * @throws Exception if failed
     */
    @Test
    public void getName() throws Exception {
        try (FileResourceMirror resource = new FileResourceMirror("testing")) {
            assertThat(resource.getName(), is("testing"));
        }
    }

    /**
     * Test method for {@link FileResourceMirror#prepare(com.asakusafw.windgate.core.GateScript)}.
     * @throws Exception if failed
     */
    @Test
    public void prepare() throws Exception {
        File source = folder.newFile("source");
        File drain = folder.newFile("drain");
        try (FileResourceMirror resource = new FileResourceMirror("testing")) {
            ProcessScript<String> script = script(source, drain);
            // may do nothing
            resource.prepare(gate(script));
        }
    }

    /**
     * Test method for {@link FileResourceMirror#createSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createSource() throws Exception {
        File source = folder.newFile("source");
        File drain = folder.newFile("drain");
        put(source, "Hello", "World!");
        try (FileResourceMirror resource = new FileResourceMirror("testing")) {
            ProcessScript<String> script = script(source, drain);
            resource.prepare(gate(script));
            try (SourceDriver<String> driver = resource.createSource(script)) {
                driver.prepare();
                assertThat(driver.next(), is(true));
                assertThat(driver.get(), is("Hello"));
                assertThat(driver.next(), is(true));
                assertThat(driver.get(), is("World!"));
                assertThat(driver.next(), is(false));
            }
        }
    }

    /**
     * Test method for {@link FileResourceMirror#createDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createDrain() throws Exception {
        File source = folder.newFile("source");
        File drain = folder.newFile("drain");
        try (FileResourceMirror resource = new FileResourceMirror("testing")) {
            ProcessScript<String> script = script(source, drain);
            ProcessScript<String> opposite = script(drain, source);
            resource.prepare(gate(script, opposite));

            try (DrainDriver<String> driver = resource.createDrain(script)) {
                driver.prepare();
                driver.put("Hello");
                driver.put("World!");
            }

            try (SourceDriver<String> verifier = resource.createSource(opposite)) {
                verifier.prepare();
                assertThat(verifier.next(), is(true));
                assertThat(verifier.get(), is("Hello"));
                assertThat(verifier.next(), is(true));
                assertThat(verifier.get(), is("World!"));
                assertThat(verifier.next(), is(false));
            }
        }
    }

    private GateScript gate(ProcessScript<?>... scripts) {
        return new GateScript("testing", Arrays.asList(scripts));
    }

    private ProcessScript<String> script(File source, File drain) {
        return new ProcessScript<>(
                "example",
                "plain",
                String.class,
                new DriverScript("fs", Collections.singletonMap(FileProcess.FILE.key(), source.getPath())),
                new DriverScript("fs", Collections.singletonMap(FileProcess.FILE.key(), drain.getPath())));
    }

    private void put(File file, String...values) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file);
                ObjectOutputStream output = new ObjectOutputStream(out)) {
            for (String string : values) {
                output.writeObject(string);
            }
        }
    }
}
