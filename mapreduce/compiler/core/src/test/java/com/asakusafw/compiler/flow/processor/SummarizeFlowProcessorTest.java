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

import org.apache.hadoop.io.Writable;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.processor.flow.SummarizeFlowKeyConflict;
import com.asakusafw.compiler.flow.processor.flow.SummarizeFlowRenameKey;
import com.asakusafw.compiler.flow.processor.flow.SummarizeFlowTrivial;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.compiler.flow.testing.model.KeyConflict;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link SummarizeFlowProcessor}.
 */
public class SummarizeFlowProcessorTest extends JobflowCompilerTestRoot {

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
     */
    @Test
    public void trivial() {
        run(false);
    }

    /**
     * w/ combiner.
     */
    @Test
    public void combine() {
        run(true);
    }

    /**
     * simple case.
     * @throws Exception if test was failed
     */
    @Test
    public void renameKey() throws Exception {
        runRenameKey(false);
    }

    /**
     * w/ combiner that modifying grouping keys.
     * @throws Exception if test was failed
     */
    @Test
    public void combineRenameKey() throws Exception {
        runRenameKey(true);
    }

    /**
     * Test for grouping key is conflict.
     * @throws Exception if test was failed
     */
    @Test
    public void conflictKey() throws Exception {
        runKeyConflict(false);
    }

    /**
     * Test for grouping key is conflict with combiner.
     * @throws Exception if test was failed
     */
    @Test
    public void combineConflictKey() throws Exception {
        runKeyConflict(true);
    }

    private void run(boolean combine) {
        environment.getOptions().setEnableCombiner(combine);
        List<StageModel> stages = compile(SummarizeFlowTrivial.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<ExSummarized> result = mapper.create("out");

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        ExSummarized ex1 = new ExSummarized();
        ex1.setCount(1);
        f.begin();

        ex1.setValue(10);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(20);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(30);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(40);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        f.end();

        assertThat(result.getResults().size(), is(1));
        assertThat(result.getResults().get(0).getValue(), is(100L));
        assertThat(result.getResults().get(0).getCount(), is(4L));
    }

    private void runRenameKey(boolean combine) throws Exception {
        environment.getOptions().setEnableCombiner(combine);

        TestInput<Ex1> in = tester.input(Ex1.class, "Ex1");
        TestOutput<ExSummarized2> summarized = tester.output(ExSummarized2.class, "summarized");

        Ex1 ex1 = new Ex1();

        ex1.setStringAsString("a");
        ex1.setSid(1);
        ex1.setValue(10);
        in.add(ex1);

        ex1.setSid(2);
        ex1.setValue(20);
        in.add(ex1);

        ex1.setStringAsString("b");
        ex1.setSid(3);
        ex1.setValue(30);
        in.add(ex1);

        ex1.setSid(4);
        ex1.setValue(40);
        in.add(ex1);

        ex1.setSid(5);
        ex1.setValue(50);
        in.add(ex1);

        assertThat(tester.runFlow(new SummarizeFlowRenameKey(
                in.flow(), summarized.flow())), is(true));

        List<ExSummarized2> results = summarized.toList((o1, o2) -> o1.getKeyOption().compareTo(o2.getKeyOption()));

        assertThat(results.size(), is(2));
        assertThat(results.get(0).getKeyAsString(), is("a"));
        assertThat(results.get(0).getCount(), is(2L));
        assertThat(results.get(0).getValue(), is(30L));

        assertThat(results.get(1).getKeyAsString(), is("b"));
        assertThat(results.get(1).getCount(), is(3L));
        assertThat(results.get(1).getValue(), is(120L));
    }

    private void runKeyConflict(boolean combine) throws Exception {
        environment.getOptions().setEnableCombiner(combine);

        TestInput<Ex1> in = tester.input(Ex1.class, "Ex1");
        TestOutput<KeyConflict> summarized = tester.output(KeyConflict.class, "summarized");

        Ex1 ex1 = new Ex1();

        ex1.setStringAsString("a");
        ex1.setSid(1);
        in.add(ex1);

        ex1.setSid(2);
        in.add(ex1);

        ex1.setStringAsString("b");
        ex1.setSid(3);
        in.add(ex1);

        ex1.setSid(4);
        in.add(ex1);

        ex1.setSid(5);
        in.add(ex1);

        assertThat(tester.runFlow(new SummarizeFlowKeyConflict(
                in.flow(), summarized.flow())), is(true));

        List<KeyConflict> results = summarized.toList((o1, o2) -> o1.getKeyOption().compareTo(o2.getKeyOption()));

        assertThat(results.size(), is(2));
        assertThat(results.get(0).getKeyAsString(), is("a"));
        assertThat(results.get(0).getCount(), is(2L));

        assertThat(results.get(1).getKeyAsString(), is("b"));
        assertThat(results.get(1).getCount(), is(3L));
    }
}
