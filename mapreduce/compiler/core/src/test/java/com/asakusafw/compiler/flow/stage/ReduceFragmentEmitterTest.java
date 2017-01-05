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

import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.SimpleShuffleStage;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link ReduceFragmentEmitter}.
 */
public class ReduceFragmentEmitterTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if error was occurred while output
     */
    @SuppressWarnings("deprecation")
    @Test
    public void simple() throws Exception {
        StageModel analyzed = mr(SimpleShuffleStage.class);
        ReduceUnit red = analyzed.getReduceUnits().get(0);
        assertThat(red.getFragments().size(), is(1));
        Fragment fragment = red.getFragments().get(0);

        ReduceFragmentEmitter emitter = new ReduceFragmentEmitter(environment);
        ShuffleModel shuffle = analyzed.getShuffleModel();
        CompiledType name = emitter.emit(fragment, shuffle, analyzed.getStageBlock());

        ClassLoader loader = start();
        MockResult<ExSummarized> result = MockResult.create();
        Rendezvous<Writable> object = createRendezvous(
                loader,
                name.getQualifiedName(),
                result);
        Writable key = (Writable) create(loader, shuffle.getCompiled().getKeyTypeName());
        Writable value = (Writable) create(loader, shuffle.getCompiled().getValueTypeName());

        Ex1 orig = new Ex1();
        orig.setValueOption(new IntOption().modify(100));
        ExSummarized model = new ExSummarized();
        model.setValue(100);
        model.setCount(1);

        invoke(key, Naming.getShuffleKeySetter(1), model);
        invoke(value, Naming.getShuffleValueSetter(1), model);

        object.begin();
        object.process(value);
        object.process(value);
        object.process(value);
        object.end();

        List<ExSummarized> results = result.getResults();
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getValue(), is(300L));
        assertThat(results.get(0).getCount(), is(3L));
    }

    private StageModel mr(Class<? extends FlowDescription> aClass) throws IOException {
        StageGraph graph = jfToStageGraph(aClass);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);
        StageAnalyzer analyzer = new StageAnalyzer(environment);
        StageModel analyzed = analyzer.analyze(target, compileShuffle(target));
        return analyzed;
    }
}
