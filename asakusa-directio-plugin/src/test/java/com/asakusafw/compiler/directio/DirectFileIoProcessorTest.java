/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.compiler.directio.testing.model.Line1;
import com.asakusafw.compiler.directio.testing.model.Line2;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link DirectFileIoProcessor}.
 */
public class DirectFileIoProcessorTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple.
     */
    @Test
    public void validate() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * dual inputs and outputs.
     */
    @Test
    public void validate_dual() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in1 = flow.createIn("in1",
                new Input(Line1.class, LineFormat.class, "input", "input-1.txt", DataSize.LARGE));
        In<Line2> in2 = flow.createIn("in2",
                new Input(Line2.class, LineFormat.class, "input", "input-2.txt", DataSize.TINY));
        Out<Line1> out1 = flow.createOut("out1",
                new Output(Line1.class, LineFormat.class, "output-1", "output.txt"));
        Out<Line2> out2 = flow.createOut("out2",
                new Output(Line2.class, LineFormat.class, "output-2", "output.txt"));

        FlowDescription desc = new DualIdentityFlow<Line1, Line2>(in1, in2, out1, out2);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * no input.
     */
    @Test
    public void validate_no_input() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new DirectImporterDescription(Line1.class, "some/file-*"));
        Out<Line1> out = flow.createOut("out1", new Output("something", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * no output.
     */
    @Test
    public void validate_no_output() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("something", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new DirectExporterDescription(Line1.class, "some/file-*"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * input size is tiny.
     */
    @Test
    public void validate_input_tiny() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input(
                Line1.class, LineFormat.class, "input", "input.txt", DataSize.TINY));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * input contains a variable.
     */
    @Test
    public void validate_input_variable() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "${input}.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * output parameterize.
     */
    @Test
    public void validate_output_pattern() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "{first}.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * output contains a variable.
     */
    @Test
    public void validate_output_variable() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "${output}.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * output ordering.
     */
    @Test
    public void validate_output_asc() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt", "+value"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * output ordering.
     */
    @Test
    public void validate_output_desc() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt", "-value"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * output ordering.
     */
    @Test
    public void validate_output_complexorder() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt", "-length", "+position"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * invalid input resource.
     */
    @Test
    public void invalid_input_resource() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn(
                "in1",
                new Input(Line1.class, LineFormat.class, "input", "?", DataSize.UNKNOWN));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid input format.
     */
    @Test
    public void no_input_format() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn(
                "in1",
                new Input(Line1.class, null, "input", "*", DataSize.UNKNOWN));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid input format.
     */
    @Test
    public void invalid_input_format() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn(
                "in1",
                new Input(Line1.class, PrivateFormat.class, "input", "*", DataSize.UNKNOWN));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid input format.
     */
    @Test
    public void inconsistent_input_format() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn(
                "in1",
                new Input(Line1.class, VoidFormat.class, "input", "*", DataSize.UNKNOWN));
        Out<Line1> out = flow.createOut("out1", new Output("output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid output resource.
     */
    @Test
    public void invalid_output_resource() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1",
                new Output(Line1.class, LineFormat.class, "output", "?", "position"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid output resource.
     */
    @Test
    public void invalid_output_order() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1",
                new Output(Line1.class, LineFormat.class, "output", "output.txt", "?"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid output format.
     */
    @Test
    public void no_output_format() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1",
                new Output(Line1.class, null, "output", "output.txt", "position"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid output format.
     */
    @Test
    public void invalid_output_format() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1",
                new Output(Line1.class, PrivateFormat.class, "output", "output.txt", "position"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * invalid output format.
     */
    @Test
    public void inconsistent_output_format() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("input", "input.txt"));
        Out<Line1> out = flow.createOut("out1",
                new Output(Line1.class, VoidFormat.class, "output", "output.txt", "position"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * conflict input and output.
     */
    @Test
    public void input_conflict_output() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("path/conflict", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("path/conflict", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * input contains other output (allowed).
     */
    @Test
    public void input_contains_output() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("conflict", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("conflict/output", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * conflict input and output.
     */
    @Test
    public void output_contains_input() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in = flow.createIn("in1", new Input("conflict/input", "input.txt"));
        Out<Line1> out = flow.createOut("out1", new Output("conflict", "output.txt"));

        FlowDescription desc = new IdentityFlow<Line1>(in, out);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * conflict between outputs.
     */
    @Test
    public void output_conflict_output() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in1 = flow.createIn("in1", new Input("input", "input-1.txt"));
        In<Line1> in2 = flow.createIn("in2", new Input("input", "input-2.txt"));
        Out<Line1> out1 = flow.createOut("out1", new Output("conflict", "output.txt"));
        Out<Line1> out2 = flow.createOut("out2", new Output("conflict", "output.txt"));

        FlowDescription desc = new DualIdentityFlow<Line1, Line1>(in1, in2, out1, out2);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    /**
     * outputs has the common prefix (allowed).
     */
    @Test
    public void output_common_prefix() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in1 = flow.createIn("in1", new Input("input", "input-1.txt"));
        In<Line1> in2 = flow.createIn("in2", new Input("input", "input-2.txt"));
        Out<Line1> out1 = flow.createOut("out1", new Output("conflict/a", "output.txt"));
        Out<Line1> out2 = flow.createOut("out2", new Output("conflict/aa", "output.txt"));

        FlowDescription desc = new DualIdentityFlow<Line1, Line1>(in1, in2, out1, out2);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(not(nullValue())));
    }

    /**
     * conflict between outputs.
     */
    @Test
    public void output_contains_output() {
        FlowDescriptionDriver flow = new FlowDescriptionDriver();
        In<Line1> in1 = flow.createIn("in1", new Input("input", "input-1.txt"));
        In<Line1> in2 = flow.createIn("in2", new Input("input", "input-2.txt"));
        Out<Line1> out1 = flow.createOut("out1", new Output("conflict", "output.txt"));
        Out<Line1> out2 = flow.createOut("out2", new Output("conflict/internal", "output.txt"));

        FlowDescription desc = new DualIdentityFlow<Line1, Line1>(in1, in2, out1, out2);
        JobflowInfo info = compile(flow, desc);
        assertThat(info, is(nullValue()));
    }

    JobflowInfo compile(FlowDescriptionDriver flow, FlowDescription desc) {
        try {
            return DirectFlowCompiler.compile(
                    flow.createFlowGraph(desc),
                    "test",
                    "test",
                    "com.example",
                    Location.fromPath("target/testing", '/'),
                    folder.newFolder("build"),
                    Collections.<File>emptyList(),
                    getClass().getClassLoader(),
                    FlowCompilerOptions.load(System.getProperties()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Input extends DirectFileInputDescription {

        private final Class<?> modelType;
        private final Class<? extends BinaryStreamFormat<?>> format;
        private final String basePath;
        private final String resourcePattern;
        private final DataSize dataSize;

        Input(
                Class<?> modelType,
                Class<? extends BinaryStreamFormat<?>> format,
                String basePath,
                String resourcePattern,
                DataSize dataSize) {
            this.modelType = modelType;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.format = format;
            this.dataSize = dataSize;
        }

        Input(
                String basePath,
                String resourcePattern) {
            this.modelType = Line1.class;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.format = LineFormat.class;
            this.dataSize = DataSize.UNKNOWN;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Class<? extends BinaryStreamFormat<?>> getFormat() {
            return format;
        }

        @Override
        public String getBasePath() {
            return basePath;
        }

        @Override
        public String getResourcePattern() {
            return resourcePattern;
        }

        @Override
        public DataSize getDataSize() {
            return dataSize;
        }
    }

    private static class Output extends DirectFileOutputDescription {

        private final Class<?> modelType;
        private final Class<? extends BinaryStreamFormat<?>> format;
        private final String basePath;
        private final String resourcePattern;
        private final String[] order;

        Output(
                Class<?> modelType,
                Class<? extends BinaryStreamFormat<?>> format,
                String basePath,
                String resourcePattern,
                String... order) {
            this.modelType = modelType;
            this.format = format;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.order = order;
        }

        Output(
                String basePath,
                String resourcePattern,
                String... order) {
            this.modelType = Line1.class;
            this.format = LineFormat.class;
            this.basePath = basePath;
            this.resourcePattern = resourcePattern;
            this.order = order;
        }

        @Override
        public Class<?> getModelType() {
            return modelType;
        }

        @Override
        public Class<? extends BinaryStreamFormat<?>> getFormat() {
            return format;
        }

        @Override
        public String getBasePath() {
            return basePath;
        }

        @Override
        public String getResourcePattern() {
            return resourcePattern;
        }

        @Override
        public List<String> getOrder() {
            return Arrays.asList(order);
        }
    }

    /**
     * Mock {@link BinaryStreamFormat}.
     * @param <T> supported type
     */
    protected abstract static class MockAbstractFormat<T> extends BinaryStreamFormat<T> {

        private final Class<T> type;

        /**
         * Creates a new instance.
         * @param type type
         */
        public MockAbstractFormat(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> getSupportedType() {
            return type;
        }

        @Override
        public long getPreferredFragmentSize() throws IOException, InterruptedException {
            return -1;
        }

        @Override
        public long getMinimumFragmentSize() throws IOException, InterruptedException {
            return -1;
        }

        @Override
        public ModelInput<T> createInput(Class<? extends T> dataType, String path,
                InputStream stream, long offset, long fragmentSize) throws IOException,
                InterruptedException {
            return null;
        }

        @Override
        public ModelOutput<T> createOutput(Class<? extends T> dataType, String path,
                OutputStream stream) throws IOException, InterruptedException {
            return null;
        }
    }

    /**
     * Supports nothing.
     */
    public static class VoidFormat extends MockAbstractFormat<Void> {

        /**
         * Creates a new instance.
         */
        public VoidFormat() {
            super(Void.class);
        }
    }

    /**
     * Cannot instantiation.
     */
    private static final class PrivateFormat extends MockAbstractFormat<Object> {

        private PrivateFormat() {
            super(Object.class);
        }
    }
}
