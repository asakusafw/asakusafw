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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import com.asakusafw.testdriver.testing.flowpart.DependencyFlowPart;
import com.asakusafw.testdriver.testing.flowpart.InvalidFlowPart;
import com.asakusafw.testdriver.testing.flowpart.SimpleFlowPart;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link FlowPartTester}.
 */
public class FlowPartTesterTest extends TesterTestRoot {

    /**
     * simple testing.
     */
    @Test
    public void simple() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }

    /**
     * saves actual data using path string.
     */
    @Test
    public void dumpActual_path() {
        File target = new File("target/testing/dump/actual-path.xls");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpActual(target.getPath());
        tester.runTest(new SimpleFlowPart(in, out));
        assertThat(target.exists(), is(true));
    }

    /**
     * saves actual data without verifying.
     */
    @Test
    public void dumpActual_noverify() {
        File target = new File("target/testing/dump/actual-noverify.xls");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .dumpActual(target.getPath());
        tester.runTest(new SimpleFlowPart(in, out));
        assertThat(target.exists(), is(true));
    }

    /**
     * saves actual data using URI.
     */
    @Test
    public void dumpActual_uri() {
        File target = new File("target/testing/dump/actual-uri.xls");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpActual(target.toURI().toString());
        tester.runTest(new SimpleFlowPart(in, out));
        assertThat(target.exists(), is(true));
    }

    /**
     * saves actual data using path.
     */
    @Test
    public void dumpActual_file() {
        File target = new File("target/testing/dump/actual-file.xls");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpActual(target);
        tester.runTest(new SimpleFlowPart(in, out));
        assertThat(target.exists(), is(true));
    }

    /**
     * saves difference data using path string.
     */
    @Test
    public void dumpDifference_path() {
        File target = new File("target/testing/dump/difference-path.html");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/difference-out.json", new IdentityVerifier())
            .dumpDifference(target.getPath());
        try {
            tester.runTest(new SimpleFlowPart(in, out));
            fail();
        } catch (AssertionError e) {
            // ok.
        }
        assertThat(target.exists(), is(true));
    }

    /**
     * saves difference data using URI.
     */
    @Test
    public void dumpDifference_uri() {
        File target = new File("target/testing/dump/difference-uri.html");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/difference-out.json", new IdentityVerifier())
            .dumpDifference(target.toURI().toString());
        try {
            tester.runTest(new SimpleFlowPart(in, out));
            fail();
        } catch (AssertionError e) {
            // ok.
        }
        assertThat(target.exists(), is(true));
    }

    /**
     * saves difference data using path.
     */
    @Test
    public void dumpDifference_file() {
        File target = new File("target/testing/dump/difference-file.html");
        target.delete();
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/difference-out.json", new IdentityVerifier())
            .dumpDifference(target);
        try {
            tester.runTest(new SimpleFlowPart(in, out));
            fail();
        } catch (AssertionError e) {
            // ok.
        }
        assertThat(target.exists(), is(true));
    }

    /**
     * empty differences should not create difference file.
     */
    @Test
    public void dumpDifference_none() {
        File target = new File("target/testing/dump/difference-none.html");
        Assume.assumeThat(target.exists() == false || target.delete(), is(true));
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpDifference(target.getPath());
        tester.runTest(new SimpleFlowPart(in, out));
        assertThat(target.exists(), is(false));
    }

    /**
     * files in archive.
     */
    @Test
    public void inArchive() {
        URL archive = getClass().getResource("data/json-files.jar");
        assertThat(archive, is(notNullValue()));

        URLClassLoader loader = new URLClassLoader(new URL[] { archive });
        try {
            URL inUrl = loader.findResource("simple-in.json");
            URL outUrl = loader.findResource("simple-out.json");
            assertThat(inUrl, is(notNullValue()));
            assertThat(outUrl, is(notNullValue()));

            FlowPartTester tester = new FlowPartTester(getClass());
            tester.setFrameworkHomePath(framework.getHome());
            In<Simple> in = tester.input("in", Simple.class).prepare(inUrl.toExternalForm());
            Out<Simple> out = tester.output("out", Simple.class).verify(outUrl.toExternalForm(), new IdentityVerifier());
            tester.runTest(new SimpleFlowPart(in, out));
        } finally {
            closeQuietly(loader);
        }
    }

    /**
     * path includes white spaces.
     */
    @Ignore("FIXME for invalid characters")
    @Test
    public void withSpace() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
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
        tester.setFrameworkHomePath(framework.getHome());
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
        tester.setFrameworkHomePath(framework.getHome());
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
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("in", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to prepare output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_prepare_data() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.output("out", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to verify output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_data() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.output("out", Simple.class).verify("INVALID", new IdentityVerifier());
    }

    /**
     * Attempts to verify output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_rule() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.output("out", Simple.class).verify("data/simple-out.json", "INVALID");
    }

    /**
     * Skips testing phases.
     */
    @Test
    public void skip() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.skipValidateCondition(true);
        tester.skipCleanInput(true);
        tester.skipCleanOutput(true);
        tester.skipPrepareInput(true);
        tester.skipPrepareOutput(true);
        tester.skipRunJobflow(true);
        tester.skipVerify(true);
        In<Simple> in = tester.input("in", Simple.class).prepare("data/invalid-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/invalid-out.json", new IdentityVerifier());
        tester.runTest(new InvalidFlowPart(in, out));
    }

    /**
     * Using dependency libraries.
     */
    @Test
    public void dependency_libraries() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setJobExecutorFactory(null);
        tester.setLibrariesPath(new File("src/test/lib"));
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new DependencyFlowPart(in, out));
    }

    /**
     * batchapps directory must be escaped.
     */
    @Test
    public void escape_batchapps() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());

        File escaped = tester.getDriverContext().getBatchApplicationsInstallationPath();
        tester.runTest(new SimpleFlowPart(in, out));

        assertThat(getDefaultBatchappsLocation().exists(), is(false));

        // escaped batchapps location must be deleted after run the test.
        assertThat(escaped.exists(), is(false));
    }

    /**
     * using explicit batchapps location.
     */
    @Test
    public void use_explicit_batchapps_location() {
        File explicitBatchappsLocation = framework.getWork("explicit-batchapps");

        FlowPartTester tester = new FlowPartTester(getClass());
        tester.getDriverContext().setBatchApplicationsInstallationPath(explicitBatchappsLocation);
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));

        assertThat(explicitBatchappsLocation.exists(), is(true));
        assertThat(getDefaultBatchappsLocation().exists(), is(false));
    }

    /**
     * using system batchapps location.
     */
    @Test
    public void use_system_batchapps_location() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.getDriverContext().useSystemBatchApplicationsInstallationPath(true);
        tester.setFrameworkHomePath(framework.getHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));

        assertThat(getDefaultBatchappsLocation().exists(), is(true));
    }

    private File getDefaultBatchappsLocation() {
        return new File(framework.getHome(), TestDriverContext.DEFAULT_BATCHAPPS_PATH);
    }

    private void closeQuietly(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
