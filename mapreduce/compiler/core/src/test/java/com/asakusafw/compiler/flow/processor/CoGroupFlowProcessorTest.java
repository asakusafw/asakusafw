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
import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.processor.flow.CoGroupFlowOp1;
import com.asakusafw.compiler.flow.processor.flow.CoGroupFlowOp2;
import com.asakusafw.compiler.flow.processor.flow.CoGroupFlowOp3;
import com.asakusafw.compiler.flow.processor.flow.CoGroupFlowSwap;
import com.asakusafw.compiler.flow.processor.flow.CoGroupFlowWithParameter;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link CoGroupFlowProcessor}.
 */
public class CoGroupFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * test for 1-group.
     */
    @Test
    public void op1() {
        List<StageModel> stages = compile(CoGroupFlowOp1.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> result = mapper.create("r1");

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("string");

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
        assertThat(result.getResults().get(0).getValue(), is(100));
    }

    /**
     * test for 2-groups.
     */
    @Test
    public void op2() {
        List<StageModel> stages = compile(CoGroupFlowOp2.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> r1 = mapper.create("r1");
        MockResult<Ex2> r2 = mapper.create("r2");

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment s1 = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        Segment s2 = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(1));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();
        ex1.setStringAsString("string");
        ex2.setStringAsString("string");

        f.begin();
        ex1.setValue(10);
        setShuffleValue(s1, value, ex1);
        f.process(value);

        ex1.setValue(20);
        setShuffleValue(s1, value, ex1);
        f.process(value);

        ex2.setValue(30);
        setShuffleValue(s2, value, ex2);
        f.process(value);

        ex2.setValue(40);
        setShuffleValue(s2, value, ex2);
        f.process(value);

        f.end();

        assertThat(r1.getResults().size(), is(1));
        assertThat(r1.getResults().get(0).getValue(), is(70));

        assertThat(r2.getResults().size(), is(1));
        assertThat(r2.getResults().get(0).getValue(), is(30));
    }

    /**
     * test for 3-groups.
     */
    @Test
    public void op3() {
        List<StageModel> stages = compile(CoGroupFlowOp3.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> r1 = mapper.create("r1");
        MockResult<Ex1> r2 = mapper.create("r2");
        MockResult<Ex1> r3 = mapper.create("r3");

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment s1 = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        Segment s2 = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(1));
        Segment s3 = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(2));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("string");

        f.begin();

        ex1.setValue(10);
        setShuffleValue(s1, value, ex1);
        f.process(value);
        ex1.setValue(20);
        setShuffleValue(s1, value, ex1);
        f.process(value);

        ex1.setValue(30);
        setShuffleValue(s2, value, ex1);
        f.process(value);
        ex1.setValue(40);
        setShuffleValue(s2, value, ex1);
        f.process(value);

        ex1.setValue(50);
        setShuffleValue(s3, value, ex1);
        f.process(value);
        ex1.setValue(60);
        setShuffleValue(s3, value, ex1);
        f.process(value);

        f.end();

        assertThat(r1.getResults().size(), is(1));
        assertThat(r1.getResults().get(0).getValue(), is(110));
        assertThat(r2.getResults().size(), is(1));
        assertThat(r2.getResults().get(0).getValue(), is(30));
        assertThat(r3.getResults().size(), is(1));
        assertThat(r3.getResults().get(0).getValue(), is(70));
    }

    /**
     * parameterized.
     */
    @Test
    public void withParameter() {
        List<StageModel> stages = compile(CoGroupFlowWithParameter.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> result = mapper.create("r1");

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("string");

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
        assertThat(result.getResults().get(0).getValue(), is(500));
    }

    /**
     * test for inputbuffer=escape.
     */
    @Test
    public void swap() {
        List<StageModel> stages = compile(CoGroupFlowSwap.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> result = mapper.create("r1");

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("string");

        f.begin();
        for (int i = 0; i < 100000; i++) {
            ex1.setValue(10);
            setShuffleValue(segment, value, ex1);
            f.process(value);
        }
        f.end();

        assertThat(result.getResults().size(), is(1));
        assertThat(result.getResults().get(0).getValue(), is(100000 * 10));
    }
}
