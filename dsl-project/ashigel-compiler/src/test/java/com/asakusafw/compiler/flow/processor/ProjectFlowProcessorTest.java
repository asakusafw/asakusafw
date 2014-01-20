/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.processor.flow.ProjectFlowInvalid;
import com.asakusafw.compiler.flow.processor.flow.ProjectFlowSame;
import com.asakusafw.compiler.flow.processor.flow.ProjectFlowSimple;
import com.asakusafw.compiler.flow.stage.StageCompiler;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.Part1;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link ProjectFlowProcessor}.
 */
public class ProjectFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * Ex1 to Part.
     */
    @Test
    public void Ex1_Part() {
        List<StageModel> stages = compile(ProjectFlowSimple.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Part1> result = mapper.create("out");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 in = new Ex1();
        in.setSid(10L);
        in.setValue(100);
        in.setStringAsString("Hello, world!");
        f.add(in);

        assertThat(result.getResults().size(), is(1));
        Part1 out = result.getResults().get(0);
        assertThat(out.getSid(), is(10L));
        assertThat(out.getValue(), is(100));
    }

    /**
     * Ex1 to Ex2.
     */
    @Test
    public void Ex1_Ex2() {
        List<StageModel> stages = compile(ProjectFlowSame.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex2> result = mapper.create("out");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 in = new Ex1();
        in.setSid(10L);
        in.setValue(100);
        in.setStringAsString("Hello, world!");
        f.add(in);

        assertThat(result.getResults().size(), is(1));
        Ex2 out = result.getResults().get(0);
        assertThat(out.getSid(), is(10L));
        assertThat(out.getValue(), is(100));
        assertThat(out.getStringAsString(), is("Hello, world!"));
    }

    /**
     * Part to Ex1 (invalid).
     */
    @Test
    public void Part_Ex1() {
        StageGraph graph = jfToStageGraph(ProjectFlowInvalid.class);
        try {
            new StageCompiler(environment).compile(graph);
            assertThat(environment.hasError(), is(true));
        } catch (IOException e) {
            // ok
        }
    }
}
