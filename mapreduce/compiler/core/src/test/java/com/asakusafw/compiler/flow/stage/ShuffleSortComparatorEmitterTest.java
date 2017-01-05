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

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.CoGroupStage;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link ShuffleSortComparatorEmitter}.
 */
public class ShuffleSortComparatorEmitterTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if error was occurred while output
     */
    @Test
    public void simple() throws Exception {
        ShuffleModel analyzed = shuffle(CoGroupStage.class);
        ShuffleSortComparatorEmitter emitter = new ShuffleSortComparatorEmitter(environment);
        Name key = emitKey(analyzed);
        Name name = emitter.emit(analyzed, key);

        ClassLoader loader = start();
        @SuppressWarnings("unchecked")
        RawComparator<Writable> cmp = (RawComparator<Writable>) create(loader, name);

        SegmentedWritable k1 = (SegmentedWritable) create(loader, key);
        SegmentedWritable k2 = (SegmentedWritable) create(loader, key);

        List<Segment> segments = analyzed.getSegments();
        assertThat(segments.size(), is(2));

        Segment seg1 = segments.get(0);
        Segment seg2 = segments.get(1);
        assertThat(seg1.getTerms().size(), is(2));
        assertThat(seg2.getTerms().size(), is(2));

        Ex1 ex1 = new Ex1();
        ex1.setSid(10);
        ex1.setValue(100);
        ex1.setStringAsString("ex1");

        setShuffleKey(seg1, k1, ex1);
        ex1.setStringAsString("ex2");
        setShuffleKey(seg1, k2, ex1);
        assertThat(cmp.compare(k1, k2), is(0));
        assertThat(cmp.compare(k2, k1), is(0));

        setShuffleKey(seg1, k1, ex1);
        ex1.setSid(9);
        setShuffleKey(seg1, k2, ex1);
        assertThat(cmp.compare(k1, k2), greaterThan(0));
        assertThat(cmp.compare(k2, k1), lessThan(0));

        setShuffleKey(seg1, k1, ex1);
        ex1.setSid(Integer.MIN_VALUE);
        setShuffleKey(seg1, k2, ex1);
        assertThat(cmp.compare(k1, k2), greaterThan(0));
        assertThat(cmp.compare(k2, k1), lessThan(0));

        Ex2 ex2 = new Ex2();
        ex2.setSid(2);
        ex2.setValue(100);
        ex2.setStringAsString("ex2");
        setShuffleKey(seg2, k1, ex2);
        ex2.setSid(3);
        setShuffleKey(seg2, k2, ex2);
        assertThat(cmp.compare(k1, k2), is(0));
        assertThat(cmp.compare(k2, k1), is(0));

        setShuffleKey(seg2, k1, ex2);
        ex2.setStringAsString("ex3");
        setShuffleKey(seg2, k2, ex2);
        assertThat(cmp.compare(k1, k2), greaterThan(0));
        assertThat(cmp.compare(k2, k1), lessThan(0));

        setShuffleKey(seg2, k1, ex2);
        ex2.setStringAsString("");
        setShuffleKey(seg2, k2, ex2);
        assertThat(cmp.compare(k1, k2), lessThan(0));
        assertThat(cmp.compare(k2, k1), greaterThan(0));

        setShuffleKey(seg2, k1, ex2);
        ex2.setString(null);
        setShuffleKey(seg2, k2, ex2);
        assertThat(cmp.compare(k1, k2), lessThan(0));
        assertThat(cmp.compare(k2, k1), greaterThan(0));

        setShuffleKey(seg1, k1, ex1);
        setShuffleKey(seg2, k2, ex2);
        assertThat(cmp.compare(k1, k2), lessThan(0));
        assertThat(cmp.compare(k2, k1), greaterThan(0));
    }

    private ShuffleModel shuffle(Class<? extends FlowDescription> aClass) {
        StageGraph graph = jfToStageGraph(aClass);
        assertThat(graph.getStages().size(), is(1));
        StageBlock target = graph.getStages().get(0);
        ShuffleAnalyzer analyzer = new ShuffleAnalyzer(environment);
        ShuffleModel analyzed = analyzer.analyze(target);
        return analyzed;
    }

    private Name emitKey(ShuffleModel model) throws IOException {
        ShuffleKeyEmitter emitter = new ShuffleKeyEmitter(environment);
        Name name = emitter.emit(model);
        return name;
    }
}
