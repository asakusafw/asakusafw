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

import javax.lang.model.element.ExecutableElement;

import org.junit.Test;

import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.operator.MasterJoinUpdate;

/**
 * Test for {@link MasterKindOperatorHelper}.
 */
public class MasterKindOperatorHelperTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public MasterKindOperatorHelperTest() {
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
            }
        });
    }

    /**
     * w/ selector method.
     */
    @Test
    public void selector() {
        compile(new Action("com.example.WithSelector") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(1));
            }
        });
    }

    /**
     * selector method w/ transaction input.
     */
    @Test
    public void selector_transaction() {
        compile(new Action("com.example.SelectorWithTransaction") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(2));
            }
        });
    }

    /**
     * selector method w/ master input candidates as collection type.
     */
    @Test
    public void selector_master_collection() {
        compile(new Action("com.example.SelectorWithMasterInputCollection") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(1));
            }
        });
    }

    /**
     * selector method w/ master input candidates as iterable type.
     */
    @Test
    public void selector_master_iterable() {
        compile(new Action("com.example.SelectorWithMasterInputIterable") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(1));
            }
        });
    }

    /**
     * selector method w/ basic parameter.
     */
    @Test
    public void selector_basic() {
        compile(new Action("com.example.SelectorWithBasic") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(1));

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(3));
                assertThat(selector.getParameters().get(2).asType(), is(sameType(String.class)));
            }
        });
    }

    /**
     * selector method w/ basic parameter but is ignored.
     */
    @Test
    public void selector_basic_ignore() {
        compile(new Action("com.example.SelectorWithBasicIgnore") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(2));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(1));

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(2));
            }
        });
    }

    /**
     * selector method w/ view.
     */
    @Test
    public void selector_view() {
        compile(new Action("com.example.SelectorWithView") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(3));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(3));
                assertThat(description.getInputs().get(2).getAttributes(), hasItem(flatView()));
            }
        });
    }

    /**
     * selector method w/ group view.
     */
    @Test
    public void selector_group_view() {
        compile(new Action("com.example.SelectorWithGroupView") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(3));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(3));
                assertThat(description.getInputs().get(2).getAttributes(), hasItem(groupView("=id")));
            }
        });
    }

    /**
     * selector method w/ group view as supertype.
     */
    @Test
    public void selector_group_view_supertype() {
        compile(new Action("com.example.SelectorWithGroupViewSupertype") {
            @Override
            protected void perform(OperatorElement target) {
                OperatorDescription description = target.getDescription();
                assertThat(description.getInputs().size(), is(3));
                assertThat(description.getOutputs().size(), is(2));
                assertThat(description.getArguments().size(), is(0));

                ExecutableElement selector = description.getSupport();
                assertThat(selector.getSimpleName().toString(), is("selector"));
                assertThat(selector.getParameters(), hasSize(3));
                assertThat(description.getInputs().get(2).getAttributes(), hasItem(groupView("=id")));
            }
        });
    }

    /**
     * violates master input is data model type.
     */
    @Test
    public void violate_master_input_data_model() {
        violate("com.example.ViolateMasterInputDataModel");
    }

    /**
     * violates tx input is data model type.
     */
    @Test
    public void violate_tx_input_data_model() {
        violate("com.example.ViolateTxInputDataModel");
    }

    /**
     * violates selector method exists.
     */
    @Test
    public void violate_selector_exist() {
        violate("com.example.ViolateSelectorExist");
    }

    /**
     * violates selector method is identical.
     */
    @Test
    public void violate_selector_identical() {
        violate("com.example.ViolateSelectorIdentical");
    }

    /**
     * violates selector method is public.
     */
    @Test
    public void violate_selector_public() {
        violate("com.example.ViolateSelectorPublic");
    }

    /**
     * violates selector method is not abstract.
     */
    @Test
    public void violate_selector_not_abstract() {
        violate("com.example.ViolateSelectorNotAbstract");
    }

    /**
     * violates selector method is not static.
     */
    @Test
    public void violate_selector_not_static() {
        violate("com.example.ViolateSelectorNotStatic");
    }

    /**
     * violates selector method has consistent master type.
     */
    @Test
    public void violate_selector_master_type_consistent() {
        violate("com.example.ViolateSelectorMasterInputConsistentType");
    }

    /**
     * violates selector method has consistent tx type.
     */
    @Test
    public void violate_selector_tx_type_consistent() {
        violate("com.example.ViolateSelectorTxInputConsistentType");
    }

    /**
     * violates selector method has consistent return type.
     */
    @Test
    public void violate_selector_return_type_consistent() {
        violate("com.example.ViolateSelectorReturnConsistentType");
    }

    /**
     * violates selector method has consistent argument_type.
     */
    @Test
    public void violate_selector_argument_type_consistent() {
        violate("com.example.ViolateSelectorArgumentConsistentType");
    }

    /**
     * violates selector method has non-empty parameters.
     */
    @Test
    public void violate_selector_parameter_not_empty() {
        violate("com.example.ViolateSelectorNotEmptyParameter");
    }

    /**
     * violates selector method has less parameters.
     */
    @Test
    public void violate_selector_parameter_less() {
        violate("com.example.ViolateSelectorLessParameter");
    }

    /**
     * violates selector method has master input with list.
     */
    @Test
    public void violate_selector_master_list_type() {
        violate("com.example.ViolateSelectorMasterInputList");
    }

    /**
     * violates selector method has master input with parameterized list.
     */
    @Test
    public void violate_selector_master_list_parameterized() {
        violate("com.example.ViolateSelectorMasterInputParameterizedList");
    }

    /**
     * violates selector method has pre-defined view.
     */
    @Test
    public void violate_selector_undefined_view() {
        violate("com.example.ViolateSelectorPreDefinedView");
    }

    /**
     * violates selector method views does'nt has view.
     */
    @Test
    public void violate_selector_view_without_key() {
        violate("com.example.ViolateSelectorViewWithoutKey");
    }
}
