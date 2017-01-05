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

import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.CoGroupStage;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link ShuffleKeyEmitter}.
 */
public class ShuffleKeyEmitterTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if error was occurred while output
     */
    @Test
    public void simple() throws Exception {
        ShuffleModel analyzed = shuffle(CoGroupStage.class);
        ShuffleKeyEmitter emitter = new ShuffleKeyEmitter(environment);
        Name name = emitter.emit(analyzed);

        ClassLoader loader = start();
        SegmentedWritable key = (SegmentedWritable) create(loader, name);

        List<Segment> segments = analyzed.getSegments();
        assertThat(segments.size(), is(2));

        Segment seg1 = segments.get(0);
        Segment seg2 = segments.get(1);
        assertThat(seg1.getTerms().size(), is(2));
        assertThat(seg2.getTerms().size(), is(2));

        Ex1 ex1 = new Ex1();
        ex1.setSid(1);
        ex1.setValue(100);
        ex1.setStringAsString("ex1");

        setShuffleKey(seg1, key, ex1);

        assertThat(key.getSegmentId(), is(seg1.getPortId()));
        Object k1value = getKeyGroupField(seg1, "value", key);
        assertThat(k1value, is((Object) ex1.getValueOption()));
        Object k1sid = getKeySortField(seg1, "sid", key);
        assertThat(k1sid, is((Object) ex1.getSidOption()));

        Ex2 ex2 = new Ex2();
        ex2.setSid(2);
        ex2.setValue(200);
        ex2.setStringAsString("ex2");
        setShuffleKey(seg2, key, ex2);
        assertThat(key.getSegmentId(), is(seg2.getPortId()));
        Object k2value = getKeyGroupField(seg2, "value", key);
        assertThat(k2value, is((Object) ex2.getValueOption()));
        Object k2string = getKeySortField(seg2, "string", key);
        assertThat(k2string, is((Object) ex2.getStringOption()));
    }

    private Object getKeyGroupField(
            Segment segment,
            String propertyName,
            SegmentedWritable key) {
        Term term = segment.findTerm(propertyName);
        assertThat(propertyName, term, not(nullValue()));
        String fieldName = Naming.getShuffleKeyGroupProperty(
                segment.getElementId(),
                term.getTermId());
        return access(key, fieldName);
    }

    private Object getKeySortField(
            Segment segment,
            String propertyName,
            SegmentedWritable key) {
        Term term = segment.findTerm(propertyName);
        assertThat(propertyName, term, not(nullValue()));
        String fieldName = Naming.getShuffleKeySortProperty(
                segment.getPortId(),
                term.getTermId());
        return access(key, fieldName);
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
}
