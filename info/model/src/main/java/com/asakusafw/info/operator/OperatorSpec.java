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
package com.asakusafw.info.operator;

import com.asakusafw.info.plan.PlanInputSpec;
import com.asakusafw.info.plan.PlanOutputSpec;
import com.asakusafw.info.plan.PlanVertexSpec;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An abstract super interface of operator details.
 * @since 0.9.2
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = Constants.ID_KIND,
        visible = false
)
@JsonSubTypes({
    @Type(value = CoreOperatorSpec.class, name = CoreOperatorSpec.KIND),
    @Type(value = UserOperatorSpec.class, name = UserOperatorSpec.KIND),
    @Type(value = FlowOperatorSpec.class, name = FlowOperatorSpec.KIND),
    @Type(value = InputOperatorSpec.class, name = InputOperatorSpec.KIND),
    @Type(value = OutputOperatorSpec.class, name = OutputOperatorSpec.KIND),
    @Type(value = MarkerOperatorSpec.class, name = MarkerOperatorSpec.KIND),
    @Type(value = CustomOperatorSpec.class, name = CustomOperatorSpec.KIND),
    @Type(value = PlanVertexSpec.class, name = PlanVertexSpec.KIND),
    @Type(value = PlanInputSpec.class, name = PlanInputSpec.KIND),
    @Type(value = PlanOutputSpec.class, name = PlanOutputSpec.KIND),
})
public interface OperatorSpec {

    /**
     * Returns the kind of this operator.
     * @return the operator kind
     */
    @JsonProperty(Constants.ID_KIND)
    OperatorKind getOperatorKind();

    /**
     * Represents a kind of {@link OperatorSpec}.
     * @since 0.9.2
     */
    enum OperatorKind {

        /**
         * Core operators.
         */
        @JsonProperty(CoreOperatorSpec.KIND) CORE,

        /**
         * User operators.
         */
        @JsonProperty(UserOperatorSpec.KIND) USER,

        /**
         * Nested operators.
         */
        @JsonProperty(FlowOperatorSpec.KIND) FLOW,

        /**
         * External inputs.
         */
        @JsonProperty(InputOperatorSpec.KIND) INPUT,

        /**
         * External outputs.
         */
        @JsonProperty(OutputOperatorSpec.KIND) OUTPUT,

        /**
         * Pseudo operators for information.
         * This will be used only in planning, and must not appear in DSLs.
         */
        @JsonProperty(MarkerOperatorSpec.KIND) MARKER,

        /**
         * Custom operators.
         * This will be appeared in some optimization phases.
         */
        @JsonProperty(CustomOperatorSpec.KIND) CUSTOM,

        /**
         * Pseudo operators for plan vertices.
         */
        @JsonProperty(PlanVertexSpec.KIND) PLAN_VERTEX,

        /**
         * Pseudo operators for plan vertex inputs.
         */
        @JsonProperty(PlanInputSpec.KIND) PLAN_INPUT,

        /**
         * Pseudo operators for plan vertex outputs.
         */
        @JsonProperty(PlanOutputSpec.KIND) PLAN_OUTPUT,
    }
}
