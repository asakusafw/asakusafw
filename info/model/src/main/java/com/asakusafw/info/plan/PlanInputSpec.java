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
package com.asakusafw.info.plan;

import java.util.Objects;
import java.util.Optional;

import com.asakusafw.info.operator.InputGroup;
import com.asakusafw.info.operator.NamedOperatorSpec;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a pseudo-operator detail of inputs in execution plan.
 * @since 0.9.2
 */
public final class PlanInputSpec implements NamedOperatorSpec {

    /**
     * The kind label.
     */
    public static final String KIND = "plan-input";

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonProperty(Constants.ID_EXCHANGE)
    private final DataExchange exchange;

    @JsonProperty(Constants.ID_GROUP)
    private final InputGroup group;

    private PlanInputSpec(String name, DataExchange exchange, InputGroup group) {
        this.name = name;
        this.exchange = exchange;
        this.group = group;
    }

    /**
     * Creates a new instance.
     * @param name the port name
     * @param exchange the data exchanging strategy
     * @param group the data exchanging group (nullable)
     * @return the instance
     */
    @JsonCreator
    public static PlanInputSpec of(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_EXCHANGE) DataExchange exchange,
            @JsonProperty(Constants.ID_GROUP) InputGroup group) {
        return new PlanInputSpec(name, exchange, group);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.PLAN_INPUT;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the data exchanging strategy.
     * @return the data exchanging strategy
     */
    public DataExchange getExchange() {
        return exchange;
    }

    /**
     * Returns the data exchanging group.
     * @return the data exchanging group, or {@code null} if it is not defined
     */
    public InputGroup getGroup() {
        return group;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(exchange);
        result = prime * result + Objects.hashCode(group);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlanInputSpec other = (PlanInputSpec) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(exchange, other.exchange)
                && Objects.equals(group, other.group);
    }

    @Override
    public String toString() {
        return String.format("PlanInput(name=%s, exchange=%s, group=%s)",
                name,
                exchange,
                Optional.ofNullable(group).map(InputGroup::toString).orElse("N/A"));
    }
}
