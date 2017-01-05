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
package com.asakusafw.compiler.flow.jobflow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.example.NoShuffleStage;
import com.asakusafw.compiler.flow.example.SequentialMultiStage;
import com.asakusafw.compiler.flow.example.SimpleShuffleStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Delivery;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Process;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.ExSummarizedMockExporterDescription;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * Test for {@link JobflowAnalyzer}.
 */
public class JobflowAnalyzerTest extends JobflowCompilerTestRoot {

    /**
     * analyzes map only stage.
     */
    @Test
    public void mapperOnly() {
        StageGraph graph = jfToStageGraph(NoShuffleStage.class);
        List<StageModel> stages = compileStages(graph);
        JobflowAnalyzer analyzer = new JobflowAnalyzer(environment);
        JobflowModel jobflow = analyzer.analyze(graph, stages);

        assertThat(jobflow.getStages().size(), is(1));
        assertThat(jobflow.getImports().size(), is(1));
        assertThat(jobflow.getExports().size(), is(1));

        Import prologue = jobflow.getImports().get(0);
        assertThat(prologue.getDescription().getName(), is("ex1"));
        assertThat(
                prologue.getDescription().getImporterDescription().getClass(),
                is((Object) Ex1MockImporterDescription.class));

        Export epilogue = jobflow.getExports().get(0);
        assertThat(epilogue.getDescription().getName(), is("ex1"));
        assertThat(
                epilogue.getDescription().getExporterDescription().getClass(),
                is((Object) Ex1MockExporterDescription.class));

        Stage stage = jobflow.getStages().get(0);
        assertThat(stage.getProcesses().size(), is(1));
        assertThat(stage.getReduceOrNull(), is(nullValue()));
    }

    /**
     * analyzes single stage.
     */
    @Test
    public void single() {
        StageGraph graph = jfToStageGraph(SimpleShuffleStage.class);
        List<StageModel> stages = compileStages(graph);
        JobflowAnalyzer analyzer = new JobflowAnalyzer(environment);
        JobflowModel jobflow = analyzer.analyze(graph, stages);

        assertThat(jobflow.getStages().size(), is(1));
        assertThat(jobflow.getImports().size(), is(1));
        assertThat(jobflow.getExports().size(), is(1));

        Import prologue = jobflow.getImports().get(0);
        assertThat(prologue.getDescription().getName(), is("ex1"));
        assertThat(
                prologue.getDescription().getImporterDescription().getClass(),
                is((Object) Ex1MockImporterDescription.class));

        Export epilogue = jobflow.getExports().get(0);
        assertThat(epilogue.getDescription().getName(), is("exs"));
        assertThat(
                epilogue.getDescription().getExporterDescription().getClass(),
                is((Object) ExSummarizedMockExporterDescription.class));

        Stage stage = jobflow.getStages().get(0);
        assertThat(stage.getProcesses().size(), is(1));
        assertThat(stage.getReduceOrNull(), not(nullValue()));
    }

    /**
     * analyze multiple stages.
     */
    @Test
    public void multi() {
        StageGraph graph = jfToStageGraph(SequentialMultiStage.class);
        List<StageModel> stages = compileStages(graph);
        JobflowAnalyzer analyzer = new JobflowAnalyzer(environment);
        JobflowModel jobflow = analyzer.analyze(graph, stages);

        assertThat(jobflow.getStages().size(), is(2));
        assertThat(jobflow.getImports().size(), is(1));
        assertThat(jobflow.getExports().size(), is(1));

        Import prologue = jobflow.getImports().get(0);
        assertThat(prologue.getDescription().getName(), is("ex1"));
        assertThat(
                prologue.getDescription().getImporterDescription().getClass(),
                is((Object) Ex1MockImporterDescription.class));

        Export epilogue = jobflow.getExports().get(0);
        assertThat(epilogue.getDescription().getName(), is("ex1"));
        assertThat(
                epilogue.getDescription().getExporterDescription().getClass(),
                is((Object) Ex1MockExporterDescription.class));

        Graph<Stage> dep = jobflow.getDependencyGraph();
        Set<Stage> heads = Graphs.collectHeads(dep);
        Set<Stage> tails = Graphs.collectTails(dep);

        assertThat(heads.size(), is(1));
        assertThat(tails.size(), is(1));

        Stage st1 = tails.iterator().next();
        Stage st2 = heads.iterator().next();

        assertThat(st1, not(equalTo(st2)));

        List<Process> st1p = st1.getProcesses();
        assertThat(st1p.size(), is(1));
        assertThat(st1p.get(0).getResolvedSources().contains(prologue), is(true));

        List<Delivery> st2d = st2.getDeliveries();
        assertThat(st2d.size(), is(1));
        assertThat(epilogue.getResolvedSources().contains(st2d.get(0)), is(true));
    }
}
