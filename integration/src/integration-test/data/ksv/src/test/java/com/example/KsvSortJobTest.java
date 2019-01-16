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

import com.asakusafw.testdriver.JobFlowTester;
import com.example.KsvSortJob;
import com.example.modelgen.dmdl.model.Ksv;

/**
 * Test for {@link KsvSortJob}.
 */
public class KsvSortJobTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setBatchArg("input", "input");
        tester.setBatchArg("output", "output");

        tester.input("ksv", Ksv.class).prepare("simple.xls#data");
        tester.output("ksv", Ksv.class).verify("simple.xls#data", "simple.xls#rule");
        tester.runTest(KsvSortJob.class);
    }
}
