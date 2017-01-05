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
import com.asakusafw.compiler.flow.processor.flow.ExtractFlowOp1;
import com.asakusafw.compiler.flow.processor.flow.ExtractFlowOp2;
import com.asakusafw.compiler.flow.processor.flow.ExtractFlowOp3;
import com.asakusafw.compiler.flow.processor.flow.ExtractFlowWithParameter;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link ExtractFlowProcessor}.
 */
public class ExtractFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * () -> (in) -> (r1)
     */
    @Test
    public void op1() {
        List<StageModel> stages = compile(ExtractFlowOp1.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> out1 = mapper.create("r1");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(100);
        f.add(ex1);

        assertThat(out1.getResults().size(), is(1));
        assertThat(out1.getResults().get(0).getValue(), is(101));
    }

    /**
     * () -> (in) -> (r1, r2)
     */
    @Test
    public void op2() {
        List<StageModel> stages = compile(ExtractFlowOp2.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> out1 = mapper.create("r1");
        MockResult<Ex2> out2 = mapper.create("r2");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(100);
        f.add(ex1);

        assertThat(out1.getResults().size(), is(1));
        assertThat(out1.getResults().get(0).getValue(), is(101));

        assertThat(out2.getResults().size(), is(1));
        assertThat(out2.getResults().get(0).getValue(), is(102));
    }

    /**
     * () -> (in) -> (r1, r2, r3)
     */
    @Test
    public void op3() {
        List<StageModel> stages = compile(ExtractFlowOp3.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> out1 = mapper.create("r1");
        MockResult<Ex2> out2 = mapper.create("r2");
        MockResult<Ex1> out3 = mapper.create("r3");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(100);
        f.add(ex1);

        assertThat(out1.getResults().size(), is(1));
        assertThat(out1.getResults().get(0).getValue(), is(101));

        assertThat(out2.getResults().size(), is(1));
        assertThat(out2.getResults().get(0).getValue(), is(102));

        assertThat(out3.getResults().size(), is(1));
        assertThat(out3.getResults().get(0).getValue(), is(103));
    }

    /**
     * (100) -> (in) -> (r1)
     */
    @Test
    public void withParameter() {
        List<StageModel> stages = compile(ExtractFlowWithParameter.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex2> out2 = mapper.create("r1");

        @SuppressWarnings("unchecked")
        Result<Ex1> f = (Result<Ex1>) create(loader, name, mapper.toArguments());

        Ex1 ex1 = new Ex1();
        ex1.setValue(100);
        f.add(ex1);

        assertThat(out2.getResults().size(), is(1));
        assertThat(out2.getResults().get(0).getValue(), is(200));
    }
}
