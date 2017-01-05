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
package com.asakusafw.compiler.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.example.NoShuffleStage;
import com.asakusafw.compiler.flow.example.SimpleShuffleStage;
import com.asakusafw.compiler.flow.processor.operator.MasterJoinFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.MasterJoinFlowFactory.Join;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Restructure;

/**
 * Test for {@link FlowCompiler}.
 */
public class FlowCompilerTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    @SuppressWarnings("all")
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * map only.
     * @throws Exception if exception was occurred
     */
    @Test
    public void mapOnly() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();
        ex1.setSid(1);
        ex1.setValue(10);
        in.add(ex1);

        FlowDescription flow = new NoShuffleStage(in.flow(), out.flow());
        assertThat(tester.runFlow(flow), is(true));

        List<Ex1> list = out.toList();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getValue(), is(100));
    }

    /**
     * w/ reduce block.
     * @throws Exception if exception was occurred
     */
    @Test
    public void withReduce() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<ExSummarized> out = tester.output(ExSummarized.class, "exs");

        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("group-1");
        ex1.setValue(10);
        in.add(ex1);
        ex1.setValue(20);
        in.add(ex1);
        ex1.setValue(30);
        in.add(ex1);
        ex1.setStringAsString("group-2");
        ex1.setValue(40);
        in.add(ex1);
        ex1.setValue(50);
        in.add(ex1);

        FlowDescription flow = new SimpleShuffleStage(in.flow(), out.flow());
        assertThat(tester.runFlow(flow), is(true));

        List<ExSummarized> list = out.toList((o1, o2) -> o1.getCountOption().compareTo(o2.getCountOption()));
        assertThat(list.size(), is(2));
        assertThat(list.get(0).getValue(), is(90L));
        assertThat(list.get(0).getCount(), is(2L));
        assertThat(list.get(1).getValue(), is(60L));
        assertThat(list.get(1).getCount(), is(3L));
    }

    /**
     * Compiles unified resources.
     * @throws Exception if failed
     */
    @Test
    public void unifiedResources() throws Exception {
        TestInput<Ex1> mst = tester.input(Ex1.class, "mst", DataSize.TINY);
        TestInput<Ex1> in1 = tester.input(Ex1.class, "in1");
        TestInput<Ex1> in2 = tester.input(Ex1.class, "in2");
        TestOutput<ExJoined> out = tester.output(ExJoined.class, "out");

        Ex1 dMst = new Ex1();
        dMst.setSid(1);
        dMst.setValue(10);
        mst.add(dMst);

        Ex1 dIn1 = new Ex1();
        dIn1.setSid(2);
        dIn1.setValue(10);
        in1.add(dIn1);

        Ex1 dIn2 = new Ex1();
        dIn2.setSid(3);
        dIn2.setValue(10);
        in2.add(dIn2);

        final In<Ex1> pMst = mst.flow();
        final In<Ex1> pIn1 = in1.flow();
        final In<Ex1> pIn2 = in2.flow();
        final Out<ExJoined> pOut = out.flow();
        FlowDescription flow = new FlowDescription() {
            @Override
            protected void describe() {
                CoreOperatorFactory c = new CoreOperatorFactory();
                Restructure<Ex2> r1 = c.restructure(pIn1, Ex2.class);
                Restructure<Ex2> r2 = c.restructure(pIn2, Ex2.class);

                MasterJoinFlowFactory f = new MasterJoinFlowFactory();
                Join join = f.join(pMst, c.confluent(r1, r2));
                c.stop(join.missed);
                pOut.add(join.joined);
            }
        };
        assertThat(tester.runFlow(flow), is(true));
        assertThat(out.toList().size(), is(2));
    }
}
