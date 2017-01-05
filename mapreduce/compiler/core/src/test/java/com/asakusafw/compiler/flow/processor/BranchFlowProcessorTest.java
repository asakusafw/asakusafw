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
package com.asakusafw.compiler.flow.processor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.processor.flow.BranchFlowSimple;
import com.asakusafw.compiler.flow.processor.flow.BranchFlowWithParameter;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link BranchFlowProcessor}.
 */
public class BranchFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        List<StageModel> stages = compile(BranchFlowSimple.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> high = mapper.create("high");
        MockResult<Ex1> low = mapper.create("low");
        MockResult<Ex1> stop = mapper.create("stop");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(-100);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(1));
        assertThat(stop.getResults().get(0), is(ex1));
        stop.getResults().clear();

        ex1.setValue(0);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(1));
        assertThat(stop.getResults().get(0), is(ex1));
        stop.getResults().clear();

        ex1.setValue(1);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(1));
        assertThat(stop.getResults().size(), is(0));
        assertThat(low.getResults().get(0), is(ex1));
        low.getResults().clear();

        ex1.setValue(50);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(1));
        assertThat(stop.getResults().size(), is(0));
        assertThat(low.getResults().get(0), is(ex1));
        low.getResults().clear();

        ex1.setValue(100);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(1));
        assertThat(stop.getResults().size(), is(0));
        assertThat(low.getResults().get(0), is(ex1));
        low.getResults().clear();

        ex1.setValue(101);
        f.add(ex1);
        assertThat(high.getResults().size(), is(1));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(0));
        assertThat(high.getResults().get(0), is(ex1));
        high.getResults().clear();

        ex1.setValue(150);
        f.add(ex1);
        assertThat(high.getResults().size(), is(1));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(0));
        assertThat(high.getResults().get(0), is(ex1));
        high.getResults().clear();
    }

    /**
     * parameterized.
     */
    @Test
    public void withParameter() {
        List<StageModel> stages = compile(BranchFlowWithParameter.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> high = mapper.create("high");
        MockResult<Ex1> low = mapper.create("low");
        MockResult<Ex1> stop = mapper.create("stop");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(-100);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(1));
        assertThat(stop.getResults().get(0), is(ex1));
        stop.getResults().clear();

        ex1.setValue(0);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(1));
        assertThat(stop.getResults().get(0), is(ex1));
        stop.getResults().clear();

        ex1.setValue(1);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(1));
        assertThat(stop.getResults().size(), is(0));
        assertThat(low.getResults().get(0), is(ex1));
        low.getResults().clear();

        ex1.setValue(25);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(1));
        assertThat(stop.getResults().size(), is(0));
        assertThat(low.getResults().get(0), is(ex1));
        low.getResults().clear();

        ex1.setValue(50);
        f.add(ex1);
        assertThat(high.getResults().size(), is(0));
        assertThat(low.getResults().size(), is(1));
        assertThat(stop.getResults().size(), is(0));
        assertThat(low.getResults().get(0), is(ex1));
        low.getResults().clear();

        ex1.setValue(51);
        f.add(ex1);
        assertThat(high.getResults().size(), is(1));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(0));
        assertThat(high.getResults().get(0), is(ex1));
        high.getResults().clear();

        ex1.setValue(100);
        f.add(ex1);
        assertThat(high.getResults().size(), is(1));
        assertThat(low.getResults().size(), is(0));
        assertThat(stop.getResults().size(), is(0));
        assertThat(high.getResults().get(0), is(ex1));
        high.getResults().clear();
    }
}
