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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceProvider;
import com.asakusafw.testdriver.core.SpiDataModelSourceProvider;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.file.resource.FileResourceProvider;

/**
 * Test for {@link WindGateSourceProvider}.
 */
public class WindGateSourceProviderTest {

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

    volatile static File file;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        file = folder.newFile("file");
        Properties profile = context.getTemplate();
        profile.setProperty("resource.file", FileResourceProvider.class.getName());
        context.put("testing", profile);
        try (FileOutputStream output = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(output)) {
            out.writeObject("Hello1, world!");
            out.writeObject("Hello2, world!");
            out.writeObject("Hello3, world!");
        }
    }

    /**
     * Opens importer description as a source.
     * @throws Exception if occur
     */
    @Test
    public void open_importer() throws Exception {
        DataModelSourceProvider provider = new SpiDataModelSourceProvider(getClass().getClassLoader());
        URI uri = new URI("windgate:" + MockImporter.class.getName());
        ValueDefinition<String> definition = ValueDefinition.of(String.class);
        try (DataModelSource source = provider.open(definition, uri, context.get())) {
            DataModelReflection r1 = source.next();
            assertThat(r1, is(notNullValue()));
            assertThat(definition.toObject(r1), is("Hello1, world!"));

            DataModelReflection r2 = source.next();
            assertThat(r2, is(notNullValue()));
            assertThat(definition.toObject(r2), is("Hello2, world!"));

            DataModelReflection r3 = source.next();
            assertThat(r3, is(notNullValue()));
            assertThat(definition.toObject(r3), is("Hello3, world!"));

            DataModelReflection r4 = source.next();
            assertThat(r4, is(nullValue()));
        }
    }

    /**
     * Opens exporter description as a source.
     * @throws Exception if occur
     */
    @Test
    public void open_exporter() throws Exception {
        DataModelSourceProvider provider = new SpiDataModelSourceProvider(getClass().getClassLoader());
        URI uri = new URI("windgate:" + MockExporter.class.getName());
        ValueDefinition<String> definition = ValueDefinition.of(String.class);
        try (DataModelSource source = provider.open(definition, uri, context.get())) {
            DataModelReflection r1 = source.next();
            assertThat(r1, is(notNullValue()));
            assertThat(definition.toObject(r1), is("Hello1, world!"));

            DataModelReflection r2 = source.next();
            assertThat(r2, is(notNullValue()));
            assertThat(definition.toObject(r2), is("Hello2, world!"));

            DataModelReflection r3 = source.next();
            assertThat(r3, is(notNullValue()));
            assertThat(definition.toObject(r3), is("Hello3, world!"));

            DataModelReflection r4 = source.next();
            assertThat(r4, is(nullValue()));
        }
    }

    /**
     * Attempts to open URI with invalid scheme.
     * @throws Exception if occur
     */
    @Test
    public void invalid_scheme() throws Exception {
        DataModelSourceProvider provider = new WindGateSourceProvider();
        URI uri = new URI("INVALIDwindgate:" + MockExporter.class.getName());
        ValueDefinition<String> definition = ValueDefinition.of(String.class);
        DataModelSource source = provider.open(definition, uri, context.get());
        assertThat(source, is(nullValue()));
    }

    /**
     * Attempts to open URI with missing class.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void unknown_class() throws Exception {
        DataModelSourceProvider provider = new WindGateSourceProvider();
        URI uri = new URI("windgate:__INVALID__");
        ValueDefinition<String> definition = ValueDefinition.of(String.class);
        provider.open(definition, uri, context.get());
    }

    /**
     * Attempts to open URI with unexpected class.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void unexpected_class() throws Exception {
        DataModelSourceProvider provider = new WindGateSourceProvider();
        URI uri = new URI("windgate:" + String.class.getName());
        ValueDefinition<String> definition = ValueDefinition.of(String.class);
        provider.open(definition, uri, context.get());
    }

    /**
     * Importer description for testing.
     */
    public static class MockImporter extends WindGateImporterDescription {

        @Override
        public Class<?> getModelType() {
            return String.class;
        }

        @Override
        public String getProfileName() {
            return "testing";
        }

        @Override
        public DriverScript getDriverScript() {
            DriverScript driver = new DriverScript(
                    "file",
                    Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
            return driver;
        }
    }

    /**
     * Exporter description for testing.
     */
    public static class MockExporter extends WindGateExporterDescription {

        @Override
        public Class<?> getModelType() {
            return String.class;
        }

        @Override
        public String getProfileName() {
            return "testing";
        }

        @Override
        public DriverScript getDriverScript() {
            DriverScript driver = new DriverScript(
                    "file",
                    Collections.singletonMap(FileProcess.FILE.key(), file.getAbsolutePath()));
            return driver;
        }
    }
}
