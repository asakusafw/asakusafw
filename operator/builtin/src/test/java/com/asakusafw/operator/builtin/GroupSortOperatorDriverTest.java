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
package com.asakusafw.operator.builtin;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.GroupSort;

/**
 * Test for {@link GroupSortOperatorDriver}.
 */
public class GroupSortOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public GroupSortOperatorDriverTest() {
        super(new GroupSortOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(GroupSort.class)));
    }

    /**
     * simple case.
     */
    @Test
    public void simple() {
        compile(new Action("com.example.Simple") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ multiple output.
     */
    @Test
    public void output_multiple() {
        compile(new Action("com.example.WithOutputMany") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(3));
                assertThat(description.getArguments().size(), is(0));

                assertThat(description.getOutputs().get(0).getName(), is("out0"));
                assertThat(description.getOutputs().get(1).getName(), is("out1"));
                assertThat(description.getOutputs().get(2).getName(), is("out2"));
            }
        });
    }

    /**
     * w/ arguments.
     */
    @Test
    public void with_argument() {
        compile(new Action("com.example.WithArgument") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(2));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));

                Map<String, Node> arguments = toMap(description.getArguments());
                assertThat(arguments.get("stringArg"), is(notNullValue()));
                assertThat(arguments.get("stringArg").getType(), is(sameType(String.class)));
                assertThat(arguments.get("intArg"), is(notNullValue()));
                assertThat(arguments.get("intArg").getType(), is(kindOf(TypeKind.INT)));
            }
        });
    }

    /**
     * w/ iterable input.
     */
    @Test
    public void with_iterable() {
        compile(new Action("com.example.WithIterable") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("out"));
                assertThat(output.getType(), is(sameType("com.example.Proceeded")));
            }
        });
    }

    /**
     * w/ type parameters.
     */
    @Test
    public void with_projective() {
        compile(new Action("com.example.WithProjective") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                TypeVariable t = getTypeVariable(target.getDeclaration(), "T");

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("in"));
                assertThat(input.getType(), is(sameType(t)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getType(), is(sameType(t)));
            }
        });
    }

    /**
     * violates method is not abstract.
     */
    @Test
    public void violate_not_abstract() {
        violate("com.example.ViolateNotAbstract");
    }

    /**
     * violates method returns void.
     */
    @Test
    public void violate_return_void() {
        violate("com.example.ViolateReturnVoid");
    }

    /**
     * violates method input must be a model.
     */
    @Test
    public void violate_input_with_model() {
        violate("com.example.ViolateInputWithModel");
    }

    /**
     * violates method output type is inferable.
     */
    @Test
    public void violate_input_single() {
        violate("com.example.ViolateInputSingle");
    }

    /**
     * violates method output must be a model.
     */
    @Test
    public void violate_output_with_model() {
        violate("com.example.ViolateOutputWithModel");
    }

    /**
     * violates method has only valid parameters.
     */
    @Test
    public void violate_valid_parameter() {
        violate("com.example.ViolateValidParameter");
    }

    /**
     * violates method output type is inferable.
     */
    @Test
    public void violate_output_inferable() {
        violate("com.example.ViolateOutputInferable");
    }
}
