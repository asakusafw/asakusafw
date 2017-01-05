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
package com.asakusafw.compiler.flow.stage;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.CoGroupStage;
import com.asakusafw.compiler.flow.example.SimpleShuffleStage;
import com.asakusafw.compiler.flow.mock.MockOutput;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link ShuffleFragmentEmitter}.
 */
public class ShuffleFragmentEmitterTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if error was occurred while output
     */
    @Test
    public void simple() throws Exception {
        ShuffleModel analyzed = shuffle(SimpleShuffleStage.class);
        ShuffleFragmentEmitter emitter = new ShuffleFragmentEmitter(environment);
        Name key = emitKey(analyzed);
        Name value = emitValue(analyzed);
        Segment segment = analyzed.getSegments().get(0);
        CompiledShuffleFragment compiled = emitter.emit(segment, key, value, analyzed.getStageBlock());

        ClassLoader loader = start();
        MockResult<? extends SegmentedWritable> keys = MockResult.create();
        MockResult<? extends SegmentedWritable> values = MockResult.create();
        @SuppressWarnings("unchecked")
        Result<Ex1> output = (Result<Ex1>) create(
                loader,
                compiled.getMapOutputType().getQualifiedName(),
                MockOutput.create(keys, values));

        Ex1 ex1 = new Ex1();
        ex1.setSid(5);
        ex1.setValue(100);
        ex1.setStringAsString("ex1");

        output.add(ex1);

        List<? extends SegmentedWritable> keyList = keys.getResults();
        List<? extends SegmentedWritable> valueList = values.getResults();

        assertThat(keyList.size(), is(1));
        assertThat(valueList.size(), is(1));

        SegmentedWritable sKey = keyList.get(0);
        SegmentedWritable sValue = valueList.get(0);

        assertThat(sKey.getSegmentId(), is(segment.getPortId()));
        assertThat(sValue.getSegmentId(), is(segment.getPortId()));

        ExSummarized mapped = (ExSummarized) getShuffleValue(segment, sValue);
        assertThat(mapped.getCount(), is(1L));
        assertThat(mapped.getValue(), is(100L));
    }

    /**
     * identity operation.
     * @throws Exception if error was occurred while output
     */
    @Test
    public void identity() throws Exception {
        ShuffleModel analyzed = shuffle(CoGroupStage.class);
        ShuffleFragmentEmitter emitter = new ShuffleFragmentEmitter(environment);
        Name key = emitKey(analyzed);
        Name value = emitValue(analyzed);
        Segment segment = analyzed.getSegments().get(0);
        CompiledShuffleFragment compiled = emitter.emit(segment, key, value, analyzed.getStageBlock());

        ClassLoader loader = start();
        MockResult<? extends SegmentedWritable> keys = MockResult.create();
        MockResult<? extends SegmentedWritable> values = MockResult.create();
        @SuppressWarnings("unchecked")
        Result<Ex1> output = (Result<Ex1>) create(
                loader,
                compiled.getMapOutputType().getQualifiedName(),
                MockOutput.create(keys, values));

        Ex1 ex1 = new Ex1();
        ex1.setSid(5);
        ex1.setValue(100);
        ex1.setStringAsString("ex1");

        output.add(ex1);

        List<? extends SegmentedWritable> keyList = keys.getResults();
        List<? extends SegmentedWritable> valueList = values.getResults();

        assertThat(keyList.size(), is(1));
        assertThat(valueList.size(), is(1));

        SegmentedWritable sKey = keyList.get(0);
        SegmentedWritable sValue = valueList.get(0);

        assertThat(sKey.getSegmentId(), is(segment.getPortId()));
        assertThat(sValue.getSegmentId(), is(segment.getPortId()));

        Object shuffled = getShuffleValue(segment, sValue);
        assertThat(shuffled, is((Object) ex1));
    }

    private ShuffleModel shuffle(Class<? extends FlowDescription> aClass) {
        StageGraph graph = jfToStageGraph(aClass);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);
        ShuffleAnalyzer analyzer = new ShuffleAnalyzer(environment);
        ShuffleModel analyzed = analyzer.analyze(target);
        assertThat(environment.hasError(), is(false));
        return analyzed;
    }

    private Name emitKey(ShuffleModel model) throws IOException {
        ShuffleKeyEmitter emitter = new ShuffleKeyEmitter(environment);
        Name name = emitter.emit(model);
        return name;
    }

    private Name emitValue(ShuffleModel model) throws IOException {
        ShuffleValueEmitter emitter = new ShuffleValueEmitter(environment);
        Name name = emitter.emit(model);
        return name;
    }
}
