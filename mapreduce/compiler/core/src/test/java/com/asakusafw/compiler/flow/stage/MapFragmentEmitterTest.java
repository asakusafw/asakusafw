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
import com.asakusafw.compiler.flow.example.NoShuffleStage;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link MapFragmentEmitter}.
 */
public class MapFragmentEmitterTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if error was occurred while output
     */
    @Test
    public void simple() throws Exception {
        StageModel analyzed = mr(NoShuffleStage.class);
        MapUnit map = analyzed.getMapUnits().get(0);
        assertThat(map.getFragments().size(), is(1));
        Fragment fragment = map.getFragments().get(0);

        MapFragmentEmitter emitter = new MapFragmentEmitter(environment);
        CompiledType name = emitter.emit(fragment, analyzed.getStageBlock());

        ClassLoader loader = start();
        MockResult<Ex1> result = MockResult.create();
        Result<Ex1> object = createResult(
                loader,
                name.getQualifiedName(),
                result);

        object.add(new Ex1());

        List<Ex1> results = result.getResults();
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getValueOption().get(), is(100));
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
