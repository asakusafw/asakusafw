/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.flow.example.BranchStage;
import com.asakusafw.compiler.flow.example.CombineStage;
import com.asakusafw.compiler.flow.example.DuplicateStage;
import com.asakusafw.compiler.flow.example.SplitStage;
import com.asakusafw.compiler.flow.example.StickyStage;
import com.asakusafw.compiler.flow.example.TwinCogroupStage;
import com.asakusafw.compiler.flow.example.VolatileStage;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory.Simple;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Branch;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Cogroup;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.CogroupAdd;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Update;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Checkpoint;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Confluent;

/**
 * Test for {@link StagePlanner} (with Running).
 */
@RunWith(Parameterized.class)
public class StagePlannerRunTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * A test helper.
     */
    @Rule
    public final CompilerTester tester;

    /**
     * Returns test parameter sets.
     * @return test parameter sets
     */
    @Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { GenericOptionValue.DISABLED },
                { GenericOptionValue.ENABLED },
        });
    }

    /**
     * Creates a new instance.
     * @param opt options
     */
    public StagePlannerRunTest(GenericOptionValue opt) {
        tester = new CompilerTester();
        tester.options().putExtraAttribute(StagePlanner.KEY_COMPRESS_FLOW_BLOCK_GROUP, opt.getSymbol());
    }

    /**
     * Sticky annotation.
     * @throws Exception if exception was occurred
     */
    @Test
    public void keep_sticky() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        in.add(new Ex1());

        boolean result = tester.runFlow(new StickyStage(in.flow()));
        assertThat(result, is(false));
    }

    /**
     * Volatile annotation.
     * @throws Exception if exception was occurred
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
     * duplicate flow.
     * @throws Exception if exception was occurred
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
     * confluent flow.
     * @throws Exception if exception was occurred
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
     * branch flow.
     * @throws Exception if exception was occurred
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
     * split and unify.
     * @throws Exception if failed
     */
    @Test
    public void split_unify() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "in");
        TestOutput<Ex1> out1 = tester.output(Ex1.class, "out1");
        TestOutput<Ex1> out2 = tester.output(Ex1.class, "out2");

        Ex1 model = new Ex1();
        model.setValue(0);
        in.add(model);

        boolean result = tester.runFlow(new SplitStage(in.flow(), out1.flow(), out2.flow()));
        assertThat(result, is(true));

        assertThat(out1.toList().size(), is(1));
        assertThat(out2.toList().size(), is(1));
    }

    /**
     * split and unify.
     * @throws Exception if failed
     */
    @Test
    public void ident_unify() throws Exception {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "in1");
        TestInput<Ex1> in2 = tester.input(Ex1.class, "in2");
        TestOutput<Ex1> out1 = tester.output(Ex1.class, "out1");

        Ex1 model = new Ex1();
        model.setValue(1);
        in1.add(model);
        model.setValue(2);
        in2.add(model);

        final In<Ex1> pIn1 = in1.flow();
        final In<Ex1> pIn2 = in2.flow();
        final Out<Ex1> pOut1 = out1.flow();
        boolean result = tester.runFlow(new FlowDescription() {
            @Override
            protected void describe() {
                UpdateFlowFactory uf = new UpdateFlowFactory();
                ExOperatorFactory f = new ExOperatorFactory();
                CoreOperatorFactory c = new CoreOperatorFactory();
                Confluent<Ex1> in = c.confluent(pIn1, pIn2);
                Simple simple = uf.simple(in);
                Checkpoint<Ex1> cp = c.checkpoint(simple.out);
                Cogroup cog = f.cogroup(cp, c.empty(Ex2.class));
                c.stop(cog.r2);
                pOut1.add(cog.r1);
            }
        });
        assertThat(result, is(true));
        assertThat(out1.toList().size(), is(2));
    }

    /**
     * combine.
     * @throws Exception if exception was occurred
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

        List<Ex1> list1 = out1.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        List<Ex1> list2 = out2.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));

        assertThat(list1.size(), is(3));
        assertThat(list1.get(0).getStringAsString(), is("a"));
        assertThat(list1.get(1).getStringAsString(), is("b"));
        assertThat(list1.get(2).getStringAsString(), is("c"));
        assertThat(list1.get(0).getValue(), is(1));
        assertThat(list1.get(1).getValue(), is(5));
        assertThat(list1.get(2).getValue(), is(15));

        assertThat(list1, is(list2));
    }

    /**
     * split and unify.
     * @throws Exception if failed
     */
    @Test
    public void compress_flow_block() throws Exception {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "in1");
        TestOutput<Ex1> out1 = tester.output(Ex1.class, "out1");
        TestOutput<Ex1> out2 = tester.output(Ex1.class, "out2");

        Ex1 model = new Ex1();
        model.setValue(0);
        model.setStringAsString("k");
        in1.add(model);

        final In<Ex1> pIn1 = in1.flow();
        final Out<Ex1> pOut1 = out1.flow();
        final Out<Ex1> pOut2 = out2.flow();
        boolean result = tester.runFlow(new FlowDescription() {
            @Override
            protected void describe() {
                CoreOperatorFactory c = new CoreOperatorFactory();
                ExOperatorFactory f = new ExOperatorFactory();
                Update u1 = f.update(pIn1, 1);
                Update u2 = f.update(pIn1, 2);
                CogroupAdd c1 = f.cogroupAdd(c.confluent(u1.out, u2.out));
                CogroupAdd c2 = f.cogroupAdd(c.confluent(u1.out, u2.out));
                pOut1.add(c1.result);
                pOut2.add(c2.result);
            }
        });
        assertThat(result, is(true));
        List<Ex1> r1 = out1.toList();
        List<Ex1> r2 = out2.toList();
        assertThat(r1.size(), is(1));
        assertThat(r2.size(), is(1));
        assertThat(r1.get(0).getValue(), is(3));
        assertThat(r2.get(0).getValue(), is(3));
    }

    /**
     * split and unify.
     * @throws Exception if failed
     */
    @Test
    public void branch_pushdown() throws Exception {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "in1");
        TestOutput<Ex1> out1 = tester.output(Ex1.class, "out1");
        Ex1 model = new Ex1();
        model.setValue(0);
        in1.add(model);
        model.setValue(1);
        in1.add(model);
        model.setValue(2);
        in1.add(model);
        final In<Ex1> pIn1 = in1.flow();
        final Out<Ex1> pOut1 = out1.flow();
        boolean result = tester.runFlow(new FlowDescription() {
            @Override
            protected void describe() {
                ExOperatorFactory f = new ExOperatorFactory();
                CoreOperatorFactory c = new CoreOperatorFactory();
                Cogroup cog1 = f.cogroup(pIn1, c.empty(Ex2.class));
                c.stop(cog1.r2);
                Branch bra = f.branch(cog1.r1);
                c.stop(bra.cancel);
                c.stop(bra.no);
                Cogroup cog2 = f.cogroup(bra.yes, c.empty(Ex2.class));
                c.stop(cog2.r2);
                pOut1.add(cog2.r1);
            }
        });
        assertThat(result, is(true));
        assertThat(out1.toList().size(), is(1));
    }
}
