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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.asakusafw.info.operator.NamedOperatorSpec;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a pseudo-operator detail of vertices in execution plan.
 * @since 0.9.2
 */
public final class PlanVertexSpec implements NamedOperatorSpec {

    /**
     * The kind label.
     */
    public static final String KIND = "plan-vertex";

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonProperty(Constants.ID_LABEL)
    private final String label;

    @JsonProperty(Constants.ID_DEPENDENCIES)
    @JsonInclude(Include.NON_EMPTY)
    private final List<String> dependencies;

    private PlanVertexSpec(String name, String label, Collection<String> dependencies) {
        this.name = name;
        this.label = label;
        this.dependencies = Util.freeze(dependencies);
    }

    /**
     * Returns an instance.
     * @param name the vertex name
     * @param label the vertex label (nullable)
     * @param dependencies the blocker vertices
     * @return the instance
     */
    @JsonCreator
    public static PlanVertexSpec of(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_LABEL) String label,
            @JsonProperty(Constants.ID_DEPENDENCIES) Collection<String> dependencies) {
        return new PlanVertexSpec(name, label, dependencies);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.PLAN_VERTEX;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the label.
     * @return the label, or {@code null} if it is not defined
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the blocker vertex names.
     * @return the blockers
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getOperatorKind());
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(label);
        result = prime * result + Objects.hashCode(dependencies);
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
        PlanVertexSpec other = (PlanVertexSpec) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(label, other.label)
                && Objects.equals(dependencies, other.dependencies);
    }

    @Override
    public String toString() {
        return String.format("PlanVertex(%s:%s)",
                name,
                Optional.ofNullable(label)
                    .orElse("N/A"));
    }
}
