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
package com.asakusafw.operator.builtin;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import javax.lang.model.type.TypeKind;

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.Convert;

/**
 * Test for {@link ConvertOperatorDriver}.
 */
public class ConvertOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public ConvertOperatorDriverTest() {
        super(new ConvertOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(Convert.class)));
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

                Node orig = description.getOutputs().get(Convert.ID_OUTPUT_ORIGINAL);
                assertThat(orig, is(notNullValue()));
                assertThat(orig.getName(), is(defaultName(Convert.class, "originalPort")));
                assertThat(orig.getType(), is(sameType("com.example.Model")));
                assertThat(orig.getReference(), is((Reference) Reference.parameter(0)));

                Node out = description.getOutputs().get(Convert.ID_OUTPUT_CONVERTED);
                assertThat(out, is(notNullValue()));
                assertThat(out.getName(), is(defaultName(Convert.class, "convertedPort")));
                assertThat(out.getType(), is(sameType("com.example.Proceeded")));
                assertThat(out.getReference(), is(Reference.returns()));
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
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(2));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Map<String, Node> outputs = toMap(description.getOutputs());

                Node orig = outputs.get(defaultName(Convert.class, "originalPort"));
                assertThat(orig, is(notNullValue()));
                assertThat(orig.getType(), is(sameType("com.example.Model")));
                assertThat(orig.getReference(), is((Reference) Reference.parameter(0)));

                Node out = outputs.get(defaultName(Convert.class, "convertedPort"));
                assertThat(out, is(notNullValue()));
                assertThat(out.getType(), is(sameType("com.example.Proceeded")));
                assertThat(out.getReference(), is(Reference.returns()));

                Map<String, Node> arguments = toMap(description.getArguments());
                assertThat(arguments.get("stringArg"), is(notNullValue()));
                assertThat(arguments.get("stringArg").getType(), is(sameType(String.class)));
                assertThat(arguments.get("intArg"), is(notNullValue()));
                assertThat(arguments.get("intArg").getType(), is(kindOf(TypeKind.INT)));
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
     * violates method returns model.
     */
    @Test
    public void violate_return_model() {
        violate("com.example.ViolateReturnModel");
    }

    /**
     * violates method has only single input.
     */
    @Test
    public void violate_single_input() {
        violate("com.example.ViolateSingleInput");
    }

    /**
     * violates method has only valid parameters.
     */
    @Test
    public void violate_valid_parameter() {
        violate("com.example.ViolateValidParameter");
    }

    /**
     * violates input must appear before parameters.
     */
    @Test
    public void violate_argument_order() {
        violate("com.example.ViolateArgumentOrder");
    }
}
