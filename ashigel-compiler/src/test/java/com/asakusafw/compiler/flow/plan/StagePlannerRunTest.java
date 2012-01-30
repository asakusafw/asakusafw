/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.plan;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.example.BranchStage;
import com.asakusafw.compiler.flow.example.CombineStage;
import com.asakusafw.compiler.flow.example.DuplicateStage;
import com.asakusafw.compiler.flow.example.StickyStage;
import com.asakusafw.compiler.flow.example.TwinCogroupStage;
import com.asakusafw.compiler.flow.example.VolatileStage;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.util.CompilerTester;
import com.asakusafw.compiler.util.CompilerTester.TestInput;
import com.asakusafw.compiler.util.CompilerTester.TestOutput;

/**
 * Test for {@link StagePlanner} (with Running).
 */
public class StagePlannerRunTest {

    /**
     * テストへルパ。
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * Stickyの確認。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void keep_sticky() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        in.add(new Ex1());

        boolean result = tester.runFlow(new StickyStage(in.flow()));
        assertThat(result, is(false));
    }

    /**
     * Stickyの確認。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void unify_volatile() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        TestOutput<Ex1> out = tester.output(Ex1.class, "out");
        in.add(new Ex1());

        boolean result = tester.runFlow(new VolatileStage(in.flow(), out.flow()));
        assertThat(result, is(true));

        List<Ex1> outputs = out.toList();
        assertThat(outputs.size(), is(2));
        assertThat(outputs.get(0), equalTo(outputs.get(1)));
    }

    /**
     * duplicateの確認。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void duplicate() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        TestOutput<Ex1> out = tester.output(Ex1.class, "out");
        in.add(new Ex1());

        boolean result = tester.runFlow(new DuplicateStage(in.flow(), out.flow()));
        assertThat(result, is(true));

        List<Ex1> outputs = out.toList();
        assertThat(outputs.size(), is(2));
        assertThat(outputs.get(0), equalTo(outputs.get(1)));
    }

    /**
     * confluentの確認。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void confluent() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        TestOutput<Ex1> out = tester.output(Ex1.class, "out");
        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("Hello");
        ex1.setValue(100);
        in.add(ex1);

        boolean result = tester.runFlow(new TwinCogroupStage(in.flow(), out.flow()));
        assertThat(result, is(true));

        List<Ex1> outputs = out.toList();
        assertThat(outputs.size(), is(1));
        assertThat(outputs.get(0).getValue(), is(200));
    }

    /**
     * branchの確認。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void branch() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        TestOutput<Ex1> out1 = tester.output(Ex1.class, "out1");
        TestOutput<Ex1> out2 = tester.output(Ex1.class, "out2");
        TestOutput<Ex1> out3 = tester.output(Ex1.class, "out3");

        Ex1 model = new Ex1();
        model.setValue(0);
        in.add(model);
        model.setValue(1);
        in.add(model);

        boolean result = tester.runFlow(new BranchStage(
                in.flow(),
                out1.flow(), out2.flow(), out3.flow()));
        assertThat(result, is(true));

        assertThat(out1.toList().size(), is(1));
        assertThat(out2.toList().size(), is(2));
        assertThat(out3.toList().size(), is(1));
    }

    /**
     * combineの確認。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void combine() throws Exception {
        tester.options().setEnableCombiner(true);

        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        TestOutput<Ex1> out1 = tester.output(Ex1.class, "out1");
        TestOutput<Ex1> out2 = tester.output(Ex1.class, "out2");

        Ex1 model = new Ex1();
        model.setStringAsString("a");
        model.setValue(1);
        in.add(model);
        model.setStringAsString("b");
        model.setValue(2);
        in.add(model);
        model.setValue(3);
        in.add(model);
        model.setStringAsString("c");
        model.setValue(4);
        in.add(model);
        model.setValue(5);
        in.add(model);
        model.setValue(6);
        in.add(model);

        boolean result = tester.runFlow(new CombineStage(in.flow(), out1.flow(), out2.flow()));
        assertThat(result, is(true));

        List<Ex1> list1 = out1.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getStringOption().compareTo(o2.getStringOption());
            }
        });
        List<Ex1> list2 = out2.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getStringOption().compareTo(o2.getStringOption());
            }
        });

        assertThat(list1.size(), is(3));
        assertThat(list1.get(0).getStringAsString(), is("a"));
        assertThat(list1.get(1).getStringAsString(), is("b"));
        assertThat(list1.get(2).getStringAsString(), is("c"));
        assertThat(list1.get(0).getValue(), is(1));
        assertThat(list1.get(1).getValue(), is(5));
        assertThat(list1.get(2).getValue(), is(15));

        assertThat(list1, is(list2));
    }
}
