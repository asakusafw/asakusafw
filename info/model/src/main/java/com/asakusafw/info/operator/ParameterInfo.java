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

import java.util.Objects;

import com.asakusafw.info.value.ClassInfo;
import com.asakusafw.info.value.ValueInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details of operator parameters.
 * @since 0.9.2
 */
public class ParameterInfo {

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonIgnore
    private final ClassInfo type;

    @JsonProperty(Constants.ID_VALUE)
    private final ValueInfo value;

    /**
     * Creates a new instance.
     * @param name the parameter name
     * @param type the parameter type
     * @param value the argument
     */
    public ParameterInfo(String name, ClassInfo type, ValueInfo value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @JsonCreator
    static ParameterInfo restore(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_TYPE) String type,
            @JsonProperty(Constants.ID_VALUE) ValueInfo value) {
        return new ParameterInfo(name, ClassInfo.of(type), value);
    }

    @JsonProperty(Constants.ID_TYPE)
    String getTypeName() {
        return type.getName();
    }

    /**
     * Returns the parameter name.
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameter type.
     * @return the parameter type
     */
    public ClassInfo getType() {
        return type;
    }

    /**
     * Returns the argument.
     * @return the argument
     */
    public ValueInfo getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(type);
        result = prime * result + Objects.hashCode(value);
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
        ParameterInfo other = (ParameterInfo) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(type, other.type)
                && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return String.format(
                "Parameter(%s:%s:%s)",
                name,
                type.getClassName(),
                value.getObject());
    }
}