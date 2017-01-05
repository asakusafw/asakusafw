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
package com.asakusafw.operator.method;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.junit.Test;

import com.asakusafw.operator.Callback;
import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.MockSource;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.StringDataModelMirrorRepository;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.description.EnumConstantDescription;
import com.asakusafw.operator.model.KeyMirror;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.MethodReference;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Node.Kind;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorDescription.ReferenceDocument;
import com.asakusafw.operator.model.OperatorDescription.ReturnReference;
import com.asakusafw.operator.model.OperatorDescription.SpecialReference;
import com.asakusafw.operator.model.OperatorDescription.TextDocument;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.util.AnnotationHelper;
import com.asakusafw.operator.util.ElementHelper;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription.Parameter;
import com.asakusafw.vocabulary.flow.graph.OperatorHelper;
import com.asakusafw.vocabulary.flow.graph.PortConnection;

/**
 * Test for {@link OperatorFactoryEmitter}.
 */
public class OperatorFactoryEmitterTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Object factory = compile(new Action("com.example.Simple", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                Document document = new TextDocument("Hello, world!");
                List<Node> parameters = new ArrayList<>();
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(document, parameters, outputs);
            }
        });
        Object node = invoke(factory, "method");
        assertThat(node.getClass().getName().replace('$', '.'), is("com.example.SimpleFactory.Method"));
    }

    /**
     * w/ input.
     */
    @Test
    public void input() {
        Object factory = compile(new Action("com.example.WithParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getInputPorts().get(0).getDescription();
        assertThat(port.getName(), is("in"));
        assertThat(port.getDataType(), is((Object) String.class));
    }

    /**
     * w/ output.
     */
    @Test
    public void output() {
        Object factory = compile(new Action("com.example.WithParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "out",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        Object node = invoke(factory, "method");
        assertThat(field(node.getClass(), "out"), is(notNullValue()));
        Object accessed = access(node, "out");
        assertThat(accessed, is(instanceOf(Source.class)));

        FlowElement info = getNode((Source<?>) accessed);
        assertThat(info.getInputPorts().size(), is(0));
        assertThat(info.getOutputPorts().size(), is(1));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getOutputPorts().get(0).getDescription();
        assertThat(port.getName(), is("out"));
        assertThat(port.getDataType(), is((Object) String.class));
    }

    /**
     * w/ argument.
     */
    @Test
    public void argument() {
        Object factory = compile(new Action("com.example.WithParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.DATA,
                        "arg",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        invoke(factory, "method", "Hello, world!");
    }

    /**
     * w/ type parameters.
     */
    @Test
    public void projective() {
        Object factory = compile(new Action("com.example.WithTypeParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        element.getTypeParameters().get(0).asType(),
                        new ParameterReference(0)));
                parameters.add(new Node(
                        Kind.DATA,
                        "arg",
                        new ReferenceDocument(new ParameterReference(1)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(1)));
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "out",
                        new ReferenceDocument(new ReturnReference()),
                        element.getTypeParameters().get(0).asType(),
                        new ReturnReference()));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source, "Hello, world!");

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(1));
        assertThat(getParameters(info).size(), is(1));
    }

    /**
     * w/ attribute.
     */
    @Test
    public void attribute() {
        Object factory = compile(new Action("com.example.WithParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                Document document = new TextDocument("Hello, world!");
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                List<EnumConstantDescription> attributes = new ArrayList<>();
                attributes.add(EnumConstantDescription.of(MockAttribute.OK));
                return new OperatorDescription(document, parameters, outputs, attributes);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));
        assertThat(info.getAttribute(MockAttribute.class), is(MockAttribute.OK));
    }

    /**
     * w/ key.
     */
    @Test
    public void key() {
        add(new StringDataModelMirrorRepository());
        Object factory = compile(new Action("com.example.WithKey", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                VariableElement param = element.getParameters().get(0);
                AnnotationMirror mirror = param.getAnnotationMirrors().get(0);
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0))
                        .withKey(KeyMirror.parse(env, mirror, param, env.findDataModel(param.asType()))));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getInputPorts().get(0).getDescription();
        assertThat(port.getName(), is("in"));
        assertThat(port.getDataType(), is((Object) String.class));
    }

    /**
     * w/ support.
     */
    @Test
    public void support() {
        Object factory = compile(new Action("com.example.WithSupport", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                Document document = new TextDocument("Hello, world!");
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                List<EnumConstantDescription> attributes = new ArrayList<>();
                ExecutableElement support = null;
                for (ExecutableElement m : ElementFilter.methodsIn(element.getEnclosingElement().getEnclosedElements())) {
                    if (m.getSimpleName().contentEquals("support")) {
                        support = m;
                        break;
                    }
                }
                assertThat(support, is(notNullValue()));
                return new OperatorDescription(document, parameters, outputs, attributes)
                    .withSupport(support);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));

        OperatorHelper support = info.getAttribute(OperatorHelper.class);
        assertThat(support, is(notNullValue()));
        assertThat(support.getName(), is("support"));
        assertThat(support.getParameterTypes(), contains((Object) int.class));
    }

    /**
     * w/ external reference.
     */
    @Test
    public void external() {
        Object factory = compile(new Action("com.example.WithVariable", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                TypeElement type = (TypeElement) element.getEnclosingElement();
                VariableElement var = type.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.FIELD)
                    .map(VariableElement.class::cast)
                    .filter(e -> e.getSimpleName().contentEquals("VAR"))
                    .findAny()
                    .get();
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        Document.external(var),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new SpecialReference("SPECIAL")));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getInputPorts().get(0).getDescription();
        assertThat(port.getName(), is("in"));
        assertThat(port.getDataType(), is((Object) String.class));
    }

    /**
     * w/ special reference.
     */
    @Test
    public void special() {
        Object factory = compile(new Action("com.example.Simple", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        Document.reference(Reference.special("SPECIAL")),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new SpecialReference("SPECIAL")));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getInputPorts().get(0).getDescription();
        assertThat(port.getName(), is("in"));
        assertThat(port.getDataType(), is((Object) String.class));
    }

    /**
     * input w/ attribute.
     */
    @Test
    public void input_attribute() {
        Object factory = compile(new Action("com.example.WithParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)).withAttribute(EnumConstantDescription.of(MockAttribute.OK)));
                List<Node> outputs = new ArrayList<>();
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        MockSource<String> source = MockSource.of(String.class);
        invoke(factory, "method", source);

        FlowElement info = getOppositeNode(source);
        assertThat(info.getInputPorts().size(), is(1));
        assertThat(info.getOutputPorts().size(), is(0));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getInputPorts().get(0).getDescription();
        assertThat(port.getName(), is("in"));
        assertThat(port.getDataType(), is((Object) String.class));
        assertThat(port.getAttribute(MockAttribute.class), is(MockAttribute.OK));
    }

    /**
     * output w/ attribute.
     */
    @Test
    public void output_attribute() {
        Object factory = compile(new Action("com.example.WithParameter", "method") {
            @Override
            protected OperatorDescription analyze(ExecutableElement element) {
                List<Node> parameters = new ArrayList<>();
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "out",
                        new ReferenceDocument(new ParameterReference(0)),
                        env.findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)).withAttribute(EnumConstantDescription.of(MockAttribute.OK)));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        Object node = invoke(factory, "method");
        assertThat(field(node.getClass(), "out"), is(notNullValue()));
        Object accessed = access(node, "out");
        assertThat(accessed, is(instanceOf(Source.class)));

        FlowElement info = getNode((Source<?>) accessed);
        assertThat(info.getInputPorts().size(), is(0));
        assertThat(info.getOutputPorts().size(), is(1));
        assertThat(getParameters(info).size(), is(0));
        FlowElementPortDescription port = info.getOutputPorts().get(0).getDescription();
        assertThat(port.getName(), is("out"));
        assertThat(port.getDataType(), is((Object) String.class));
        assertThat(port.getAttribute(MockAttribute.class), is(MockAttribute.OK));
    }

    private FlowElement getOppositeNode(Source<?> source) {
        FlowElementOutput output = source.toOutputPort();
        assertThat(output.getConnected().isEmpty(), is(false));
        for (PortConnection connection : output.getConnected()) {
            return connection.getDownstream().getOwner();
        }
        throw new AssertionError(source);
    }

    private FlowElement getNode(Source<?> source) {
        FlowElementOutput output = source.toOutputPort();
        return output.getOwner();
    }

    private List<Parameter> getParameters(FlowElement info) {
        return ((com.asakusafw.vocabulary.flow.graph.OperatorDescription) info.getDescription()).getParameters();
    }

    private Object compile(Action action) {
        add(action.className);
        add("com.example.Mock");
        ClassLoader classLoader = start(action);
        assertThat(action.performed, is(true));
        ClassDescription implClass = Constants.getFactoryClass(action.className);
        return create(classLoader, implClass);
    }

    private abstract class Action extends Callback {

        final String className;

        final Set<String> methodNames;

        boolean performed;

        Action(String className, String... methodNames) {
            this.className = className;
            this.methodNames = new HashSet<>();
            Collections.addAll(this.methodNames, methodNames);
        }

        @Override
        protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
            return new CompileEnvironment(processingEnv, operatorDrivers, dataModelMirrors);
        }

        @Override
        protected void test() {
            TypeElement element = env.findTypeElement(new ClassDescription(className));
            if (round.getRootElements().contains(element)) {
                TypeElement annotationType = env.findTypeElement(new ClassDescription("com.example.Mock"));
                this.performed = true;
                List<OperatorElement> elems = new ArrayList<>();
                for (ExecutableElement e : ElementFilter.methodsIn(element.getEnclosedElements())) {
                    AnnotationMirror annotation = AnnotationHelper.findAnnotation(env, annotationType, e);
                    if (methodNames.contains(e.getSimpleName().toString())) {
                        OperatorDescription desc = analyze(e);
                        ElementHelper.validate(env, e, desc);
                        elems.add(new OperatorElement(annotation, e, desc));
                    }
                }
                OperatorClass analyzed = new OperatorClass(element, elems);
                new OperatorFactoryEmitter(env).emit(analyzed);
                new OperatorImplementationEmitter(env).emit(analyzed);
            }
        }

        protected abstract OperatorDescription analyze(ExecutableElement element);
    }
}
