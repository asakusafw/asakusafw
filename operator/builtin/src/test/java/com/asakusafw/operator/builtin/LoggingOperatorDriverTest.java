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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.flow.graph.Connectivity;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.operator.Logging;

/**
 * Test for {@link LoggingOperatorDriver}.
 */
public class LoggingOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public LoggingOperatorDriverTest() {
        super(new LoggingOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(Logging.class)));
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
                assertThat(description.getAttributes(), hasItem(Descriptions.valueOf(Connectivity.OPTIONAL)));
                assertThat(description.getAttributes(), hasItem(Descriptions.valueOf(ObservationCount.AT_LEAST_ONCE)));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is(defaultName(Logging.class, "outputPort")));
                assertThat(output.getType(), is(sameType("com.example.Model")));
                assertThat(output.getAttributes(), hasItem(Descriptions.valueOf(Connectivity.OPTIONAL)));
            }
        });
    }

    /**
     * w/ basic parameters.
     */
    @Test
    public void with_arguments() {
        compile(new Action("com.example.WithArgument") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(1));
                assertThat(description.getArguments().size(), is(1));

                Node node = description.getArguments().get(0);
                assertThat(node.getName(), is("arg"));
                assertThat(node.getType(), is(kindOf(TypeKind.INT)));
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
                assertThat(output.getName(), is("out"));
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
     * violates method returns string.
     */
    @Test
    public void violate_return_string() {
        violate("com.example.ViolateReturnString");
    }

    /**
     * violates method number of input is 1.
     */
    @Test
    public void violate_input_single() {
        violate("com.example.ViolateInputSingle");
    }

    /**
     * violates method valid parameter type.
     */
    @Test
    public void violate_parameter_type() {
        violate("com.example.ViolateParameterType");
    }
}
