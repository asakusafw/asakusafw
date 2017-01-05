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

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.NoShuffleStage;
import com.asakusafw.compiler.flow.example.SimpleShuffleStage;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Arrangement;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link ShuffleAnalyzer}.
 */
public class ShuffleAnalyzerTest extends JobflowCompilerTestRoot {

    /**
     * w/o shuffle operations.
     */
    @Test
    public void nothing() {
        StageGraph graph = jfToStageGraph(NoShuffleStage.class);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);

        ShuffleAnalyzer analyzer = new ShuffleAnalyzer(environment);
        ShuffleModel analyzed = analyzer.analyze(target);
        assertThat(analyzed, is(nullValue()));
    }

    /**
     * w/ shuffle operations.
     */
    @Test
    public void simple() {
        StageGraph graph = jfToStageGraph(SimpleShuffleStage.class);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);

        ShuffleAnalyzer analyzer = new ShuffleAnalyzer(environment);
        ShuffleModel analyzed = analyzer.analyze(target);
        assertThat(analyzed, not(nullValue()));

        List<Segment> segments = analyzed.getSegments();
        assertThat(segments.size(), is(1));
        Segment segment = segments.get(0);

        assertThat(segment.getTerms().size(), is(1));

        Term grouping = segment.getTerms().get(0);
        assertThat(grouping.getArrangement(), is(Arrangement.GROUPING));
        assertThat(grouping.getSource().getName(), is("string"));
        assertThat(grouping.getSource().getType(), equalTo((Type) StringOption.class));
    }
}
