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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.testdriver.testing.flowpart.SimpleFlowPart;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link FlowPartTester}.
 * @since 0.2.0
 */
public class FlowPartTesterTest {

    /**
     * Temporary framework installation target.
     */
    @Rule
    public FrameworkDeployer framework = new FrameworkDeployer();

    /**
     * simple testing.
     */
    @Test
    public void simple() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }

    /**
     * files in archive.
     */
    @Test
    public void inArchive() {
        URL archive = getClass().getResource("data/json-files.jar");
        assertThat(archive, is(notNullValue()));

        URLClassLoader loader = new URLClassLoader(new URL[] { archive });
        URL inUrl = loader.findResource("simple-in.json");
        URL outUrl = loader.findResource("simple-out.json");
        assertThat(inUrl, is(notNullValue()));
        assertThat(outUrl, is(notNullValue()));

        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        In<Simple> in = tester.input("in", Simple.class).prepare(inUrl.toExternalForm());
        Out<Simple> out = tester.output("out", Simple.class).verify(outUrl.toExternalForm(), new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }

    /**
     * path includes white spaces.
     */
    @Ignore("FIXME for invalid characters")
    @Test
    public void withSpace() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/with space-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/with space-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }

    /**
     * using full path.
     */
    @Test
    public void fullpath() {
        String prefix = getClass().getName();
        prefix = prefix.substring(0, prefix.length() - getClass().getSimpleName().length()).replace('.', '/');
        prefix = '/' + prefix;

        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        In<Simple> in = tester.input("in", Simple.class).prepare(prefix + "data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify(prefix + "data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }

    /**
     * simple testing with specified data size.
     */
    @Test
    public void simpleWithDataSize() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        FlowPartDriverInput<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json").withDataSize(DataSize.TINY);
        assertEquals(DataSize.TINY, in.getImporterDescription().getDataSize());
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }


    /**
     * Attempts to prepare input with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_input_prepare_data() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        tester.input("in", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to prepare output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_prepare_data() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        tester.output("out", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to verify output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_data() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        tester.output("out", Simple.class).verify("INVALID", new IdentityVerifier());
    }

    /**
     * Attempts to verify output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_rule() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        tester.output("out", Simple.class).verify("data/simple-out.json", "INVALID");
    }

    @Test
    public void skip() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        tester.skipCleanInput(true);
        tester.skipCleanOutput(true);
        tester.skipPrepareInput(true);
        tester.skipPrepareOutput(true);
        tester.skipRunJobflow(true);
        tester.skipVerify(true);
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }

}
