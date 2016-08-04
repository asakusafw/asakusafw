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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.Update;

/**
 * Test for {@link UpdateOperatorDriver}.
 */
public class UpdateOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public UpdateOperatorDriverTest() {
        super(new UpdateOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(Update.class)));
    }

    /**
     * Simple testing.
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
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType("com.example.Model")));

                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is(defaultName(Update.class, "outputPort")));
                assertThat(output.getType(), is(sameType("com.example.Model")));
            }
        });
    }

    /**
     * With output name specified.
     */
    @Test
    public void renamed() {
        compile(new Action("com.example.Renamed") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                Node output = description.getOutputs().get(0);
                assertThat(output.getName(), is("renamed"));
            }
        });
    }

    /**
     * With arguments.
     */
    @Test
    public void arguments() {
        compile(new Action("com.example.Arguments") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getArguments().size(), is(2));

                Node arg0 = description.getArguments().get(0);
                assertThat(arg0.getName(), is("arg0"));
                assertThat(arg0.getType(), is(kindOf(TypeKind.INT)));

                Node arg1 = description.getArguments().get(1);
                assertThat(arg1.getName(), is("arg1"));
                assertThat(arg1.getType(), is(sameType(String.class)));
            }
        });
    }

    /**
     * With type parameters.
     */
    @Test
    public void type_parameters() {
        compile(new Action("com.example.TypeParameters") {
            @Override
            protected void perform(OperatorElement target) {
                TypeVariable typeVariable = getTypeVariable(target.getDeclaration(), "T");
                OperatorDescription description = target.getDescription();

                Node input = description.getInputs().get(0);
                assertThat(input.getType(), is(sameType(typeVariable)));

                Node output = description.getOutputs().get(0);
                assertThat(output.getType(), is(sameType(typeVariable)));
            }
        });
    }
}
