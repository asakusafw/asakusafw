/**
 * Copyright 2011 Asakusa Framework Team.
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
import com.asakusafw.compiler.flow.processor.flow.SummarizeFlowTrivial;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.runtime.testing.MockResult;
import com.ashigeru.lang.java.model.syntax.Name;

/**
 * Test for {@link SummarizeFlowProcessor}.
 */
public class SummarizeFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * 単純なテスト。
     */
    @Test
    public void trivial() {
        run(false);
    }

    /**
     * Combinerつきの単純なテスト。
     */
    @Test
    public void combine() {
        run(true);
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
}
