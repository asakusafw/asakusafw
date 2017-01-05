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

import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.testing.TemporaryInputDescription;
import com.asakusafw.runtime.configuration.HadoopEnvironmentChecker;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.mapreduce.mock.MockTextDefinition;
import com.asakusafw.utils.collections.Sets;

/**
 * Test for {@link TemporaryInputPreparator}.
 */
public class TemporaryImporterPreparatorTest {

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
     * simple case.
     * @throws Exception if test was failed
     */
    @Test
    public void simple() throws Exception {
        TemporaryInputPreparator target = new TemporaryInputPreparator(factory);
        try (ModelOutput<Text> open = target.createOutput(
                new MockTextDefinition(),
                new MockTemporaryImporter(Text.class, "target/testing/input"),
                EMPTY)) {
            open.write(new Text("Hello, world!"));
            open.write(new Text("This is a test."));
        }
        try (ModelInput<Text> input = TemporaryStorage.openInput(
                factory.newInstance(),
                Text.class,
                new Path("target/testing/input"))) {
            Text text = new Text();
            assertThat(input.readTo(text), is(true));
            assertThat(text.toString(), is("Hello, world!"));
            assertThat(input.readTo(text), is(true));
            assertThat(text.toString(), is("This is a test."));
            assertThat(input.readTo(text), is(false));
        }
    }

    private static class MockTemporaryImporter extends TemporaryInputDescription {

        private final Class<?> modelType;

        private final Set<String> paths;

        MockTemporaryImporter(Class<?> modelType, String... paths) {
            this.modelType = modelType;
            this.paths = Sets.from(paths);
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Set<String> getPaths() {
            return paths;
        }
    }
}
