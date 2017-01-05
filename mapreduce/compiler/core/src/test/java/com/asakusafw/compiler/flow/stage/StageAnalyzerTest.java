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

import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.MultipleUpdateStage;
import com.asakusafw.compiler.flow.example.NoShuffleStage;
import com.asakusafw.compiler.flow.example.SimpleShuffleStage;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.testing.operator.ExOperator;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Test for {@link StageAnalyzer}.
 */
public class StageAnalyzerTest extends JobflowCompilerTestRoot {

    /**
     * map only stage.
     */
    @Test
    public void mapOnly() {
        StageGraph graph = jfToStageGraph(NoShuffleStage.class);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);

        StageAnalyzer analyzer = new StageAnalyzer(environment);
        StageModel analyzed = analyzer.analyze(target, null);

        assertThat(analyzed.getMapUnits().size(), is(1));
        assertThat(analyzed.getReduceUnits().size(), is(0));

        MapUnit map = analyzed.getMapUnits().get(0);
        assertThat(map.getFragments().size(), is(1));

        Fragment fragment = map.getFragments().get(0);
        assertThat(fragment.isRendezvous(), is(false));
        assertThat(fragment.getInputPorts().size(), is(1));
        assertThat(fragment.getOutputPorts().size(), is(1));
        assertThat(fragment.getFactors().size(), is(1));

        Factor factor = fragment.getFactors().get(0);
        assertThat(factor.getElement().getDescription(), instanceOf(OperatorDescription.class));
        OperatorDescription op = (OperatorDescription) factor.getElement().getDescription();
        assertThat(op.getDeclaration().getDeclaring(), is((Object) ExOperator.class));
    }

    /**
     * w/ reduce block.
     */
    @Test
    public void withReduce() {
        StageGraph graph = jfToStageGraph(SimpleShuffleStage.class);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);

        StageAnalyzer analyzer = new StageAnalyzer(environment);
        StageModel analyzed = analyzer.analyze(target, null);

        assertThat(analyzed.getMapUnits().size(), is(1));
        assertThat(analyzed.getReduceUnits().size(), is(1));

        ReduceUnit reduce = analyzed.getReduceUnits().get(0);
        assertThat(reduce.getInputs().size(), is(1));

        Fragment fragment = reduce.getFragments().get(0);
        assertThat(fragment.isRendezvous(), is(true));
        assertThat(fragment.getInputPorts().size(), is(1));
        assertThat(fragment.getOutputPorts().size(), is(1));
        assertThat(fragment.getFactors().size(), is(1));

        Factor factor = fragment.getFactors().get(0);
        assertThat(factor.getElement().getDescription(), instanceOf(OperatorDescription.class));
        OperatorDescription op = (OperatorDescription) factor.getElement().getDescription();
        assertThat(op.getDeclaration().getDeclaring(), is((Object) ExOperator.class));
    }

    /**
     * fragment contains multiple factors.
     */
    @Test
    public void sequencialFactors() {
        StageGraph graph = jfToStageGraph(MultipleUpdateStage.class);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);

        StageAnalyzer analyzer = new StageAnalyzer(environment);
        StageModel analyzed = analyzer.analyze(target, null);

        assertThat(analyzed.getMapUnits().size(), is(1));
        assertThat(analyzed.getReduceUnits().size(), is(0));

        MapUnit map = analyzed.getMapUnits().get(0);
        assertThat(map.getFragments().size(), is(1));

        Fragment fragment = map.getFragments().get(0);
        assertThat(fragment.isRendezvous(), is(false));
        assertThat(fragment.getInputPorts().size(), is(1));
        assertThat(fragment.getOutputPorts().size(), is(1));
        assertThat(fragment.getFactors().size(), is(3));
    }
}
