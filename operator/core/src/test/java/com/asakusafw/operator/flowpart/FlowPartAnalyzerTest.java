/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import org.junit.Test;

import com.asakusafw.operator.Callback;
import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.StringDataModelMirrorRepository;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.flowpart.FlowPartAnalyzer;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link FlowPartAnalyzer}.
 */
public class FlowPartAnalyzerTest extends OperatorCompilerTestRoot {

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
                OperatorElement element = target.getElements().get(0);
                OperatorDescription description = element.getDescription();
                assertThat(description, is(notNullValue()));
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(String.class)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType(String.class)));
            }
        });
    }

    /**
     * With projective model.
     */
    @Test
    public void with_projective() {
        compile(new Action("com.example.ctor.WithProjective") {
            @Override
            protected void perform(OperatorClass target) {
                assertThat(target, is(notNullValue()));
                assertThat(target.getElements().size(), is(1));
                OperatorElement element = target.getElements().get(0);
                OperatorDescription description = element.getDescription();
                assertThat(description, is(notNullValue()));
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                TypeVariable t = getTypeVariable(target.getDeclaration(), "T");

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(t)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType(t)));
            }
        });
    }

    /**
     * With arguments.
     */
    @Test
    public void with_ctor_argument() {
        compile(new Action("com.example.ctor.WithArgument") {
            @Override
            protected void perform(OperatorClass target) {
                assertThat(target, is(notNullValue()));
                assertThat(target.getElements().size(), is(1));
                OperatorElement element = target.getElements().get(0);
                OperatorDescription description = element.getDescription();
                assertThat(description, is(notNullValue()));
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(2));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(String.class)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType(String.class)));

                Map<String, Node> arguments = toMap(description.getArguments());
                assertThat(arguments.get("stringArg"), is(notNullValue()));
                assertThat(arguments.get("stringArg").getType(), is(sameType(String.class)));
                assertThat(arguments.get("intArg"), is(notNullValue()));
                assertThat(arguments.get("intArg").getType(), is(kindOf(TypeKind.INT)));
            }
        });
    }

    /**
     * With extern in.
     */
    @Test
    public void with_ctor_extern_in() {
        add("com/example/external/StringImporter");
        compile(new Action("com.example.ctor.WithExternIn") {
            @Override
            protected void perform(OperatorClass target) {
                assertThat(target, is(notNullValue()));
                assertThat(target.getElements().size(), is(1));
                OperatorElement element = target.getElements().get(0);
                OperatorDescription description = element.getDescription();
                assertThat(description, is(notNullValue()));
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(String.class)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType(String.class)));
            }
        });
    }

    /**
     * With extern out.
     */
    @Test
    public void with_ctor_extern_out() {
        add("com/example/external/StringExporter");
        compile(new Action("com.example.ctor.WithExternOut") {
            @Override
            protected void perform(OperatorClass target) {
                assertThat(target, is(notNullValue()));
                assertThat(target.getElements().size(), is(1));
                OperatorElement element = target.getElements().get(0);
                OperatorDescription description = element.getDescription();
                assertThat(description, is(notNullValue()));
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(String.class)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType(String.class)));
            }
        });
    }

    /**
     * class must be top level.
     */
    @Test
    public void violate_top_level() {
        error("com.example.ViolateTopLevel$Nested");
    }

    /**
     * class must be public.
     */
    @Test
    public void violate_public() {
        error("com.example.ViolatePublic");
    }

    /**
     * class must not be abstract.
     */
    @Test
    public void violate_not_abstract() {
        error("com.example.ViolateNotAbstract");
    }

    /**
     * class must extend {@link FlowDescription}.
     */
    @Test
    public void violate_extends_FlowDescription() {
        error("com.example.ViolateExtendsFlowDescription");
    }

    /**
     * class must be with annotation.
     */
    @Test
    public void violate_with_annotation() {
        error("com.example.ViolateWithAnnotation");
    }

    /**
     * class must have single constructor.
     */
    @Test
    public void violate_at_least_one_constructor() {
        error("com.example.ViolateAtLeastOneConstructor");
    }

    /**
     * class must have single constructor.
     */
    @Test
    public void violate_up_to_one_constructor() {
        error("com.example.ViolateUpToOneConstructor");
    }

    /**
     * constructor must be public.
     */
    @Test
    public void violate_ctor_public() {
        error("com.example.ctor.ViolatePublic");
    }

    /**
     * constructor must not have any type parameters.
     */
    @Test
    public void violate_ctor_no_type_parameters() {
        error("com.example.ctor.ViolateNoTypeParameters");
    }

    /**
     * constructor must not have any exceptions.
     */
    @Test
    public void violate_ctor_no_exceptions() {
        error("com.example.ctor.ViolateNoExceptions");
    }

    /**
     * constructor must have input.
     */
    @Test
    public void violate_ctor_at_least_one_input() {
        error("com.example.ctor.ViolateAtLeastOneInput");
    }

    /**
     * constructor must have input.
     */
    @Test
    public void violate_ctor_at_least_one_output() {
        error("com.example.ctor.ViolateAtLeastOneOutput");
    }

    /**
     * constructor input must be data-model.
     */
    @Test
    public void violate_ctor_input_argument() {
        error("com.example.ctor.ViolateInputArgument");
    }

    /**
     * constructor output must be data-model.
     */
    @Test
    public void violate_ctor_output_argument() {
        error("com.example.ctor.ViolateOutputArgument");
    }

    /**
     * constructor input must be data-model.
     */
    @Test
    public void violate_ctor_input_type() {
        error("com.example.ctor.ViolateInputType");
    }

    /**
     * constructor input must be data-model.
     */
    @Test
    public void violate_ctor_output_type() {
        error("com.example.ctor.ViolateOutputType");
    }

    /**
     * external input must not be projective.
     */
    @Test
    public void violate_ctor_extern_input_not_projective() {
        add("com/example/external/StringImporter");
        error("com.example.ctor.ViolateExternInputNotProjective");
    }

    /**
     * external output must not be projective.
     */
    @Test
    public void violate_ctor_extern_output_not_projective() {
        add("com/example/external/StringExporter");
        error("com.example.ctor.ViolateExternOutputNotProjective");
    }

    /**
     * constructor argument must not have extern.
     */
    @Test
    public void violate_ctor_no_import_argument() {
        add("com/example/external/StringImporter");
        add("com/example/external/StringExporter");
        error("com.example.ctor.ViolateNoImportArgument");
    }

    /**
     * constructor argument must not have extern.
     */
    @Test
    public void violate_ctor_no_export_argument() {
        add("com/example/external/StringImporter");
        add("com/example/external/StringExporter");
        error("com.example.ctor.ViolateNoExportArgument");
    }

    /**
     * constructor parameter must not have import annotation.
     */
    @Test
    public void violate_ctor_with_not_only_extern() {
        add("com/example/external/StringImporter");
        add("com/example/external/StringExporter");
        error("com.example.ctor.ViolateWithNotOnlyExtern");
    }

    /**
     * constructor output must be type-inferable.
     */
    @Test
    public void violate_ctor_output_inferable() {
        error("com.example.ctor.ViolateOutputInferable");
    }

    private void compile(Action action) {
        add(action.className);
        start(action);
        assertThat(action.performed, is(true));
    }

    private void error(String className) {
        Action action = new Action(className) {
            @Override
            protected void perform(OperatorClass target) {
                return;
            }
        };
        error(action);
    }

    private void error(Action action) {
        int index = action.className.indexOf('$');
        String file;
        if (index >= 0) {
            file = action.className.substring(0, index);
        } else {
            file = action.className;
        }
        add(file);
        error((Callback) action);
        assertThat("not performed", action.performed, is(true));
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
                    Arrays.asList(new StringDataModelMirrorRepository())).withFlowpartExternalIo(true);
        }

        @Override
        protected void test() {
            TypeElement element = env.findTypeElement(new ClassDescription(className));
            TypeElement current = element;
            while (current != null && current.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                current = (TypeElement) current.getEnclosingElement();
            }
            if (round.getRootElements().contains(current)) {
                this.performed = true;
                FlowPartAnalyzer analyzer = new FlowPartAnalyzer(env);
                analyzer.register(element);
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

        /**
         * Converts node list into {@code name -> node} map.
         * @param nodes node list
         * @return converted map
         */
        protected Map<String, Node> toMap(List<Node> nodes) {
            Map<String, Node> results = new HashMap<>();
            for (Node node : nodes) {
                results.put(node.getName(), node);
            }
            return results;
        }
    }
}
