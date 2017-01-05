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
package com.asakusafw.compiler.flow.processor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.processor.flow.FoldFlowSimple;
import com.asakusafw.compiler.flow.processor.flow.FoldFlowWithParameter;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link FoldFlowProcessor}.
 */
public class FoldFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * A test helper
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * simple case.
     * @throws Exception if exception was occurred
     */
    @Test
    public void simple() throws Exception {
        tester.options().setEnableCombiner(false);

        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();

        ex1.setStringAsString("a");
        ex1.setValue(1);
        in.add(ex1);

        ex1.setStringAsString("b");
        ex1.setValue(2);
        in.add(ex1);
        ex1.setValue(3);
        in.add(ex1);

        ex1.setStringAsString("c");
        ex1.setValue(4);
        in.add(ex1);
        ex1.setValue(5);
        in.add(ex1);
        ex1.setValue(6);
        in.add(ex1);
        ex1.setValue(7);
        in.add(ex1);

        assertThat(tester.runFlow(new FoldFlowSimple(in.flow(), out.flow())), is(true));
        List<Ex1> results = out.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(results.size(), is(3));
        assertThat(results.get(0).getStringOption().has("a"), is(true));
        assertThat(results.get(1).getStringOption().has("b"), is(true));
        assertThat(results.get(2).getStringOption().has("c"), is(true));

        assertThat(results.get(0).getValue(), is(1));
        assertThat(results.get(1).getValue(), is(5));
        assertThat(results.get(2).getValue(), is(22));
    }

    /**
     * parameterized.
     * @throws Exception if exception was occurred
     */
    @Test
    public void withParameter() throws Exception {
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();

        ex1.setStringAsString("a");
        ex1.setValue(1);
        in.add(ex1);

        ex1.setStringAsString("b");
        ex1.setValue(2);
        in.add(ex1);
        ex1.setValue(3);
        in.add(ex1);

        ex1.setStringAsString("c");
        ex1.setValue(4);
        in.add(ex1);
        ex1.setValue(5);
        in.add(ex1);
        ex1.setValue(6);
        in.add(ex1);
        ex1.setValue(7);
        in.add(ex1);

        assertThat(tester.runFlow(new FoldFlowWithParameter(in.flow(), out.flow())), is(true));
        List<Ex1> results = out.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(results.size(), is(3));
        assertThat(results.get(0).getStringOption().has("a"), is(true));
        assertThat(results.get(1).getStringOption().has("b"), is(true));
        assertThat(results.get(2).getStringOption().has("c"), is(true));

        assertThat(results.get(0).getValue(), is(1));
        assertThat(results.get(1).getValue(), is(15));
        assertThat(results.get(2).getValue(), is(52));
    }

    /**
     * w/ combiner.
     * @throws Exception if exception was occurred
     */
    @Test
    public void combine() throws Exception {
        tester.options().setEnableCombiner(true);

        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();

        ex1.setStringAsString("a");
        ex1.setValue(1);
        in.add(ex1);

        ex1.setStringAsString("b");
        ex1.setValue(2);
        in.add(ex1);
        ex1.setValue(3);
        in.add(ex1);

        ex1.setStringAsString("c");
        ex1.setValue(4);
        in.add(ex1);
        ex1.setValue(5);
        in.add(ex1);
        ex1.setValue(6);
        in.add(ex1);
        ex1.setValue(7);
        in.add(ex1);

        assertThat(tester.runFlow(new FoldFlowSimple(in.flow(), out.flow())), is(true));
        List<Ex1> results = out.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(results.size(), is(3));
        assertThat(results.get(0).getStringOption().has("a"), is(true));
        assertThat(results.get(1).getStringOption().has("b"), is(true));
        assertThat(results.get(2).getStringOption().has("c"), is(true));

        assertThat(results.get(0).getValue(), is(1));
        assertThat(results.get(1).getValue(), is(5));
        assertThat(results.get(2).getValue(), is(22));
    }
}
