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
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.Branch;

/**
 * Test for {@link BranchOperatorDriver}.
 */
public class BranchOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public BranchOperatorDriverTest() {
        super(new BranchOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(Branch.class)));
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
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Map<String, Node> outputs = toMap(description.getOutputs());
                assertThat(outputs.get("left"), is(notNullValue()));
                assertThat(outputs.get("left").getType(), is(sameType("com.example.Model")));
                assertThat(outputs.get("left").getReference(), is((Reference) Reference.special("LEFT")));
                assertThat(outputs.get("right"), is(notNullValue()));
                assertThat(outputs.get("right").getType(), is(sameType("com.example.Model")));
                assertThat(outputs.get("right").getReference(), is((Reference) Reference.special("RIGHT")));
            }
        });
    }

    /**
     * with arguments.
     */
    @Test
    public void with_argument() {
        compile(new Action("com.example.WithArgument") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(2));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Map<String, Node> outputs = toMap(description.getOutputs());
                assertThat(outputs.get("left"), is(notNullValue()));
                assertThat(outputs.get("left").getType(), is(sameType("com.example.Model")));
                assertThat(outputs.get("left").getReference(), is((Reference) Reference.special("LEFT")));
                assertThat(outputs.get("right"), is(notNullValue()));
                assertThat(outputs.get("right").getType(), is(sameType("com.example.Model")));
                assertThat(outputs.get("right").getReference(), is((Reference) Reference.special("RIGHT")));

                Map<String, Node> arguments = toMap(description.getArguments());
                assertThat(arguments.get("stringArg"), is(notNullValue()));
                assertThat(arguments.get("stringArg").getType(), is(sameType(String.class)));
                assertThat(arguments.get("intArg"), is(notNullValue()));
                assertThat(arguments.get("intArg").getType(), is(kindOf(TypeKind.INT)));
            }
        });
    }

    /**
     * with projective models.
     */
    @Test
    public void with_projective() {
        compile(new Action("com.example.WithProjective") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                TypeVariable t = getTypeVariable(target.getDeclaration(), "T");

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType(t)));

                Map<String, Node> outputs = toMap(description.getOutputs());
                assertThat(outputs.get("left"), is(notNullValue()));
                assertThat(outputs.get("left").getType(), is(sameType(t)));
                assertThat(outputs.get("left").getReference(), is((Reference) Reference.special("LEFT")));
                assertThat(outputs.get("right"), is(notNullValue()));
                assertThat(outputs.get("right").getType(), is(sameType(t)));
                assertThat(outputs.get("right").getReference(), is((Reference) Reference.special("RIGHT")));
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
     * violates method returns enum.
     */
    @Test
    public void violate_return_enum() {
        violate("com.example.ViolateReturnEnum");
    }

    /**
     * violates method has only single input.
     */
    @Test
    public void violate_single_input() {
        violate("com.example.ViolateSingleInput");
    }

    /**
     * violates method returns not empty enum.
     */
    @Test
    public void violate_not_empty_enum() {
        violate("com.example.ViolateReturnNotEmptyEnum");
    }

    /**
     * violates method has only valid parameters.
     */
    @Test
    public void violate_valid_parameter() {
        violate("com.example.ViolateValidParameter");
    }
}
