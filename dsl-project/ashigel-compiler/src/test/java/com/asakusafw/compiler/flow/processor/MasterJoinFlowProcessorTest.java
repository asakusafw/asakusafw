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

import com.asakusafw.compiler.flow.processor.flow.MasterJoinFlowRenameKey;
import com.asakusafw.compiler.flow.processor.flow.MasterJoinFlowSelection;
import com.asakusafw.compiler.flow.processor.flow.MasterJoinFlowTrivial;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.compiler.flow.testing.model.ExJoined2;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;

/**
 * Test for {@link MasterJoinFlowProcessor}.
 */
public class MasterJoinFlowProcessorTest {

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
        runEquality(DataSize.UNKNOWN);
    }

    /**
     * TINYを指定するテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void tiny() throws Exception {
        runEquality(DataSize.TINY);
    }

    private void runEquality(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "Ex1", dataSize);
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2");
        TestOutput<ExJoined> joined = tester.output(ExJoined.class, "joined");
        TestOutput<Ex2> missing = tester.output(Ex2.class, "missing");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        ex1.setValue(10);
        in1.add(ex1);
        ex2.setValue(10);
        ex2.setSid(1);
        in2.add(ex2);

        ex1.setValue(20);
        in1.add(ex1);
        ex2.setValue(21);
        ex2.setSid(2);
        in2.add(ex2);
        ex2.setValue(22);
        ex2.setSid(3);
        in2.add(ex2);

        ex1.setValue(30);
        in1.add(ex1);
        ex2.setValue(30);
        ex2.setSid(4);
        in2.add(ex2);
        ex2.setValue(30);
        ex2.setSid(5);
        in2.add(ex2);
        ex2.setValue(30);
        ex2.setSid(6);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterJoinFlowTrivial(
                in1.flow(), in2.flow(),
                joined.flow(), missing.flow())), is(true));

        List<ExJoined> joinedList = joined.toList(new Comparator<ExJoined>() {
            @Override
            public int compare(ExJoined o1, ExJoined o2) {
                return o1.getSid2Option().compareTo(o2.getSid2Option());
            }
        });
        List<Ex2> missingList = missing.toList(new Comparator<Ex2>() {
            @Override
            public int compare(Ex2 o1, Ex2 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        assertThat(joinedList.size(), is(4));
        assertThat(missingList.size(), is(2));

        assertThat(joinedList.get(0).getSid2(), is(1L));
        assertThat(missingList.get(0).getSid(), is(2L));
        assertThat(missingList.get(1).getSid(), is(3L));
        assertThat(joinedList.get(1).getSid2(), is(4L));
        assertThat(joinedList.get(2).getSid2(), is(5L));
        assertThat(joinedList.get(3).getSid2(), is(6L));
    }

    /**
     * キー名を変更するテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void renameKey() throws Exception {
        runRenameKey(DataSize.UNKNOWN);
    }

    /**
     * キー名を変更してTINYを指定するテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void renameKeyTiny() throws Exception {
        runRenameKey(DataSize.TINY);
    }

    private void runRenameKey(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "Ex1", dataSize);
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2");
        TestOutput<ExJoined2> joined = tester.output(ExJoined2.class, "joined");
        TestOutput<Ex2> missing = tester.output(Ex2.class, "missing");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        ex1.setValue(10);
        in1.add(ex1);
        ex2.setValue(10);
        ex2.setSid(1);
        in2.add(ex2);

        ex1.setValue(20);
        in1.add(ex1);
        ex2.setValue(21);
        ex2.setSid(2);
        in2.add(ex2);
        ex2.setValue(22);
        ex2.setSid(3);
        in2.add(ex2);

        ex1.setValue(30);
        in1.add(ex1);
        ex2.setValue(30);
        ex2.setSid(4);
        in2.add(ex2);
        ex2.setValue(30);
        ex2.setSid(5);
        in2.add(ex2);
        ex2.setValue(30);
        ex2.setSid(6);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterJoinFlowRenameKey(
                in1.flow(), in2.flow(),
                joined.flow(), missing.flow())), is(true));

        List<ExJoined2> joinedList = joined.toList(new Comparator<ExJoined2>() {
            @Override
            public int compare(ExJoined2 o1, ExJoined2 o2) {
                return o1.getSid2Option().compareTo(o2.getSid2Option());
            }
        });
        List<Ex2> missingList = missing.toList(new Comparator<Ex2>() {
            @Override
            public int compare(Ex2 o1, Ex2 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        assertThat(joinedList.size(), is(4));
        assertThat(missingList.size(), is(2));

        assertThat(joinedList.get(0).getSid2(), is(1L));
        assertThat(missingList.get(0).getSid(), is(2L));
        assertThat(missingList.get(1).getSid(), is(3L));
        assertThat(joinedList.get(1).getSid2(), is(4L));
        assertThat(joinedList.get(2).getSid2(), is(5L));
        assertThat(joinedList.get(3).getSid2(), is(6L));
    }

    /**
     * セレクタつきのテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void selection() throws Exception {
        runNoEquality(DataSize.UNKNOWN);
    }

    /**
     * セレクタつきTINYのテスト。
     * @throws Exception テストが失敗した場合
     */
    @Test
    public void tinySelection() throws Exception {
        runNoEquality(DataSize.TINY);
    }

    private void runNoEquality(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1", dataSize);
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2");
        TestOutput<ExJoined> joined = tester.output(ExJoined.class, "joined");
        TestOutput<Ex2> missing = tester.output(Ex2.class, "missing");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        ex1.setValue(10);
        ex1.setStringAsString("a");
        ex1.setSid(10);
        in1.add(ex1);
        ex1.setStringAsString("b");
        ex1.setSid(11);
        in1.add(ex1);
        ex1.setStringAsString("c");
        ex1.setSid(12);
        in1.add(ex1);
        ex2.setValue(10);
        ex2.setStringAsString("a");
        ex2.setSid(1);
        in2.add(ex2);

        ex1.setValue(20);
        ex1.setStringAsString("a");
        ex1.setSid(20);
        in1.add(ex1);
        ex1.setStringAsString("b");
        ex1.setSid(21);
        in1.add(ex1);
        ex1.setStringAsString("c");
        ex1.setSid(22);
        in1.add(ex1);
        ex2.setValue(21);
        ex2.setSid(2);
        ex2.setStringAsString("a");
        in2.add(ex2);
        ex2.setValue(22);
        ex2.setStringAsString("b");
        ex2.setSid(3);
        in2.add(ex2);

        ex1.setValue(30);
        ex1.setStringAsString("a");
        ex1.setSid(30);
        in1.add(ex1);
        ex1.setStringAsString("b");
        ex1.setSid(31);
        in1.add(ex1);
        ex1.setStringAsString("c");
        ex1.setSid(32);
        in1.add(ex1);
        ex2.setValue(30);
        ex2.setStringAsString("a");
        ex2.setSid(4);
        in2.add(ex2);
        ex2.setStringAsString("b");
        ex2.setSid(5);
        in2.add(ex2);
        ex2.setStringAsString("d");
        ex2.setSid(6);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterJoinFlowSelection(
                in1.flow(), in2.flow(),
                joined.flow(), missing.flow())), is(true));

        List<ExJoined> joinedList = joined.toList(new Comparator<ExJoined>() {
            @Override
            public int compare(ExJoined o1, ExJoined o2) {
                return o1.getSid2Option().compareTo(o2.getSid2Option());
            }
        });
        List<Ex2> missingList = missing.toList(new Comparator<Ex2>() {
            @Override
            public int compare(Ex2 o1, Ex2 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        assertThat(joinedList.size(), is(3));
        assertThat(missingList.size(), is(3));

        assertThat(joinedList.get(0).getSid2(), is(1L));
        assertThat(joinedList.get(0).getSid1(), is(10L));
        assertThat(missingList.get(0).getSid(), is(2L));
        assertThat(missingList.get(1).getSid(), is(3L));
        assertThat(joinedList.get(1).getSid2(), is(4L));
        assertThat(joinedList.get(1).getSid1(), is(30L));
        assertThat(joinedList.get(2).getSid2(), is(5L));
        assertThat(joinedList.get(2).getSid1(), is(31L));
        assertThat(missingList.get(2).getSid(), is(6L));
    }
}
