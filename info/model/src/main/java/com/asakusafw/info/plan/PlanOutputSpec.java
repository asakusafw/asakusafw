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
package com.asakusafw.info.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.asakusafw.info.operator.InputGroup;
import com.asakusafw.info.operator.NamedOperatorSpec;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a pseudo-operator detail of outputs in execution plan.
 * @since 0.9.2
 */
public final class PlanOutputSpec implements NamedOperatorSpec {

    /**
     * The kind label.
     */
    public static final String KIND = "plan-output";

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonProperty(Constants.ID_EXCHANGE)
    private final DataExchange exchange;

    @JsonProperty(Constants.ID_GROUP)
    private final InputGroup group;

    @JsonProperty(Constants.ID_EXTRA)
    @JsonInclude(Include.NON_EMPTY)
    private final List<String> extraOperations;

    private PlanOutputSpec(String name, DataExchange exchange, InputGroup group, Collection<String> extra) {
        this.name = name;
        this.exchange = exchange;
        this.group = group;
        this.extraOperations = Collections.unmodifiableList(new ArrayList<>(extra));
    }

    /**
     * Creates a new instance.
     * @param name the port name
     * @param exchange the data exchanging strategy
     * @param group the data exchanging group (nullable)
     * @return the instance
     */
    public static PlanOutputSpec of(
            String name,
            DataExchange exchange,
            InputGroup group) {
        return of(name, exchange, group, null);
    }

    /**
     * Creates a new instance.
     * @param name the port name
     * @param exchange the data exchanging strategy
     * @param group the data exchanging group (nullable)
     * @param extraOperations the extra operations (nullable)
     * @return the instance
     */
    @JsonCreator
    public static PlanOutputSpec of(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_EXCHANGE) DataExchange exchange,
            @JsonProperty(Constants.ID_GROUP) InputGroup group,
            @JsonProperty(Constants.ID_EXTRA) Collection<String> extraOperations) {
        return new PlanOutputSpec(name, exchange, group, Optional.ofNullable(extraOperations)
                .orElse(Collections.emptyList()));
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.PLAN_OUTPUT;
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

    /**
     * Returns the extra operations.
     * @return the extra operations
     */
    public List<String> getExtraOperations() {
        return extraOperations;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(exchange);
        result = prime * result + Objects.hashCode(group);
        result = prime * result + Objects.hashCode(extraOperations);
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
        PlanOutputSpec other = (PlanOutputSpec) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(exchange, other.exchange)
                && Objects.equals(group, other.group)
                && Objects.equals(extraOperations, other.extraOperations);
    }

    @Override
    public String toString() {
        return String.format("PlanOutput(name=%s, exchange=%s, group=%s)",
                name,
                exchange,
                Optional.ofNullable(group).map(InputGroup::toString).orElse("N/A"));
    }
}
