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

import org.junit.Test;

import com.asakusafw.operator.builtin.SplitOperatorDriver;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.vocabulary.operator.Split;

/**
 * Test for {@link SplitOperatorDriver}.
 */
public class SplitOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public SplitOperatorDriverTest() {
        super(new SplitOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(Split.class)));
    }

    /**
     * Simple testing.
     */
    @Test
    public void simple() {
        addDataModel("JModel");
        addDataModel("LModel");
        addDataModel("RModel");
        compile(new Action("com.example.Simple") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(1));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                Node input = description.getInputs().get(0);
                assertThat(input.getName(), is("model"));
                assertThat(input.getType(), is(sameType("com.example.JModel")));
                assertThat(input.getReference(), is((Reference) Reference.parameter(0)));

                Map<String, Node> outputs = toMap(description.getOutputs());

                Node left = outputs.get("left");
                assertThat(left, is(notNullValue()));
                assertThat(left.getType(), is(sameType("com.example.LModel")));
                assertThat(left.getReference(), is((Reference) Reference.parameter(1)));

                Node right = outputs.get("right");
                assertThat(right, is(notNullValue()));
                assertThat(right.getType(), is(sameType("com.example.RModel")));
                assertThat(right.getReference(), is((Reference) Reference.parameter(2)));
            }
        });
    }
}
