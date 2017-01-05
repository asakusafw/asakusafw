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
package com.asakusafw.testdriver.mapreduce.io;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.runtime.configuration.HadoopEnvironmentChecker;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.mapreduce.mock.MockTextDefinition;

/**
 * Test for {@link TemporaryOutputRetriever}.
 */
public class TemporaryExporterRetrieverTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    private static final TestContext EMPTY = new TestContext.Empty();

    /**
     * This test class requires Hadoop is installed.
     */
    @Rule
    public HadoopEnvironmentChecker check = new HadoopEnvironmentChecker(false);

    private ConfigurationFactory factory;

    private FileSystem fileSystem;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        factory = ConfigurationFactory.getDefault();
        Configuration conf = factory.newInstance();
        fileSystem = FileSystem.get(conf);
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (fileSystem != null) {
            fileSystem.delete(new Path("target/testing"), true);
        }
    }

    /**
     * minimum test.
     * @throws Exception if test was failed
     */
    @Test
    public void simple() throws Exception {
        MockFileExporter exporter = new MockFileExporter(Text.class, "target/testing/hello");
        TemporaryOutputRetriever retriever = new TemporaryOutputRetriever(factory);

        putText("target/testing/hello", "Hello, world!", "This is a test.");

        MockTextDefinition definition = new MockTextDefinition();
        try (DataModelSource result = retriever.createSource(definition, exporter, EMPTY)) {
            DataModelReflection ref;
            ref = result.next();
            assertThat(ref, is(not(nullValue())));
            assertThat(definition.toObject(ref), is(new Text("Hello, world!")));

            ref = result.next();
            assertThat(ref, is(not(nullValue())));
            assertThat(definition.toObject(ref), is(new Text("This is a test.")));

            ref = result.next();
            assertThat(ref, is(nullValue()));
        }
    }

    private void putText(String path, String... lines) throws IOException {
        try (ModelOutput<Text> output = TemporaryStorage.openOutput(factory.newInstance(), Text.class, new Path(path))) {
            for (String s : lines) {
                output.write(new Text(s));
            }
        }
    }

    private static class MockFileExporter extends TemporaryOutputDescription {

        private final Class<?> modelType;

        private final String pathPrefix;

        MockFileExporter(Class<?> modelType, String pathPrefix) {
            assert modelType != null;
            assert pathPrefix != null;
            this.modelType = modelType;
            this.pathPrefix = pathPrefix;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public String getPathPrefix() {
            return pathPrefix;
        }
    }
}
