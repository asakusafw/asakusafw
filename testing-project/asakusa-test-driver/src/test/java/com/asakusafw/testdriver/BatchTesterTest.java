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

import org.junit.Test;

import com.asakusafw.testdriver.testing.batch.SimpleBatch;
import com.asakusafw.testdriver.testing.model.Simple;

/**
 * Test for {@link BatchTester}.
 */
public class BatchTesterTest extends TesterTestRoot {

    /**
     * simple testing.
     */
    @Test
    public void simple() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to use invalid jobflow.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_jobflow() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("INVALID").input("simple", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("INVALID").output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to prepare input with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_input_prepare_name() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("INVALID", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to prepare input with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_input_prepare_type() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Void.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("simple", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to prepare input with invalid data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_input_prepare_data() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Simple.class).prepare("INVALID");
    }

    /**
     * Attempts to prepare output with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_prepare_name() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("INVALID", Simple.class).prepare("data/simple-out.json");
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to prepare output with invalid type.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_prepare_type() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("simple", Void.class).prepare("data/simple-out.json");
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to prepare output with invalid type.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_prepare_data() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").output("simple", Simple.class).prepare("INVALID");
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to verify output with invalid name.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_verify_name() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("INVALID", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to verify output with invalid type.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_output_verify_type() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").input("simple", Simple.class).prepare("data/simple-in.json");
        tester.jobflow("simple").output("simple", Void.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(SimpleBatch.class);
    }

    /**
     * Attempts to verify output with invalid expected data.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_data() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").output("simple", Simple.class).verify("INVALID", new IdentityVerifier());
    }

    /**
     * Attempts to verify output with invalid rule.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_output_verify_rule() {
        BatchTester tester = new BatchTester(getClass());
        tester.setFrameworkHomePath(framework.getHome());
        tester.jobflow("simple").output("simple", Simple.class).verify("data/simple-out.json", "INVALID");
    }
}
