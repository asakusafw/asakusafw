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

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.testdriver.testing.operator.SimpleFlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Test for {@link FlowPartTester}.
 * @since 0.2.0
 */
public class FlowPartTesterTest {

    static final Logger LOG = LoggerFactory.getLogger(FlowPartTesterTest.class);

    /**
     * Temporary framework installation target.
     */
    @Rule
    public FrameworkDeployer framework = new FrameworkDeployer();

    /**
     * simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setFrameworkHomePath(framework.getFrameworkHome());
        In<Simple> in = tester.input("in", Simple.class).prepare("data/simple-in.json");
        Out<Simple> out = tester.output("out", Simple.class).verify("data/simple-out.json", new IdentityVerifier());
        tester.runTest(new SimpleFlowPart(in, out));
    }
}
