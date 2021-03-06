/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.operator.flowpart;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;

import org.junit.Test;

import com.asakusafw.operator.Callback;
import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.MockSource;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.MethodReference;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Node.Kind;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.ReferenceDocument;
import com.asakusafw.operator.model.OperatorDescription.TextDocument;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.util.AnnotationHelper;

/**
 * Test for {@link FlowPartFactoryEmitter}.
 */
public class FlowPartFactoryEmitterTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Object factory = compile(new Action("com.example.Simple") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                Document document = new TextDocument("Hello, world!");
                List<Node> parameters = new ArrayList<>();
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(document, parameters, outputs);
            }
        });
        Object node = invoke(factory, "create");
        assertThat(node.getClass().getName().replace('$', '.'), is("com.example.SimpleFactory.Simple"));
    }

    /**
     * w/ input.
     */
    @Test
    public void input() {
        Object factory = compile(new Action("com.example.WithIn") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        getType(String.class),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        invoke(factory, "create", MockSource.of(String.class));
    }

    /**
     * w/ output.
     */
    @Test
    public void output() {
        Object factory = compile(new Action("com.example.WithOut") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "output",
                        new ReferenceDocument(new ParameterReference(0)),
                        getType(String.class),
                        new ParameterReference(0)));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        Object object = invoke(factory, "create");
        assertThat(field(object.getClass(), "output"), is(notNullValue()));
    }

    /**
     * w/ argument.
     */
    @Test
    public void argument() {
        Object factory = compile(new Action("com.example.WithArgument") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.DATA,
                        "argument",
                        new ReferenceDocument(new ParameterReference(0)),
                        getType(String.class),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        invoke(factory, "create", "Hello, world!");
    }

    /**
     * w/ type parameter.
     */
    @Test
    public void projective() {
        Object factory = compile(new Action("com.example.WithTypeParameter") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                TypeElement type = (TypeElement) element.getEnclosingElement();
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "input",
                        new ReferenceDocument(new ParameterReference(0)),
                        type.getTypeParameters().get(0).asType(),
                        new ParameterReference(0)));
                parameters.add(new Node(
                        Kind.DATA,
                        "argument",
                        new ReferenceDocument(new ParameterReference(2)),
                        getType(String.class),
                        new ParameterReference(2)));
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "output",
                        new ReferenceDocument(new ParameterReference(1)),
                        type.getTypeParameters().get(0).asType(),
                        new ParameterReference(1)));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        Object node = invoke(factory, "create", MockSource.of(String.class), "Hello, world!");
        assertThat(field(node.getClass(), "output"), is(notNullValue()));
    }

    /**
     * w/ type parameter.
     */
    @Test
    public void output_infer_by_class() {
        Object factory = compile(new Action("com.example.WithClass") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                TypeElement type = (TypeElement) element.getEnclosingElement();
                TypeVariable va = getTypeVariable(type, "A");
                TypeVariable vb = getTypeVariable(type, "B");
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "input",
                        new ReferenceDocument(new ParameterReference(0)),
                        va,
                        new ParameterReference(0)));
                parameters.add(new Node(
                        Kind.DATA,
                        "argument",
                        new ReferenceDocument(new ParameterReference(2)),
                        getType(Class.class, vb),
                        new ParameterReference(2)));
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "output",
                        new ReferenceDocument(new ParameterReference(1)),
                        vb,
                        new ParameterReference(1)));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        Object node = invoke(factory, "create", MockSource.of(String.class), String.class);
        assertThat(field(node.getClass(), "output"), is(notNullValue()));
    }

    private Object compile(Action action) {
        add(action.className);
        ClassLoader classLoader = start(action);
        assertThat(action.performed, is(true));
        ClassDescription implClass = Constants.getFactoryClass(action.className);
        return create(classLoader, implClass);
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
                    Collections.emptyList(),
                    Collections.emptyList());
        }

        @Override
        protected void test() {
            TypeElement element = env.findTypeElement(new ClassDescription(className));
            if (round.getRootElements().contains(element)) {
                TypeElement annotationType = env.findTypeElement(Constants.TYPE_FLOW_PART);
                AnnotationMirror annotation = AnnotationHelper.findAnnotation(env, annotationType, element);
                this.performed = true;
                List<OperatorElement> elems = new ArrayList<>();
                for (ExecutableElement e : ElementFilter.constructorsIn(element.getEnclosedElements())) {
                    OperatorDescription desc = analyze(e);
                    elems.add(new OperatorElement(annotation, e, desc));
                }
                OperatorClass analyzed = new OperatorClass(element, elems);
                FlowPartFactoryEmitter emitter = new FlowPartFactoryEmitter(env);
                emitter.emit(analyzed);
            }
        }

        protected abstract OperatorDescription analyze(ExecutableElement element);
    }
}
