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
import com.asakusafw.compiler.flow.processor.flow.GroupSortFlowMax;
import com.asakusafw.compiler.flow.processor.flow.GroupSortFlowMin;
import com.asakusafw.compiler.flow.processor.flow.GroupSortFlowWithParameter;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.operator.GroupSort;

/**
 * Test for {@link GroupSort}.
 */
public class GroupSortFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * max.
     */
    @Test
    public void max() {
        List<StageModel> stages = compile(GroupSortFlowMax.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> result = mapper.add("r1", new Ex1Copier());

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        f.begin();

        ex1.setValue(300);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(200);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(200);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(100);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        f.end();

        assertThat(result.getResults().size(), is(1));
        assertThat(result.getResults().get(0).getValue(), is(300));
    }

    /**
     * min.
     */
    @Test
    public void min() {
        List<StageModel> stages = compile(GroupSortFlowMin.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> result = mapper.add("r1", new Ex1Copier());

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        f.begin();

        ex1.setValue(100);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(200);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(200);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(300);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        f.end();

        assertThat(result.getResults().size(), is(1));
        assertThat(result.getResults().get(0).getValue(), is(100));
    }

    /**
     * parameterized.
     */
    @Test
    public void withParameter() {
        List<StageModel> stages = compile(GroupSortFlowWithParameter.class);
        StageModel stage = stages.get(0);
        Assume.assumeThat(stage.getReduceUnits().size(), is(1));
        ReduceUnit reduce = stage.getReduceUnits().get(0);
        Fragment fragment = reduce.getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> r1 = mapper.add("r1", new Ex1Copier());
        MockResult<Ex1> r2 = mapper.add("r2", new Ex1Copier());

        @SuppressWarnings("unchecked")
        Rendezvous<Writable> f = (Rendezvous<Writable>) create(loader, name, mapper.toArguments());

        Segment segment = stage.getShuffleModel().findSegment(fragment.getInputPorts().get(0));
        SegmentedWritable value = createShuffleValue(loader, stage);

        Ex1 ex1 = new Ex1();
        f.begin();

        ex1.setValue(50);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(100);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(101);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        ex1.setValue(150);
        setShuffleValue(segment, value, ex1);
        f.process(value);

        f.end();

        assertThat(r1.getResults().size(), is(2));
        assertThat(r2.getResults().size(), is(2));
        assertThat(r1.getResults().get(0).getValue(), is(50));
        assertThat(r1.getResults().get(1).getValue(), is(100));
        assertThat(r2.getResults().get(0).getValue(), is(101));
        assertThat(r2.getResults().get(1).getValue(), is(150));
    }

    static class Ex1Copier extends MockResult<Ex1> {
        @Override
        protected Ex1 bless(Ex1 result) {
            Ex1 copy = new Ex1();
            copy.copyFrom(result);
            return copy;
        }
    }
}
