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

/**
 * Test for {@link OperatorClassEmitter}.
 */
public class OperatorClassEmitterTest extends OperatorCompilerTestRoot {

    /**
     * emits factory class.
     */
    @Test
    public void emitFactory() {
        add("com.example.Concrete");
        ClassLoader loader = compile(new MockOperatorProcessor());
        Object factory = create(loader, "com.example.ConcreteFactory");

        MockIn<String> in = new MockIn<>(String.class, "in");
        Object example = invoke(factory, "example", in, 5);

        @SuppressWarnings("unchecked")
        Source<CharSequence> op = (Source<CharSequence>) access(example, "out");

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
        assertThat(decl.getDeclaring().getName(), is("com.example.Concrete"));
        assertThat(decl.getImplementing().getName(), is("com.example.ConcreteImpl"));
        assertThat(decl.getName(), is("example"));
        assertThat(decl.getParameterTypes(), is((Object) Arrays.asList(String.class, int.class)));

        Graph<String> graph = toGraph(in);
        assertThat(graph.getConnected("in"), isJust(desc.getName()));
        assertThat(graph.getConnected(desc.getName()), isJust("out"));
    }

    /**
     * conflict between operator and annotation name.
     */
    @Test
    public void emitFactory_annotationNameConflict() {
        add("com.example.Conflict");
        compile(new MockOperatorProcessor());
    }

    /**
     * emit operator implementation w/o any abstract operator methods.
     */
    @Test
    public void emitConcreteImpl() {
        add("com.example.Concrete");
        ClassLoader loader = compile(new MockOperatorProcessor());
        Object impl = create(loader, "com.example.ConcreteImpl");
        Object result = invoke(impl, "example", "hello", 100);
        assertThat(result, is((Object) "hello100"));
    }

    /**
     * emits operator implementation w some abstract operator methods.
     */
    @Test
    public void emitAbstractImpl() {
        add("com.example.Abstract");
        ClassLoader loader = compile(new MockOperatorProcessor());
        Object impl = create(loader, "com.example.AbstractImpl");
        Object result = invoke(impl, "example", "hello", 100);
        assertThat(result, is((Object) "hello100!"));
    }

    private ClassLoader compile(OperatorProcessor... procs) {
        return start(new Callback() {
            @Override
            protected void test() {
                OperatorClassCollector collector = new OperatorClassCollector(env, round);
                for (OperatorProcessor proc : procs) {
                    proc.initialize(env);
                    collector.add(proc);
                }
                List<OperatorClass> classes = collector.collect();
                OperatorClassEmitter emitter = new OperatorClassEmitter(env);
                for (OperatorClass aClass : classes) {
                    emitter.emit(aClass);
                }
            }
        });
    }
}
