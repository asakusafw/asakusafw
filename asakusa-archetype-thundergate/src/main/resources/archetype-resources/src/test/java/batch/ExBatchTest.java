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
package ${package}.batch;

import ${package}.modelgen.table.model.Ex1;

import org.junit.Test;

import com.asakusafw.testdriver.BatchTester;
import com.asakusafw.testdriver.JobFlowTester;

/**
 * サンプル：バッチのテストクラス
 */
public class ExBatchTest {

    /**
     * サンプル：バッチの実行
     */
    @Test
    public void testExample() {

        BatchTester batchDriver = new BatchTester(this.getClass());
        String testDataSheet = "EX1.xls";

        JobFlowTester driver = batchDriver.jobflow("ex");
        driver.input("ex1", Ex1.class).prepare(testDataSheet);
        driver.output("ex1", Ex1.class).verify(testDataSheet + "#:1", testDataSheet + "#:2");

        batchDriver.runTest(ExBatch.class);
    }
}
