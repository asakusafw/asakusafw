/**
 * Copyright 2011 Asakusa Framework Team.
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

import static com.ashigeru.lang.java.model.syntax.ModifierKind.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import javax.lang.model.element.PackageElement;


import org.junit.Test;

import com.asakusafw.compiler.operator.OperatorClass;
import com.asakusafw.compiler.operator.OperatorClassCollector;
import com.asakusafw.compiler.operator.OperatorClassGenerator;
import com.asakusafw.compiler.operator.OperatorImplementationClassGenerator;
import com.asakusafw.compiler.operator.OperatorProcessor;
import com.ashigeru.lang.java.jsr269.bridge.Jsr269;
import com.ashigeru.lang.java.model.syntax.ClassDeclaration;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.TypeDeclaration;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder.Strategy;
import com.ashigeru.lang.java.model.util.Models;

/**
 * Test for {@link OperatorImplementationClassGenerator}.
 */
public class OperatorImplementationClassGeneratorTest extends OperatorCompilerTestRoot {

    /**
     * 具象メソッドに対する実装が存在しない。
     */
    @Test
    public void concrete() {
        add("com.example.Concrete");
        ClassDeclaration tree = generate(new MockOperatorProcessor());
        assertThat(Find.modifiers(tree), hasItem(PUBLIC));
        assertThat(Find.modifiers(tree), not(hasItem(ABSTRACT)));
        assertThat(tree.getName().getToken(), is("ConcreteImpl"));
        assertThat(tree.getSuperClass().toString(), is("Concrete"));

        // オーバーライド
        assertThat(Find.method(tree, "example"), is(nullValue()));
    }

    /**
     * 抽象メソッドに対する実装が行われる。
     */
    @Test
    public void skeleton() {
        add("com.example.Abstract");
        ClassDeclaration tree = generate(new MockOperatorProcessor());
        assertThat(Find.modifiers(tree), hasItem(PUBLIC));
        assertThat(Find.modifiers(tree), not(hasItem(ABSTRACT)));
        assertThat(tree.getName().getToken(), is("AbstractImpl"));
        assertThat(tree.getSuperClass().toString(), is("Abstract"));

        // オーバーライド
        MethodDeclaration method = Find.method(tree, "example");
        assertThat(method, not(nullValue()));
        assertThat(Find.modifiers(method), hasItem(PUBLIC));
        assertThat(Find.modifiers(method), not(hasItems(ABSTRACT, STATIC)));
        assertThat(method.getReturnType().toString(), is("CharSequence"));
        List<? extends FormalParameterDeclaration> params = method.getFormalParameters();
        assertThat(params.size(), is(2));
        assertThat(params.get(0).getType().toString(), is("String"));
        assertThat(params.get(0).getName().toString(), is("string"));
        assertThat(params.get(1).getType().toString(), is("int"));
        assertThat(params.get(1).getName().toString(), is("param"));
    }

    private ClassDeclaration generate(OperatorProcessor... procs) {
        Engine engine = new Engine(procs);
        start(engine);
        assertThat(engine.collected, not(nullValue()));
        return (ClassDeclaration) engine.collected;
    }

    private static class Engine extends Callback {

        private OperatorProcessor[] procs;

        TypeDeclaration collected;

        Engine(OperatorProcessor... procs) {
            this.procs = procs;
        }

        @Override
        protected final void test() {
            OperatorClassCollector collector =
                    new OperatorClassCollector(env, round);
            for (OperatorProcessor proc : procs) {
                proc.initialize(env);
                collector.add(proc);
            }
            List<OperatorClass> classes = collector.collect();
            if (classes.isEmpty()) {
                return;
            }
            assertThat(classes.size(), is(1));
            assertThat(collected, is(nullValue()));
            this.collected = collected(classes.get(0));
        }

        protected TypeDeclaration collected(OperatorClass operatorClass) {
            ModelFactory factory = Models.getModelFactory();
            PackageElement pkg = (PackageElement) operatorClass.getElement().getEnclosingElement();
            OperatorClassGenerator generator = new OperatorImplementationClassGenerator(
                    env,
                    factory,
                    new ImportBuilder(
                            factory,
                            new Jsr269(factory).convert(pkg),
                            Strategy.TOP_LEVEL),
                            operatorClass);
            return generator.generate();
        }
    }
}
