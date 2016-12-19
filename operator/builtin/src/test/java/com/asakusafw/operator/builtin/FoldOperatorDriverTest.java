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

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.Fold;

/**
 * Test for {@link FoldOperatorDriver}.
 */
public class FoldOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public FoldOperatorDriverTest() {
        super(new FoldOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(Fold.class)));
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
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is(defaultName(Fold.class, "outputPort")));
                assertThat(output.getType(), is(sameType("com.example.Model")));
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
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is(defaultName(Fold.class, "outputPort")));
                assertThat(output.getType(), is(sameType("com.example.Model")));

                Map<String, Node> arguments = toMap(description.getArguments());
                assertThat(arguments.get("stringArg"), is(notNullValue()));
                assertThat(arguments.get("stringArg").getType(), is(sameType(String.class)));
                assertThat(arguments.get("intArg"), is(notNullValue()));
                assertThat(arguments.get("intArg").getType(), is(kindOf(TypeKind.INT)));
            }
        });
    }

    /**
     * w/ table
     */
    @Test
    public void with_table() {
        compile(new Action("com.example.WithTable") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getType(), is(sameType("com.example.Model")));
                assertThat(input.getAttributes(), not(hasItem(isView())));

                Node side = description.getInputs().get(1);
                assertThat(side.getType(), is(sameType("com.example.Model")));
                assertThat(side.getAttributes(), hasItem(groupView("=content")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is(defaultName(Fold.class, "outputPort")));
                assertThat(output.getType(), is(sameType("com.example.Model")));
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
    public void violate_return_void() {
        violate("com.example.ViolateReturnVoid");
    }

    /**
     * violates method first parameter must be a model.
     */
    @Test
    public void violate_input_with_model1() {
        violate("com.example.ViolateInputWithModel1");
    }

    /**
     * violates method second parameter must be a model.
     */
    @Test
    public void violate_input_with_model2() {
        violate("com.example.ViolateInputWithModel2");
    }

    /**
     * violates method both inputs must be same type.
     */
    @Test
    public void violate_input_same_type() {
        violate("com.example.ViolateInputSameType");
    }

    /**
     * violates method has just twin input.
     */
    @Test
    public void violate_input_with_key() {
        violate("com.example.ViolateInputWithKey");
    }

    /**
     * violates method has just twin input.
     */
    @Test
    public void violate_twin_input1() {
        violate("com.example.ViolateTwinInput1");
    }

    /**
     * violates method has just twin input.
     */
    @Test
    public void violate_twin_input3() {
        violate("com.example.ViolateTwinInput3");
    }

    /**
     * violates method has only valid parameters.
     */
    @Test
    public void violate_valid_parameter() {
        violate("com.example.ViolateValidParameter");
    }

    /**
     * violates method has table without partial.
     */
    @Test
    public void violate_table_without_partial() {
        violate("com.example.ViolateTableWithoutPartial");
    }
}
