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

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;

/**
 * Test for {@link MasterJoinUpdateOperatorDriver}.
 */
public class MasterJoinUpdateOperatorDriverTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public MasterJoinUpdateOperatorDriverTest() {
        super(new MasterJoinUpdateOperatorDriver());
    }

    /**
     * annotation.
     */
    @Test
    public void annotationTypeName() {
        assertThat(driver.getAnnotationTypeName(), is(Descriptions.classOf(MasterJoinUpdate.class)));
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
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                Node mst = description.getInputs().get(0);
                assertThat(mst.getName(), is("side"));
                assertThat(mst.getType(), is(sameType("com.example.Side")));

                Node tx = description.getInputs().get(1);
                assertThat(tx.getName(), is("model"));
                assertThat(tx.getType(), is(sameType("com.example.Model")));

                Map<String, Node> outputs = toMap(description.getOutputs());

                Node found = outputs.get(defaultName(MasterJoinUpdate.class, "updatedPort"));
                assertThat(found, is(notNullValue()));
                assertThat(found.getType(), is(sameType("com.example.Model")));

                Node missed = outputs.get(defaultName(MasterJoinUpdate.class, "missedPort"));
                assertThat(missed, is(notNullValue()));
                assertThat(missed.getType(), is(sameType("com.example.Model")));
            }
        });
    }
}
