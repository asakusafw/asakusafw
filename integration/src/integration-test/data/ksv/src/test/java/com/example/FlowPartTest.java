/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.example;

import org.junit.Test;

import com.asakusafw.testdriver.FlowPartDriverInput;
import com.asakusafw.testdriver.FlowPartDriverOutput;
import com.asakusafw.testdriver.FlowPartTester;
import com.example.modelgen.dmdl.model.Ksv;

/**
 * Test for {@link KsvSortJob} as a flow-part.
 */
public class FlowPartTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        FlowPartTester tester = new FlowPartTester(getClass());
        tester.setBatchArg("input", "input");
        tester.setBatchArg("output", "output");

        FlowPartDriverInput<Ksv> in = tester.input("ksv", Ksv.class).prepare("simple.xls#data");
        FlowPartDriverOutput<Ksv> out = tester.output("ksv", Ksv.class).verify("simple.xls#data", "simple.xls#rule");
        tester.runTest(new KsvSortJob(in, out));
    }
}
