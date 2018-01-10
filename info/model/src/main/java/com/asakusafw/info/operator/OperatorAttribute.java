/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An {@link Attribute} which represents details of operators.
 * @since 0.9.2
 */
public class OperatorAttribute implements Attribute, OperatorInfo {

    @JsonProperty(Constants.ID_SPEC)
    private final OperatorSpec spec;

    @JsonProperty(Constants.ID_PARAMETERS)
    @JsonInclude(Include.NON_EMPTY)
    private final List<ParameterInfo> parameters;

    /**
     * Creates a new instance.
     * @param spec the operator body information
     */
    public OperatorAttribute(OperatorSpec spec) {
        this(spec, Collections.emptyList());
    }

    /**
     * Creates a new instance.
     * @param spec the operator body information
     * @param parameters the operator parameters
     */
    public OperatorAttribute(OperatorSpec spec, List<? extends ParameterInfo> parameters) {
        this.spec = spec;
        this.parameters = Util.freeze(parameters);
    }

    @JsonCreator
    static OperatorAttribute restore(
            @JsonProperty(Constants.ID_SPEC) OperatorSpec spec,
            @JsonProperty(Constants.ID_PARAMETERS) List<? extends ParameterInfo> parameters) {
        return new OperatorAttribute(spec, parameters);
    }

    @Override
    public OperatorSpec getSpec() {
        return spec;
    }

    @Override
    public List<ParameterInfo> getParameters() {
        return parameters;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(spec);
        result = prime * result + Objects.hashCode(parameters);
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
        OperatorAttribute other = (OperatorAttribute) obj;
        return Objects.equals(spec, other.spec)
                && Objects.equals(parameters, other.parameters);
    }

    @Override
    public String toString() {
        return String.format("Operator(%s%s)",
                spec,
                parameters);
    }
}
