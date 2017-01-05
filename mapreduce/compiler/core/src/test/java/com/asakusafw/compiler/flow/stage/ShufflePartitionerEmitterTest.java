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

import org.apache.hadoop.mapreduce.Partitioner;
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
 * Test for {@link ShufflePartitionerEmitter}.
 */
public class ShufflePartitionerEmitterTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     * @throws Exception if error was occurred while output
     */
    @Test
    public void simple() throws Exception {
        ShuffleModel analyzed = shuffle(CoGroupStage.class);
        ShufflePartitionerEmitter emitter = new ShufflePartitionerEmitter(environment);
        Name key = emitKey(analyzed);
        Name value = emitValue(analyzed);
        Name name = emitter.emit(analyzed, key, value);

        ClassLoader loader = start();
        @SuppressWarnings("unchecked")
        Partitioner<Object, Object> part = (Partitioner<Object, Object>) create(loader, name);

        SegmentedWritable k = (SegmentedWritable) create(loader, key);
        SegmentedWritable v = (SegmentedWritable) create(loader, value);

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

        Ex2 ex2 = new Ex2();
        ex2.setSid(2);
        ex2.setValue(100);
        ex2.setStringAsString("ex2");

        int p01, p02;

        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        setShuffleKeyValue(seg2, k, v, ex2);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, is(p02));

        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        ex1.setValue(101);
        setShuffleKeyValue(seg1, k, v, ex1);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, not(p02));

        ex1.setValue(100);
        ex1.setSid(2);
        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        setShuffleKeyValue(seg2, k, v, ex2);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, is(p02));

        ex2.setStringAsString("ex3");
        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        setShuffleKeyValue(seg2, k, v, ex2);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, is(p02));

        ex1.setValue(101);
        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        setShuffleKeyValue(seg2, k, v, ex2);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, not(p02));

        ex2.setValue(101);
        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        setShuffleKeyValue(seg2, k, v, ex2);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, is(p02));

        ex2.setValue(102);
        setShuffleKeyValue(seg1, k, v, ex1);
        p01 = part.getPartition(k, v, 100);
        setShuffleKeyValue(seg2, k, v, ex2);
        p02 = part.getPartition(k, v, 100);
        assertThat(p01, not(p02));
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

    private Name emitValue(ShuffleModel model) throws IOException {
        ShuffleValueEmitter emitter = new ShuffleValueEmitter(environment);
        Name name = emitter.emit(model);
        return name;
    }
}
