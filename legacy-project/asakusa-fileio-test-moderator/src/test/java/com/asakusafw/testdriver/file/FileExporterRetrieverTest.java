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
package com.asakusafw.testdriver.file;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.configuration.HadoopEnvironmentChecker;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.external.FileExporterDescription;

/**
 * Test for {@link FileExporterRetriever}.
 */
public class FileExporterRetrieverTest {

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
        MockFileExporter exporter = new MockFileExporter(Text.class, TextOutputFormat.class, "target/testing/hello");
        FileExporterRetriever retriever = new FileExporterRetriever(factory);

        putTextRaw("target/testing/hello", "Hello, world!\nThis is a test.\n".getBytes("UTF-8"));

        MockTextDefinition definition = new MockTextDefinition();
        DataModelSource result = retriever.createSource(definition, exporter, EMPTY);
        try {
            DataModelReflection ref;
            ref = result.next();
            assertThat(ref, is(not(nullValue())));
            assertThat(definition.toObject(ref), is(new Text("Hello, world!")));

            ref = result.next();
            assertThat(ref, is(not(nullValue())));
            assertThat(definition.toObject(ref), is(new Text("This is a test.")));

            ref = result.next();
            assertThat(ref, is(nullValue()));
        } finally {
            result.close();
        }
    }

    /**
     * using sequence file.
     * @throws Exception if test was failed
     */
    @Test
    public void sequenceFile() throws Exception {
        MockFileExporter exporter = new MockFileExporter(Text.class, SequenceFileOutputFormat.class, "target/testing/hello");
        FileExporterRetriever retriever = new FileExporterRetriever(factory);

        putTextSequenceFile("target/testing/hello", "Hello, world!", "This is a test.");

        MockTextDefinition definition = new MockTextDefinition();
        DataModelSource result = retriever.createSource(definition, exporter, EMPTY);
        try {
            DataModelReflection ref;
            ref = result.next();
            assertThat(ref, is(not(nullValue())));
            assertThat(definition.toObject(ref), is(new Text("Hello, world!")));

            ref = result.next();
            assertThat(ref, is(not(nullValue())));
            assertThat(definition.toObject(ref), is(new Text("This is a test.")));

            ref = result.next();
            assertThat(ref, is(nullValue()));
        } finally {
            result.close();
        }
    }

    private void putTextRaw(String path, byte[] bytes) throws IOException {
        FSDataOutputStream output = fileSystem.create(new Path(path), true);
        try {
            output.write(bytes);
        } finally {
            output.close();
        }
    }

    private void putTextSequenceFile(String path, String... lines) throws IOException {
        SequenceFile.Writer writer = new SequenceFile.Writer(
                fileSystem,
                factory.newInstance(),
                new Path(path),
                NullWritable.class,
                Text.class);
        try {
            for (String s : lines) {
                writer.append(NullWritable.get(), new Text(s));
            }
        } finally {
            writer.close();
        }
    }

    @SuppressWarnings("rawtypes")
    private static class MockFileExporter extends FileExporterDescription {

        private final Class<?> modelType;

        private final Class<? extends FileOutputFormat> format;

        private final String pathPrefix;

        MockFileExporter(Class<?> modelType, Class<? extends FileOutputFormat> format, String pathPrefix) {
            assert modelType != null;
            assert format != null;
            assert pathPrefix != null;
            this.modelType = modelType;
            this.format = format;
            this.pathPrefix = pathPrefix;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Class<? extends FileOutputFormat> getOutputFormat() {
            return format;
        }

        @Override
        public String getPathPrefix() {
            return pathPrefix;
        }
    }
}
