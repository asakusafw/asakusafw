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
package com.asakusafw.compiler.tool.batchspec;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.compiler.batch.BatchCompilerConfiguration;
import com.asakusafw.compiler.batch.BatchCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.Batch.Parameter;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.google.gson.Gson;

/**
 * Test for {@link BatchSpecProcessor}.
 */
public class BatchSpecProcessorTest {

    /**
     * The temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder() {
        @Override
        protected void before() throws Throwable {
            super.before();
            BatchSpecProcessorTest.this.output = newFolder();
        }
    };

    File output;

    /**
     * simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File file = new File(output, Constants.PATH);

        BatchSpecProcessor proc = createProcessor();
        proc.process(Simple.class);

        assertThat(file.exists(), is(true));
        BatchSpec spec = load(file);

        assertThat(spec.getId(), is("simple"));
        assertThat(spec.getComment(), is(nullValue()));
        assertThat(spec.isStrict(), is(false));
        assertThat(spec.getParameters().size(), is(0));
    }

    /**
     * with comment.
     * @throws Exception if failed
     */
    @Test
    public void comment() throws Exception {
        File file = new File(output, Constants.PATH);

        BatchSpecProcessor proc = createProcessor();
        proc.process(Comment.class);

        assertThat(file.exists(), is(true));
        BatchSpec spec = load(file);

        assertThat(spec.getId(), is("commented"));
        assertThat(spec.getComment(), is("Hello, world!"));
        assertThat(spec.isStrict(), is(false));
        assertThat(spec.getParameters().size(), is(0));
    }

    /**
     * with parameters.
     * @throws Exception if failed
     */
    @Test
    public void parameters() throws Exception {
        File file = new File(output, Constants.PATH);

        BatchSpecProcessor proc = createProcessor();
        proc.process(Parameters.class);

        assertThat(file.exists(), is(true));
        BatchSpec spec = load(file);

        assertThat(spec.getId(), is("parameters"));
        assertThat(spec.getComment(), is(nullValue()));
        assertThat(spec.isStrict(), is(true));
        assertThat(spec.getParameters().size(), is(4));

        com.asakusafw.compiler.tool.batchspec.BatchSpec.Parameter pa = spec.getParameters().get(0);
        assertThat(pa.getKey(), is("a"));
        assertThat(pa.getComment(), is(nullValue()));
        assertThat(pa.isRequired(), is(true));
        assertThat(pa.getPattern(), is(Batch.DEFAULT_PARAMETER_VALUE_PATTERN));

        com.asakusafw.compiler.tool.batchspec.BatchSpec.Parameter pb = spec.getParameters().get(1);
        assertThat(pb.getKey(), is("b"));
        assertThat(pb.getComment(), is("Hello, world!"));
        assertThat(pb.isRequired(), is(true));
        assertThat(pb.getPattern(), is(Batch.DEFAULT_PARAMETER_VALUE_PATTERN));

        com.asakusafw.compiler.tool.batchspec.BatchSpec.Parameter pc = spec.getParameters().get(2);
        assertThat(pc.getKey(), is("c"));
        assertThat(pc.getComment(), is(nullValue()));
        assertThat(pc.isRequired(), is(false));
        assertThat(pc.getPattern(), is(Batch.DEFAULT_PARAMETER_VALUE_PATTERN));

        com.asakusafw.compiler.tool.batchspec.BatchSpec.Parameter pd = spec.getParameters().get(3);
        assertThat(pd.getKey(), is("d"));
        assertThat(pd.getComment(), is(nullValue()));
        assertThat(pd.isRequired(), is(true));
        assertThat(pd.getPattern(), is("."));
    }

    private BatchSpecProcessor createProcessor() throws IOException {
        File work = folder.newFolder();
        BatchCompilerConfiguration config = DirectBatchCompiler.createConfig(
                "testing",
                "com.example",
                Location.fromPath("target/testing", '/'),
                output,
                work,
                Arrays.asList(new File[] {
                       DirectFlowCompiler.toLibraryPath(getClass()),
                       DirectFlowCompiler.toLibraryPath(StageConstants.class),
                }),
                getClass().getClassLoader(),
                FlowCompilerOptions.load(System.getProperties()));
        BatchCompilingEnvironment env = new BatchCompilingEnvironment(config).bless();
        BatchSpecProcessor result = new BatchSpecProcessor();
        result.initialize(env);
        return result;
    }

    private BatchSpec load(File file) throws IOException {
        Gson gson = new Gson();
        try (InputStream in = new FileInputStream(file);
                Reader reader = new InputStreamReader(in, Constants.ENCODING);) {
            BatchSpec result = gson.fromJson(reader, BatchSpec.class);
            return result;
        }
    }

    @Batch(name = "simple")
    private static class Simple extends BatchDescription {

        @Override
        protected void describe() {
            return;
        }
    }

    @Batch(name = "commented", comment = "Hello, world!")
    private static class Comment extends BatchDescription {

        @Override
        protected void describe() {
            return;
        }
    }

    @Batch(name = "parameters", strict = true, parameters = {
            @Parameter(key = "a"),
            @Parameter(key = "b", comment = "Hello, world!"),
            @Parameter(key = "c", required = false),
            @Parameter(key = "d", pattern = ".")
    })
    private static class Parameters extends BatchDescription {

        @Override
        protected void describe() {
            return;
        }
    }
}
