/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateProcessDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.file.resource.FileResourceProvider;
import com.asakusafw.windgate.file.resource.Preparable;

/**
 * Test for {@link WindGateTestHelper}.
 * @since 0.2.2
 */
public class WindGateTestHelperTest {

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
     * Test for {@link WindGateTestHelper#createProcessScript(Class, WindGateImporterDescription)}
     * @throws Exception if failed
     */
    @Test
    public void createImporterProcessScript() throws Exception {
        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateImporterDescription description = new MockImporterDescription(String.class, "dummy", driver);
        ProcessScript<String> script = WindGateTestHelper.createProcessScript(String.class, description);

        assertThat(script.getDataClass(), equalTo(String.class));
        assertThat(script.getSourceScript().getResourceName(), is("file"));
        assertThat(script.getSourceScript().getConfiguration(), is(driver.getConfiguration()));
    }

    /**
     * Test for {@link WindGateTestHelper#createProcessScript(Class, WindGateExporterDescription)}
     * @throws Exception if failed
     */
    @Test
    public void createExporterProcessScript() throws Exception {
        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateExporterDescription description = new MockExporterDescription(String.class, "dummy", driver);
        ProcessScript<String> script = WindGateTestHelper.createProcessScript(String.class, description);

        assertThat(script.getDataClass(), equalTo(String.class));
        assertThat(script.getDrainScript().getResourceName(), is("file"));
        assertThat(script.getDrainScript().getConfiguration(), is(driver.getConfiguration()));
    }

    /**
     * Test method for {@link WindGateTestHelper#createResourceManipulator(TestContext, WindGateProcessDescription, ParameterList)}.
     * @throws Exception if failed
     */
    @Test
    public void createResourceManipulator() throws Exception {
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

        ResourceManipulator manipulator = WindGateTestHelper.createResourceManipulator(
                new TestContext.Empty(),
                description,
                new ParameterList());
        assertThat(file.exists(), is(true));

        ProcessScript<String> script = WindGateTestHelper.createProcessScript(String.class, description);
        manipulator.cleanupSource(script);
        assertThat(file.exists(), is(false));
    }

    /**
     * Attempts to create manipulator but profile is not found.
     * @throws Exception if failed
     */
    @Test
    public void createResourceManipulator_missing_profile() throws Exception {
        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateImporterDescription description = new MockImporterDescription(
                String.class,
                "__MISSING__",
                driver);

        try {
            WindGateTestHelper.createResourceManipulator(new TestContext.Empty(), description, new ParameterList());
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * Attempts to create manipulator but profile is invalid.
     * @throws Exception if failed
     */
    @Test
    public void createResourceManipulator_invalid_profile() throws Exception {
        Properties profile = context.getTemplate();
        profile.setProperty("resource.file", "__INVALID__");
        context.put("testing", profile);

        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "file",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateImporterDescription description = new MockImporterDescription(
                String.class,
                "testing",
                driver);

        try {
            WindGateTestHelper.createResourceManipulator(new TestContext.Empty(), description, new ParameterList());
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * Attempts to create manipulator but resource is not defined in profile.
     * @throws Exception if failed
     */
    @Test
    public void createResourceManipulator_missing_resource() throws Exception {
        Properties profile = context.getTemplate();
        profile.setProperty("resource.file", FileResourceProvider.class.getName());
        context.put("testing", profile);

        File file = folder.newFile("file");
        DriverScript driver = new DriverScript(
                "missing",
                Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
        WindGateImporterDescription description = new MockImporterDescription(
                String.class,
                "testing",
                driver);

        try {
            WindGateTestHelper.createResourceManipulator(new TestContext.Empty(), description, new ParameterList());
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * Prepare drivers.
     * @throws Exception if failed
     */
    @Test
    public void prepare() throws Exception {
        MockDriver driver = new MockDriver();
        WindGateTestHelper.prepare(driver);
        assertThat(driver.prepared, is(true));
        assertThat(driver.closed, is(false));
    }

    /**
     * Close drivers on preparation failed, and then succeed to close.
     * @throws Exception if failed
     */
    @Test
    public void prepare_fail() throws Exception {
        MockDriver driver = new MockDriver();
        driver.failOnPrepare = true;
        try {
            WindGateTestHelper.prepare(driver);
            fail();
        } catch (IOException e) {
            assertThat(e.getMessage(), is("prepare"));
        }
        assertThat(driver.prepared, is(true));
        assertThat(driver.closed, is(true));
    }

    /**
     * Close drivers on preparation failed, and then failed to close.
     * @throws Exception if failed
     */
    @Test
    public void prepare_close_fail() throws Exception {
        MockDriver driver = new MockDriver();
        driver.failOnPrepare = true;
        driver.failOnClose = true;
        try {
            WindGateTestHelper.prepare(driver);
            fail();
        } catch (IOException e) {
            assertThat(e.getMessage(), is("prepare"));
        }
        assertThat(driver.prepared, is(true));
        assertThat(driver.closed, is(true));
    }

    private static class MockDriver implements Preparable, Closeable {

        boolean failOnPrepare;

        boolean failOnClose;

        boolean prepared;

        boolean closed;

        MockDriver() {
            return;
        }

        @Override
        public void prepare() throws IOException {
            prepared = true;
            if (failOnPrepare) {
                throw new IOException("prepare");
            }
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (failOnClose) {
                throw new IOException("close");
            }
        }
    }
}
