/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.operator.method;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.junit.Test;

import com.asakusafw.operator.Callback;
import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorElement;

/**
 * Test for {@link OperatorMethodAnalyzer}.
 */
public class OperatorMethodAnalyzerTest extends OperatorCompilerTestRoot {

    /**
     * simple.
     */
    @Test
    public void simple() {
        compile(new Action("com.example.Simple") {
            @Override
            protected void perform(OperatorClass target) {
                assertThat(target, is(notNullValue()));
                assertThat(target.getElements().size(), is(1));
                assertThat(find(target, "method"), is(notNullValue()));
            }
        });
    }

    /**
     * must be class.
     */
    @Test
    public void violate_class() {
        violate("com.example.ViolateClass");
    }

    /**
     * must be top-level.
     */
    @Test
    public void violate_top_level() {
        violate("com.example.ViolateTopLevel");
    }

    /**
     * must be public.
     */
    @Test
    public void violate_public() {
        violate("com.example.ViolatePublic");
    }

    /**
     * must be abstract.
     */
    @Test
    public void violate_abstract() {
        violate("com.example.ViolateAbstract");
    }

    /**
     * must be abstract.
     */
    @Test
    public void violate_no_final() {
        violate("com.example.ViolateNoFinal");
    }

    /**
     * must be no type parameters.
     */
    @Test
    public void violate_no_type_parameters() {
        violate("com.example.ViolateNoTypeParameters");
    }

    /**
     * should be no extends.
     */
    @Test
    public void violate_no_extends() {
        violate("com.example.ViolateNoExtends");
    }

    /**
     * should be no implements.
     */
    @Test
    public void violate_no_implements() {
        violate("com.example.ViolateNoImplements");
    }

    /**
     * must have a public empty constructor.
     */
    @Test
    public void violate_empty_constructor() {
        violate("com.example.ViolateEmptyConstructor");
    }

    /**
     * must have no other public methods.
     */
    @Test
    public void violate_no_public_method() {
        violate("com.example.ViolateNoPublicMethod");
    }

    /**
     * constructor must be empty.
     */
    @Test
    public void violate_ctor_public() {
        violate("com.example.ctor.ViolatePublic");
    }

    /**
     * constructor must not have type parameters.
     */
    @Test
    public void violate_ctor_no_type_parameters() {
        violate("com.example.ctor.ViolateNoTypeParameters");
    }

    /**
     * constructor must not have exceptions.
     */
    @Test
    public void violate_ctor_no_exceptions() {
        violate("com.example.ctor.ViolateNoExceptions");
    }

    /**
     * must have up to one operator annotation.
     */
    @Test
    public void violate_method_one_annotation() {
        violate("com.example.method.ViolateOneAnnotation");
    }

    /**
     * method must be public.
     */
    @Test
    public void violate_method_public() {
        violate("com.example.method.ViolatePublic");
    }

    /**
     * method must no be static.
     */
    @Test
    public void violate_method_not_static() {
        violate("com.example.method.ViolateNotStatic");
    }

    /**
     * method must have unique id.
     */
    @Test
    public void violate_method_unique_id() {
        violate("com.example.method.ViolateUniqueId");
    }

    OperatorElement find(OperatorClass target, String name) {
        for (OperatorElement element : target.getElements()) {
            if (element.getDeclaration().getSimpleName().contentEquals(name)) {
                return element;
            }
        }
        return null;
    }

    private void compile(Action action) {
        add(action.className);
        add("com.example.Mock");
        add("com.example.Conflict");
        start(action);
        assertThat(action.performed, is(true));
    }

    private void violate(String className) {
        add(className);
        add("com.example.Mock");
        add("com.example.Conflict");
        Action action = new Action(className) {
            @Override
            protected void perform(OperatorClass target) {
                return;
            }
        };
        error(action);
        assertThat(action.performed, is(true));
    }

    private abstract static class Action extends Callback {

        final String className;

        boolean performed;

        Action(String className) {
            this.className = className;
        }

        @Override
        protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
            return new CompileEnvironment(
                    processingEnv,
                    Arrays.asList(
                            new MockDriver("com.example.Mock"),
                            new MockDriver("com.example.Conflict")),
                    Collections.emptyList());
        }

        @Override
        protected void test() {
            TypeElement element = env.findTypeElement(new ClassDescription(className));
            assertThat(className, element, is(notNullValue()));
            while (element.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                element = (TypeElement) element.getEnclosingElement();
            }
            if (round.getRootElements().contains(element)) {
                this.performed = true;
                OperatorMethodAnalyzer analyzer = new OperatorMethodAnalyzer(env);
                for (TypeElement annotation : annotatios) {
                    if (env.findDriver(annotation) == null) {
                        continue;
                    }
                    for (ExecutableElement method : ElementFilter.methodsIn(round.getElementsAnnotatedWith(annotation))) {
                        analyzer.register(annotation, method);
                    }
                }
                Collection<OperatorClass> resolved = analyzer.resolve();
                if (resolved.size() == 1) {
                    OperatorClass first = resolved.iterator().next();
                    assertThat(first.getDeclaration(), is(element));
                    perform(first);
                } else {
                    perform(null);
                }
            }
        }

        protected abstract void perform(OperatorClass target);
    }

    private static class MockDriver implements OperatorDriver {

        private final ClassDescription typeName;

        public MockDriver(String typeName) {
            this.typeName = new ClassDescription(typeName);
        }

        @Override
        public ClassDescription getAnnotationTypeName() {
            return typeName;
        }

        @Override
        public OperatorDescription analyze(Context context) {
            return null;
        }
    }
}
