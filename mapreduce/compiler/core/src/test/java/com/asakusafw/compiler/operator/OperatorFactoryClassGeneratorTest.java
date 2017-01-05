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

import static com.asakusafw.utils.java.model.syntax.ModifierKind.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import javax.lang.model.element.PackageElement;

import org.junit.Test;

import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Test for {@link OperatorFactoryClassGenerator}.
 */
public class OperatorFactoryClassGeneratorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        TypeDeclaration tree = generate(new MockOperatorProcessor());
        assertThat(tree.getName().getToken(), is("SimpleFactory"));
        assertThat(Find.modifiers(tree), hasItem(PUBLIC));
        assertThat(Find.modifiers(tree), not(hasItem(ABSTRACT)));

        TypeDeclaration type = Find.type(tree, "Example");
        assertThat(type.getModelKind(), is(ModelKind.CLASS_DECLARATION));
        assertThat(Find.modifiers(type), hasItem(PUBLIC));

        FieldDeclaration field = Find.field(type, "out");
        assertThat(Find.modifiers(field), hasItems(PUBLIC, FINAL));
        assertThat(field.getType().toString(), is("Source<CharSequence>"));

        MethodDeclaration method = Find.method(tree, "example");
        assertThat(Find.modifiers(method), hasItem(PUBLIC));
        assertThat(method.getReturnType().toString(), is("SimpleFactory.Example"));
        List<? extends FormalParameterDeclaration> params = method.getFormalParameters();
        assertThat(params.size(), is(2));
        assertThat(params.get(0).getType().toString(), is("Source<String>"));
        assertThat(params.get(0).getName().getToken(), is("in"));
        assertThat(params.get(1).getType().toString(), is("int"));
        assertThat(params.get(1).getName().getToken(), is("param"));
    }

    private TypeDeclaration generate(OperatorProcessor... procs) {
        Engine engine = new Engine(procs);
        start(engine);
        assertThat(engine.collected, not(nullValue()));
        return engine.collected;
    }

    private static class Engine extends Callback {

        private final OperatorProcessor[] procs;

        TypeDeclaration collected;

        Engine(OperatorProcessor... procs) {
            this.procs = procs;
        }

        @Override
        protected final void test() {
            OperatorClassCollector collector = new OperatorClassCollector(env, round);
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
            OperatorClassGenerator generator = new OperatorFactoryClassGenerator(
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
