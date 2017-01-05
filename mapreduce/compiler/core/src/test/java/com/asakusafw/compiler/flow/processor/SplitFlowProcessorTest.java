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
import com.asakusafw.compiler.flow.processor.flow.SplitFlowTrivial;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Test for {@link SplitFlowProcessor}.
 */
public class SplitFlowProcessorTest extends JobflowCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        List<StageModel> stages = compile(SplitFlowTrivial.class);
        Fragment fragment = stages.get(0).getMapUnits().get(0).getFragments().get(0);
        Name name = fragment.getCompiled().getQualifiedName();

        ClassLoader loader = start();
        PortMapper mapper = new PortMapper(fragment);
        MockResult<Ex1> r1 = mapper.create("ex1");
        MockResult<Ex2> r2 = mapper.create("ex2");

        @SuppressWarnings("unchecked")
        Result<ExJoined> f = (Result<ExJoined>) create(loader, name, mapper.toArguments());

        ExJoined joined = new ExJoined();
        joined.setSid1(1);
        joined.setSid2(2);
        joined.setValue(100);

        f.add(joined);
        assertThat(r1.getResults().size(), is(1));
        assertThat(r2.getResults().size(), is(1));
        assertThat(r1.getResults().get(0).getSid(), is(1L));
        assertThat(r1.getResults().get(0).getValue(), is(100));
        assertThat(r2.getResults().get(0).getSid(), is(2L));
        assertThat(r2.getResults().get(0).getValue(), is(100));
    }
}
