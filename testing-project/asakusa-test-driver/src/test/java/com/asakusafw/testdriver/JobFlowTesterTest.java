/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.io.File;

import org.junit.Test;

import com.asakusafw.testdriver.testing.flowpart.SimpleFlowPart;
import com.asakusafw.testdriver.testing.jobflow.SimpleJobflow;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.testdriver.testing.operator.SimpleOperator;

/**
 * Test for {@link JobFlowTester}.
 */
public class JobFlowTesterTest extends TesterTestRoot {

    /**
     * simple testing.
     */
    @Test
    public void simple() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * input tracing.
     */
    @Test
    public void trace_in() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.addInputTrace(SimpleFlowPart.class, "in");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * output tracing.
     */
    @Test
    public void trace_out() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.addOutputTrace(SimpleFlowPart.class, "out");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * input/output tracing.
     */
    @Test
    public void trace_both() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.addInputTrace(SimpleFlowPart.class, "in");
        tester.addOutputTrace(SimpleFlowPart.class, "out");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * saves actual data using path string.
     */
    @Test
    public void dumpActual_path() {
        File target = new File("target/testing/dump/actual-path.xls");
        target.delete();
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpActual(target.getPath());
        tester.runTest(SimpleJobflow.class);
        assertThat(target.exists(), is(true));
    }

    /**
     * saves actual data using URI.
     */
    @Test
    public void dumpActual_uri() {
        File target = new File("target/testing/dump/actual-uri.xls");
        target.delete();
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpActual(target.toURI().toString());
        tester.runTest(SimpleJobflow.class);
        assertThat(target.exists(), is(true));
    }

    /**
     * saves actual data using path.
     */
    @Test
    public void dumpActual_file() {
        File target = new File("target/testing/dump/actual-file.xls");
        target.delete();
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class)
            .verify("data/simple-out.json", new IdentityVerifier())
            .dumpActual(target);
        tester.runTest(SimpleJobflow.class);
        assertThat(target.exists(), is(true));
    }

    /**
     * saves difference data using path string.
     */
    @Test
    public void dumpDifference_path() {
        File target = new File("target/testing/dump/difference-path.html");
        target.delete();
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class)
            .verify("data/difference-out.json", new IdentityVerifier())
            .dumpDifference(target.getPath());
        try {
            tester.runTest(SimpleJobflow.class);
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
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class)
            .verify("data/difference-out.json", new IdentityVerifier())
            .dumpDifference(target.toURI().toString());
        try {
            tester.runTest(SimpleJobflow.class);
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
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class)
            .verify("data/difference-out.json", new IdentityVerifier())
            .dumpDifference(target);
        try {
            tester.runTest(SimpleJobflow.class);
            fail();
        } catch (AssertionError e) {
            // ok.
        }
        assertThat(target.exists(), is(true));
    }

    /**
     * Attempts to prepare input with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_input_prepare_name() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("INVALID", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to prepare input with invalid type.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_input_prepare_type() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Void.class).prepare("data/simple-in.json");
        tester.output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to prepare input with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_input_prepare_data() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to prepare output with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_prepare_name() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("INVALID", Simple.class).prepare("data/simple-out.json");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to prepare output with invalid type.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_prepare_type() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Void.class).prepare("data/simple-out.json");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to prepare output with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_prepare_data() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.output("simple", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to verify output with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_verify_name() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("INVALID", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to verify output with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_verify_type() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.input("simple", Simple.class).prepare("data/simple-in.json");
        tester.output("simple", Void.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to verify output with invalid name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_data() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.output("simple", Simple.class).verify("INVALID", new IdentityVerifier());
    }

    /**
     * Attempts to verify output with invalid name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_rule() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.output("simple", Simple.class).verify("data/simple-out.json", "INVALID");
    }

    /**
     * Attempts to trace invalid operator class.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_trace_operator_class() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.addInputTrace(Simple.class, "setValue", "model");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to trace invalid operator method.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_trace_operator_method() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.addInputTrace(SimpleOperator.class, "UNKNOWN", "model");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to trace invalid input.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_trace_input() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.addInputTrace(SimpleOperator.class, "setValue", "UNKNOWN");
        tester.runTest(SimpleJobflow.class);
    }

    /**
     * Attempts to trace invalid output.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_trace_output() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.addOutputTrace(SimpleOperator.class, "setValue", "UNKNOWN");
        tester.runTest(SimpleJobflow.class);
    }
}
