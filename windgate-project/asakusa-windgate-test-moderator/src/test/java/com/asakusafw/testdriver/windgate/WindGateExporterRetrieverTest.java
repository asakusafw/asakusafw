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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.file.resource.FileResourceProvider;

/**
 * Test for {@link WindGateExporterRetriever}.
 * @since 0.2.2
 */
public class WindGateExporterRetrieverTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Profiles.
     */
    @Rule
    public TestContextProvider context = new TestContextProvider();

    /**
     * Test method for {@link WindGateExporterRetriever#truncate(WindGateExporterDescription, TestContext)}.
     * @throws Exception if failed
     */
    @Test
    public void truncate() throws Exception {
        Properties profile = context.getTemplate();
        profile.setProperty("resource.file", FileResourceProvider.class.getName());
        context.put("testing", profile);

        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateExporterDescription description = new MockExporterDescription(
                String.class,
                "testing",
                driver);
        WindGateExporterRetriever preparator = new WindGateExporterRetriever();

        assertThat(file.exists(), is(true));
        preparator.truncate(description, context.get());
        assertThat(file.exists(), is(false));
    }

    /**
     * Test method for {@link WindGateExporterRetriever#
     * createOutput(DataModelDefinition, WindGateExporterDescription, TestContext)}.
     * @throws Exception if failed
     */
    @Test
    public void createOutput() throws Exception {
        Properties profile = context.getTemplate();
        profile.setProperty("resource.file", FileResourceProvider.class.getName());
        context.put("testing", profile);

        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateExporterDescription description = new MockExporterDescription(
                String.class,
                "testing",
                driver);
        WindGateExporterRetriever retriever = new WindGateExporterRetriever();
        try (ModelOutput<String> output = retriever.createOutput(
                ValueDefinition.of(String.class), description, context.get())) {
            output.write("Hello1, world!");
            output.write("Hello2, world!");
            output.write("Hello3, world!");
        }
        try (FileInputStream input = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(input);) {
            assertThat(in.readObject(), is((Object) "Hello1, world!"));
            assertThat(in.readObject(), is((Object) "Hello2, world!"));
            assertThat(in.readObject(), is((Object) "Hello3, world!"));
            try {
                in.readObject();
                fail();
            } catch (IOException e) {
                // ok.
            }
        }
    }

    /**
     * Test method for {@link WindGateExporterRetriever#
     * createSource(DataModelDefinition, WindGateExporterDescription, TestContext)}.
     * @throws Exception if failed
     */
    @Test
    public void createSource() throws Exception {
        Properties profile = context.getTemplate();
        profile.setProperty("resource.file", FileResourceProvider.class.getName());
        context.put("testing", profile);

        File file = folder.newFile("file");
        try (FileOutputStream output = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(output)) {
            out.writeObject("Hello1, world!");
            out.writeObject("Hello2, world!");
            out.writeObject("Hello3, world!");
        }

        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateExporterDescription description = new MockExporterDescription(
                String.class,
                "testing",
                driver);
        WindGateExporterRetriever retriever = new WindGateExporterRetriever();
        ValueDefinition<String> stringDef = ValueDefinition.of(String.class);
        try (DataModelSource source = retriever.createSource(stringDef, description, context.get())) {
            DataModelReflection r1 = source.next();
            assertThat(r1, is(notNullValue()));
            assertThat(stringDef.toObject(r1), is("Hello1, world!"));

            DataModelReflection r2 = source.next();
            assertThat(r2, is(notNullValue()));
            assertThat(stringDef.toObject(r2), is("Hello2, world!"));

            DataModelReflection r3 = source.next();
            assertThat(r3, is(notNullValue()));
            assertThat(stringDef.toObject(r3), is("Hello3, world!"));

            DataModelReflection r4 = source.next();
            assertThat(r4, is(nullValue()));
        }
    }
}
