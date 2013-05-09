/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.processor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.processor.flow.MasterCheckFlowSelection;
import com.asakusafw.compiler.flow.processor.flow.MasterCheckFlowTrivial;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;

/**
 * Test for {@link MasterCheckFlowProcessor}.
 */
public class MasterCheckFlowProcessorTest {

    /**
     * テストヘルパ。
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * 単純なテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void trivial() throws Exception {
        runEq(DataSize.UNKNOWN);
    }

    /**
     * TINYのテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void tiny() throws Exception {
        runEq(DataSize.TINY);
    }

    private void runEq(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2", dataSize);
        TestOutput<Ex1> found = tester.output(Ex1.class, "found");
        TestOutput<Ex1> missing = tester.output(Ex1.class, "missing");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        ex2.setStringAsString("both");
        in2.add(ex2);
        ex1.setStringAsString("both");
        ex1.setSid(1);
        in1.add(ex1);

        ex2.setStringAsString("only-master");
        in2.add(ex2);
        ex1.setStringAsString("only-tx");
        ex1.setSid(2);
        in1.add(ex1);
        ex1.setStringAsString("only-tx");
        ex1.setSid(3);
        in1.add(ex1);

        ex2.setStringAsString("multi-tx");
        in2.add(ex2);
        ex1.setStringAsString("multi-tx");
        ex1.setSid(4);
        in1.add(ex1);
        ex1.setStringAsString("multi-tx");
        ex1.setSid(5);
        in1.add(ex1);
        ex1.setStringAsString("multi-tx");
        ex1.setSid(6);
        in1.add(ex1);

        assertThat(tester.runFlow(new MasterCheckFlowTrivial(
                in1.flow(), in2.flow(),
                found.flow(), missing.flow())), is(true));

        List<Ex1> foundList = found.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        List<Ex1> missingList = missing.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        assertThat(foundList.size(), is(4));
        assertThat(missingList.size(), is(2));

        assertThat(foundList.get(0).getSid(), is(1L));
        assertThat(missingList.get(0).getSid(), is(2L));
        assertThat(missingList.get(1).getSid(), is(3L));
        assertThat(foundList.get(1).getSid(), is(4L));
        assertThat(foundList.get(2).getSid(), is(5L));
        assertThat(foundList.get(3).getSid(), is(6L));
    }

    /**
     * セレクタつき。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void selection() throws Exception {
        runNoEq(DataSize.UNKNOWN);
    }

    /**
     * TINYセレクタつき。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void tinySelection() throws Exception {
        runNoEq(DataSize.TINY);
    }

    private void runNoEq(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2", dataSize);
        TestOutput<Ex1> found = tester.output(Ex1.class, "found");
        TestOutput<Ex1> missing = tester.output(Ex1.class, "missing");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        ex2.setStringAsString("both");
        ex2.setValue(10);
        in2.add(ex2);
        ex2.setValue(11);
        in2.add(ex2);
        ex1.setStringAsString("both");
        ex1.setValue(11);
        ex1.setSid(11);
        in1.add(ex1);

        ex2.setStringAsString("only-master");
        ex2.setValue(20);
        in2.add(ex2);
        ex2.setValue(21);
        in2.add(ex2);
        ex1.setStringAsString("only-master");
        ex1.setValue(22);
        ex1.setSid(22);
        in1.add(ex1);
        ex1.setValue(23);
        ex1.setSid(23);
        in1.add(ex1);

        ex2.setStringAsString("multi-tx");
        ex2.setValue(34);
        in2.add(ex2);
        ex2.setValue(35);
        in2.add(ex2);
        ex1.setStringAsString("multi-tx");
        ex1.setValue(34);
        ex1.setSid(34);
        in1.add(ex1);
        ex1.setValue(35);
        ex1.setSid(35);
        in1.add(ex1);
        ex1.setValue(36);
        ex1.setSid(36);
        in1.add(ex1);

        assertThat(tester.runFlow(new MasterCheckFlowSelection(
                in1.flow(), in2.flow(),
                found.flow(), missing.flow())), is(true));

        List<Ex1> foundList = found.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        List<Ex1> missingList = missing.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        assertThat(foundList.size(), is(3));
        assertThat(missingList.size(), is(3));

        assertThat(foundList.get(0).getSid(), is(11L));
        assertThat(missingList.get(0).getSid(), is(22L));
        assertThat(missingList.get(1).getSid(), is(23L));
        assertThat(foundList.get(1).getSid(), is(34L));
        assertThat(foundList.get(2).getSid(), is(35L));
        assertThat(missingList.get(2).getSid(), is(36L));
    }
}
