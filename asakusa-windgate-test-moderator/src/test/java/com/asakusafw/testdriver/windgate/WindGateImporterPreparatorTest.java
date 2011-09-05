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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.file.resource.FileResourceProvider;

/**
 * Test for {@link WindGateImporterPreparator}.
 * @since 0.2.2
 */
public class WindGateImporterPreparatorTest {

    private static final TestContext EMPTY = new TestContext.Empty();

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Profiles.
     */
    @Rule
    public ProfileContext context = new ProfileContext();

    /**
     * Test method for {@link WindGateImporterPreparator#truncate(WindGateImporterDescription, TestContext)}.
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
        WindGateImporterDescription description = new MockImporterDescription(
                String.class,
                "testing",
                driver);
        WindGateImporterPreparator preparator = new WindGateImporterPreparator();

        assertThat(file.exists(), is(true));
        preparator.truncate(description, EMPTY);
        assertThat(file.exists(), is(false));
    }

    /**
     * Test method for {@link WindGateImporterPreparator#
     * createOutput(DataModelDefinition, WindGateImporterDescription, TestContext)}.
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
        WindGateImporterDescription description = new MockImporterDescription(
                String.class,
                "testing",
                driver);
        WindGateImporterPreparator preparator = new WindGateImporterPreparator();
        ModelOutput<String> output = preparator.createOutput(ValueDefinition.of(String.class), description, EMPTY);
        try {
            output.write("Hello1, world!");
            output.write("Hello2, world!");
            output.write("Hello3, world!");
        } finally {
            output.close();
        }

        FileInputStream input = new FileInputStream(file);
        try {
            ObjectInputStream in = new ObjectInputStream(input);
            assertThat(in.readObject(), is((Object) "Hello1, world!"));
            assertThat(in.readObject(), is((Object) "Hello2, world!"));
            assertThat(in.readObject(), is((Object) "Hello3, world!"));
            try {
                in.readObject();
                fail();
            } catch (IOException e) {
                // ok.
            }
            in.close();
        } finally {
            input.close();
        }
    }
}
