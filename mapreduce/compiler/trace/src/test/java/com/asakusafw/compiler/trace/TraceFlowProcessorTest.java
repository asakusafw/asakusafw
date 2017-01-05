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
package com.asakusafw.compiler.trace;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asakusafw.compiler.trace.testing.OuterFlowpart;
import com.asakusafw.compiler.trace.testing.SimpleFlowpart;
import com.asakusafw.compiler.trace.testing.SimpleOperator;
import com.asakusafw.compiler.trace.testing.dmdl.model.Model;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.trace.io.TraceSettingSerializer;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.trace.model.TraceSetting.Mode;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;

/**
 * Test for {@link TraceFlowProcessor}.
 */
@RunWith(Parameterized.class)
public class TraceFlowProcessorTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * A test helper.
     */
    @Rule
    public final CompilerTester tester = new CompilerTester();

    private final Mode mode;

    /**
     * Creates a new instance.
     * @param mode trace mode
     */
    public TraceFlowProcessorTest(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns test parameter sets.
     * @return test parameter sets
     */
    @Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { Mode.OUT_OF_ORDER },
                { Mode.STRICT },
        });
    }

    /**
     * Trace disabled.
     * @throws Exception if failed
     */
    @Test
    public void normal() throws Exception {
        TestInput<Model> in = tester.input(Model.class, "in");
        prepare(in);

        TestOutput<Model> out = tester.output(Model.class, "out");
        assertThat(tester.runFlow(new SimpleFlowpart(in.flow(), out.flow())), is(true));
        verify(out);
    }

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple_in() throws Exception {
        trace(new TraceSetting[] {
                new TraceSetting(
                        new Tracepoint(SimpleOperator.class.getName(), "line", PortKind.INPUT, "model"),
                        mode, attr()),
        });
        TestInput<Model> in = tester.input(Model.class, "in");
        prepare(in);

        TestOutput<Model> out = tester.output(Model.class, "out");
        assertThat(tester.runFlow(new SimpleFlowpart(in.flow(), out.flow())), is(true));
        verify(out);
    }

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple_out() throws Exception {
        trace(new TraceSetting[] {
                new TraceSetting(
                        new Tracepoint(SimpleOperator.class.getName(), "line", PortKind.OUTPUT, "out"),
                        mode, attr()),
        });
        TestInput<Model> in = tester.input(Model.class, "in");
        prepare(in);

        TestOutput<Model> out = tester.output(Model.class, "out");
        assertThat(tester.runFlow(new SimpleFlowpart(in.flow(), out.flow())), is(true));
        verify(out);
    }

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple_both() throws Exception {
        trace(new TraceSetting[] {
                new TraceSetting(
                        new Tracepoint(SimpleOperator.class.getName(), "line", PortKind.INPUT, "model"),
                        mode, attr()),
                new TraceSetting(
                        new Tracepoint(SimpleOperator.class.getName(), "line", PortKind.OUTPUT, "out"),
                        mode, attr()),
        });
        TestInput<Model> in = tester.input(Model.class, "in");
        prepare(in);

        TestOutput<Model> out = tester.output(Model.class, "out");
        assertThat(tester.runFlow(new SimpleFlowpart(in.flow(), out.flow())), is(true));
        verify(out);
    }

    /**
     * Weaves around inner flowpart.
     * @throws Exception if failed
     */
    @Test
    public void around() throws Exception {
        trace(new TraceSetting[] {
                new TraceSetting(
                        new Tracepoint(SimpleFlowpart.class.getName(), "create", PortKind.INPUT, "in"),
                        mode, attr()),
                new TraceSetting(
                        new Tracepoint(SimpleFlowpart.class.getName(), "create", PortKind.OUTPUT, "out"),
                        mode, attr()),
        });
        TestInput<Model> in = tester.input(Model.class, "in");
        prepare(in);

        TestOutput<Model> out = tester.output(Model.class, "out");
        assertThat(tester.runFlow(new OuterFlowpart(in.flow(), out.flow())), is(true));
        verify(out);
    }

    /**
     * Weaves into inner flowpart.
     * @throws Exception if failed
     */
    @Test
    public void inner() throws Exception {
        trace(new TraceSetting[] {
                new TraceSetting(
                        new Tracepoint(SimpleOperator.class.getName(), "line", PortKind.INPUT, "model"),
                        mode, attr()),
                new TraceSetting(
                        new Tracepoint(SimpleOperator.class.getName(), "line", PortKind.OUTPUT, "out"),
                        mode, attr()),
        });
        TestInput<Model> in = tester.input(Model.class, "in");
        prepare(in);

        TestOutput<Model> out = tester.output(Model.class, "out");
        assertThat(tester.runFlow(new OuterFlowpart(in.flow(), out.flow())), is(true));
        verify(out);
    }

    private void trace(TraceSetting... settings) {
        String value = TraceSettingSerializer.serialize(Arrays.asList(settings));
        tester.options().putExtraAttribute(TracepointWeaveRewriter.KEY_COMPILER_OPTION, value);
    }

    private Map<String, String> attr() {
        return Collections.emptyMap();
    }

    private void prepare(TestInput<Model> in) throws IOException {
        prepare(in, "Hello, world!");
    }

    private void verify(TestOutput<Model> out) throws IOException {
        verify(out, "Hello, world!");
    }

    private void prepare(TestInput<Model> in, String value) throws IOException {
        Model model = new Model();
        model.setValueAsString(value);
        in.add(model);
    }

    private void verify(TestOutput<Model> out, String value) throws IOException {
        List<Model> result = out.toList();
        assertThat(result, hasSize(greaterThan(0)));
        assertThat(result.get(0).getValueAsString(), is(value));
    }
}
