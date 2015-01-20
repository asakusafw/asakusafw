/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.compiler.operator.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.junit.Test;

import com.asakusafw.compiler.operator.Callback;
import com.asakusafw.compiler.operator.OperatorCompilerException;
import com.asakusafw.compiler.operator.OperatorCompilerTestRoot;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.compiler.operator.OperatorPortDeclaration;
import com.asakusafw.compiler.operator.model.MockFoo;
import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.vocabulary.flow.FlowPart;

/**
 * Test for {@link FlowPartClassCollector}.
 */
public class FlowPartClassCollectorTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        start(new Collector() {
            @Override
            protected void onCollected(List<FlowPartClass> results) {
                assertThat(results.isEmpty(), is(false));
                FlowPartClass aClass = results.get(0);
                assertThat(aClass.getInputPorts().size(), is(1));
                assertThat(aClass.getOutputPorts().size(), is(1));
                assertThat(aClass.getParameters().size(), is(0));

                OperatorPortDeclaration in = find("in", aClass.getInputPorts());
                OperatorPortDeclaration out = find("out", aClass.getOutputPorts());

                assertTypeEquals(env, in.getType().getRepresentation(), MockHoge.class);
                assertThat(in.getParameterPosition(), is(0));

                assertTypeEquals(env, out.getType().getRepresentation(), MockHoge.class);
                assertThat(out.getParameterPosition(), is(1));
            }
        });
    }

    /**
     * パラメータ化。
     */
    @Test
    public void parameterized() {
        add("com.example.Parameterized");
        start(new Collector() {
            @Override
            protected void onCollected(List<FlowPartClass> results) {
                assertThat(results.isEmpty(), is(false));
                FlowPartClass aClass = results.get(0);
                assertThat(aClass.getInputPorts().size(), is(2));
                assertThat(aClass.getOutputPorts().size(), is(2));
                assertThat(aClass.getParameters().size(), is(2));

                OperatorPortDeclaration in1 = find("in1", aClass.getInputPorts());
                OperatorPortDeclaration in2 = find("in2", aClass.getInputPorts());
                OperatorPortDeclaration out1 = find("out1", aClass.getOutputPorts());
                OperatorPortDeclaration out2 = find("out2", aClass.getOutputPorts());
                OperatorPortDeclaration param1 = find("param1", aClass.getParameters());
                OperatorPortDeclaration param2 = find("param2", aClass.getParameters());

                assertTypeEquals(env, in1.getType().getRepresentation(), MockHoge.class);
                assertThat(in1.getParameterPosition(), is(0));
                assertTypeEquals(env, out1.getType().getRepresentation(), MockHoge.class);
                assertThat(out1.getParameterPosition(), is(1));
                assertThat(param1.getType().getRepresentation().getKind(), is(TypeKind.INT));
                assertThat(param1.getParameterPosition(), is(2));

                assertTypeEquals(env, in2.getType().getRepresentation(), MockFoo.class);
                assertThat(in2.getParameterPosition(), is(3));
                assertTypeEquals(env, out2.getType().getRepresentation(), MockFoo.class);
                assertThat(out2.getParameterPosition(), is(4));
                assertTypeEquals(env, param2.getType().getRepresentation(), String.class);
                assertThat(param2.getParameterPosition(), is(5));
            }
        });
    }

    /**
     * generic flow part.
     */
    @Test
    public void generics() {
        add("com.example.Generic");
        start(new Collector() {
            @Override
            protected void onCollected(List<FlowPartClass> results) {
                assertThat(results.isEmpty(), is(false));
                FlowPartClass aClass = results.get(0);
                assertThat(aClass.getInputPorts().size(), is(1));
                assertThat(aClass.getOutputPorts().size(), is(1));
                assertThat(aClass.getParameters().size(), is(0));

                OperatorPortDeclaration in = find("in", aClass.getInputPorts());
                OperatorPortDeclaration out = find("out", aClass.getOutputPorts());

                assertThat("input/output are same", env.getTypeUtils().isSameType(
                        in.getType().getRepresentation(),
                        out.getType().getRepresentation()),
                        is(true));

                assertThat("input/output use type variable",
                        in.getType().getRepresentation().getKind(),
                        is(TypeKind.TYPEVAR));

                assertThat(in.getParameterPosition(), is(0));
                assertThat(out.getParameterPosition(), is(1));
            }
        });
    }

    /**
     * generic flow part with specified output class.
     */
    @Test
    public void genericWithClass() {
        add("com.example.GenericWithClass");
        start(new Collector() {
            @Override
            protected void onCollected(List<FlowPartClass> results) {
                assertThat(results.isEmpty(), is(false));
                FlowPartClass aClass = results.get(0);
                assertThat(aClass.getInputPorts().size(), is(1));
                assertThat(aClass.getOutputPorts().size(), is(1));
                assertThat(aClass.getParameters().size(), is(1));

                OperatorPortDeclaration in = find("in", aClass.getInputPorts());
                OperatorPortDeclaration out = find("out", aClass.getOutputPorts());
                OperatorPortDeclaration param = aClass.getParameters().get(0);

                assertThat("param is <: Class<?>", env.getTypeUtils().isSameType(
                        env.getErasure(param.getType().getRepresentation()),
                        env.getDeclaredType(Class.class)),
                        is(true));
                assertThat("output/param are same", env.getTypeUtils().isSameType(
                        out.getType().getRepresentation(),
                        ((DeclaredType) param.getType().getRepresentation()).getTypeArguments().get(0)),
                        is(true));

                assertThat("input/output use type variable",
                        in.getType().getRepresentation().getKind(),
                        is(TypeKind.TYPEVAR));

                assertThat(in.getParameterPosition(), is(0));
                assertThat(out.getParameterPosition(), is(1));
                assertThat(param.getParameterPosition(), is(2));
            }
        });
    }

    /**
     * 抽象クラス。
     */
    @Test
    public void Abstract() {
        add("com.example.Abstract");
        error(new Collector());
    }

    /**
     * トップレベルでない。
     */
    @Test
    public void Enclosing() {
        add("com.example.Enclosing");
        error(new Collector());
    }

    /**
     * 公開コンストラクタがない。
     */
    @Test
    public void NoPublicCtors() {
        add("com.example.NoPublicCtors");
        error(new Collector());
    }

    /**
     * FlowDescriptionを継承しない。
     */
    @Test
    public void NotInherited() {
        add("com.example.NotInherited");
        error(new Collector());
    }

    /**
     * クラスが非公開。
     */
    @Test
    public void NotPublic() {
        add("com.example.NotPublic");
        error(new Collector());
    }

    /**
     * コンストラクターが例外持ち。
     */
    @Test
    public void ThrownCtor() {
        add("com.example.ThrownCtor");
        error(new Collector());
    }

    /**
     * パブリックコンストラクタが2つ以上。
     */
    @Test
    public void TooManyCtors() {
        add("com.example.TooManyCtors");
        error(new Collector());
    }

    /**
     * コンストラクタに型引数。
     */
    @Test
    public void TypeParametersCtor() {
        add("com.example.TypeParametersCtor");
        error(new Collector());
    }

    /**
     * コンストラクタに入出力がない。
     */
    @Test
    public void NoIoCtor() {
        add("com.example.NoIoCtor");
        error(new Collector());
    }

    /**
     * モデルでない入出力。
     */
    @Test
    public void NotModel() {
        add("com.example.NotModel");
        error(new Collector());
    }

    /**
     * Output ports have source type of unbound type variables.
     */
    @Test
    public void UnboundGenerics() {
        add("com.example.UnboundGenerics");
        error(new Collector());
    }

    void assertTypeEquals(
            OperatorCompilingEnvironment env,
            TypeMirror type,
            Class<?> expected) {
        TypeElement elem = env.getElementUtils().getTypeElement(expected.getName());
        TypeMirror exType = env.getTypeUtils().getDeclaredType(elem);
        assertTrue(env.getTypeUtils().isSameType(type, exType));
    }

    OperatorPortDeclaration find(String name, Collection<OperatorPortDeclaration> ports) {
        for (OperatorPortDeclaration port : ports) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        throw new AssertionError(name);
    }

    private static class Collector extends Callback {

        Collector() {
            return;
        }

        @Override
        protected final void test() {
            if (round.getRootElements().isEmpty()) {
                return;
            }
            try {
                FlowPartClassCollector collector = new FlowPartClassCollector(env);
                for (Element elem : round.getElementsAnnotatedWith(FlowPart.class)) {
                    collector.add(elem);
                }
                List<FlowPartClass> results = collector.collect();
                onCollected(results);
            } catch (OperatorCompilerException e) {
                // 大域脱出のためだけなのでスキップ
            }
        }

        /**
         * {@link FlowPartClassCollector}によって回収されたメソッドとそのクラス一覧が渡される。
         * @param results クラス一覧
         */
        protected void onCollected(List<FlowPartClass> results) {
            return;
        }
    }
}
