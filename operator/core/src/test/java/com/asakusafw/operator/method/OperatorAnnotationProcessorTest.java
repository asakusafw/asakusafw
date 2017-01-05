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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;

import org.junit.Test;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.MockSource;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.MethodReference;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Node.Kind;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.ReferenceDocument;
import com.asakusafw.operator.model.OperatorDescription.ReturnReference;

/**
 * Test for {@link OperatorAnnotationProcessor}.
 */
public class OperatorAnnotationProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple testing.
     */
    @Test
    public void simple() {
        add(new Driver() {
            @Override
            public OperatorDescription analyze(Context context) {
                List<Node> parameters = new ArrayList<>();
                parameters.add(new Node(
                        Kind.INPUT,
                        "in",
                        new ReferenceDocument(new ParameterReference(0)),
                        context.getEnvironment().findDeclaredType(Descriptions.classOf(String.class)),
                        new ParameterReference(0)));
                List<Node> outputs = new ArrayList<>();
                outputs.add(new Node(
                        Kind.OUTPUT,
                        "out",
                        new ReferenceDocument(new ReturnReference()),
                        context.getEnvironment().findDeclaredType(Descriptions.classOf(CharSequence.class)),
                        new ReturnReference()));
                return new OperatorDescription(new ReferenceDocument(new MethodReference()), parameters, outputs);
            }
        });
        Compiled compiled = compile("com.example.Simple");
        assertThat(compiled.implementation, is(instanceOf(compiled.originalClass)));

        Method impleMethod = method(compiled.implementation.getClass(), "method", String.class);
        assertThat(impleMethod, is(notNullValue()));
        assertThat(Modifier.isAbstract(impleMethod.getModifiers()), is(false));

        Object node = invoke(compiled.factory, "method", MockSource.of(String.class));
        assertThat(field(node.getClass(), "out"), is(notNullValue()));
    }

    /**
     * raise errors on initialize.
     */
    @Test
    public void failure_init() {
        add("com.example.Simple");
        add("com.example.Mock");
        error(new OperatorAnnotationProcessor() {
            @Override
            protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
                throw new RuntimeException();
            }
        });
    }

    /**
     * raise errors on process.
     */
    @Test
    public void failure_process() {
        add(new Driver() {
            @Override
            public OperatorDescription analyze(Context context) {
                throw new RuntimeException();
            }
        });
        add("com.example.Simple");
        add("com.example.Mock");
        error(operatorProcessor());
    }

    private Compiled compile(String name) {
        add(name);
        add("com.example.Mock");
        ClassLoader classLoader = start(operatorProcessor());
        Class<?> origin = load(classLoader, name);
        Object factory = create(classLoader, Constants.getFactoryClass(name));
        Object impl = create(classLoader, Constants.getImplementationClass(name));
        return new Compiled(origin, factory, impl);
    }

    private abstract static class Driver implements OperatorDriver {

        public Driver() {
            return;
        }

        @Override
        public ClassDescription getAnnotationTypeName() {
            return new ClassDescription("com.example.Mock");
        }
    }

    private static class Compiled {

        final Class<?> originalClass;

        final Object factory;

        final Object implementation;

        Compiled(Class<?> originalClass, Object factory, Object implementation) {
            this.originalClass = originalClass;
            this.factory = factory;
            this.implementation = implementation;
        }
    }
}
