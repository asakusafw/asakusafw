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
package com.asakusafw.compiler.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription.Declaration;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription.Parameter;
import com.asakusafw.vocabulary.flow.testing.MockIn;
import com.asakusafw.vocabulary.flow.testing.MockOut;
import com.asakusafw.vocabulary.operator.Branch;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.Convert;
import com.asakusafw.vocabulary.operator.Fold;
import com.asakusafw.vocabulary.operator.GroupSort;
import com.asakusafw.vocabulary.operator.Logging;
import com.asakusafw.vocabulary.operator.MasterBranch;
import com.asakusafw.vocabulary.operator.MasterCheck;
import com.asakusafw.vocabulary.operator.MasterJoin;
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;
import com.asakusafw.vocabulary.operator.Split;
import com.asakusafw.vocabulary.operator.Summarize;
import com.asakusafw.vocabulary.operator.Update;

/**
 * Test for {@link OperatorCompiler}.
 */
public class OperatorCompilerTest extends OperatorCompilerTestRoot {

    /**
     * supported annotation types.
     */
    @Test
    public void types() {
        start(new Callback() {
            @Override
            protected void test() {
                OperatorCompiler compiler = new OperatorCompiler();
                compiler.init(env.getProcessingEnvironment());
                Set<String> supported = compiler.getSupportedAnnotationTypes();
                assertThat(supported, hasItem(Branch.class.getName()));
                assertThat(supported, hasItem(CoGroup.class.getName()));
                assertThat(supported, hasItem(Convert.class.getName()));
                assertThat(supported, hasItem(Fold.class.getName()));
                assertThat(supported, hasItem(GroupSort.class.getName()));
                assertThat(supported, hasItem(Logging.class.getName()));
                assertThat(supported, hasItem(MasterBranch.class.getName()));
                assertThat(supported, hasItem(MasterCheck.class.getName()));
                assertThat(supported, hasItem(MasterJoin.class.getName()));
                assertThat(supported, hasItem(MasterJoinUpdate.class.getName()));
                assertThat(supported, hasItem(Split.class.getName()));
                assertThat(supported, hasItem(Summarize.class.getName()));
                assertThat(supported, hasItem(Update.class.getName()));
            }
        });
    }

    /**
     * simple generate factory.
     */
    @Test
    public void simple_Factory() {
        add("com.example.Simple");
        ClassLoader loader = start(new MockOperatorProcessor());
        Object factory = create(loader, "com.example.SimpleFactory");

        MockIn<String> in = new MockIn<>(String.class, "in");
        Object example = invoke(factory, "example", in, 5);

        Source<CharSequence> op = output(CharSequence.class, example, "out");

        MockOut<CharSequence> out = new MockOut<>(CharSequence.class, "out");
        out.add(op);

        FlowElementOutput port = op.toOutputPort();
        FlowElement element = port.getOwner();
        OperatorDescription desc = (OperatorDescription) element.getDescription();
        List<Parameter> params = desc.getParameters();
        assertThat(params.size(), is(1));
        assertThat(params.get(0).getName(), is("param"));
        assertThat(params.get(0).getType(), is((Type) int.class));
        assertThat(params.get(0).getValue(), is((Object) 5));

        Declaration decl = desc.getDeclaration();
        assertThat(decl.getAnnotationType(), is((Type) MockOperator.class));
        assertThat(decl.getDeclaring().getName(), is("com.example.Simple"));
        assertThat(decl.getImplementing().getName(), is("com.example.SimpleImpl"));
        assertThat(decl.getName(), is("example"));
        assertThat(decl.getParameterTypes(), is((Object) Arrays.asList(String.class, int.class)));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust(desc.getName()));
        assertThat(graph.getConnected(desc.getName()), isJust("out"));
    }

    /**
     * simple generate implementation.
     */
    @Test
    public void simple_Impl() {
        add("com.example.Simple");
        ClassLoader loader = start(new MockOperatorProcessor());
        Object impl = create(loader, "com.example.SimpleImpl");
        Object result = invoke(impl, "example", "hello", 100);
        assertThat(result, is((Object) "hello100!"));
    }

    /**
     * Operators with private overload methods.
     */
    @Test
    public void withPrivateOverload() {
        add("com.example.PrivateOverload");
        ClassLoader loader = start(new MockOperatorProcessor());
        create(loader, "com.example.PrivateOverloadFactory");
        create(loader, "com.example.PrivateOverloadImpl");
    }
}
