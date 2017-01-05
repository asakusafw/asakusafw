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
import com.asakusafw.compiler.flow.processor.flow.ConvertFlowSimple;
import com.asakusafw.compiler.flow.processor.flow.ConvertFlowWithParameter;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link ConvertFlowProcessor}.
 */
public class ConvertFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        List<StageModel> stages = compile(ConvertFlowSimple.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex2> out = mapper.create("out");
        MockResult<Ex1> orig = mapper.create("original");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(100);
        f.add(ex1);

        assertThat(out.getResults().size(), is(1));
        assertThat(out.getResults().get(0).getValue(), is(101));

        assertThat(orig.getResults().size(), is(1));
        assertThat(orig.getResults().get(0), is(ex1));
    }

    /**
     * parameterized.
     */
    @Test
    public void withParameter() {
        List<StageModel> stages = compile(ConvertFlowWithParameter.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex2> out = mapper.create("out");
        MockResult<Ex1> orig = mapper.create("original");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(100);
        f.add(ex1);

        assertThat(out.getResults().size(), is(1));
        assertThat(out.getResults().get(0).getValue(), is(110));

        assertThat(orig.getResults().size(), is(1));
        assertThat(orig.getResults().get(0), is(ex1));
    }
}
