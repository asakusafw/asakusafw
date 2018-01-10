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

import java.util.Objects;

import com.asakusafw.info.Attribute;
import com.asakusafw.info.value.ClassInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An {@link Attribute} which represents details of operator inputs.
 * @since 0.9.2
 */
public class InputAttribute implements Attribute, InputInfo {

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonIgnore
    private final ClassInfo dataType;

    @JsonProperty(Constants.ID_GRANULARITY)
    private final InputGranularity granulatity;

    @JsonProperty(Constants.ID_GROUP)
    private final InputGroup group;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param dataType the data type
     * @param granulatity the input granularity
     * @param group the input group (nullable)
     */
    public InputAttribute(String name, ClassInfo dataType, InputGranularity granulatity, InputGroup group) {
        this.name = name;
        this.dataType = dataType;
        this.granulatity = granulatity;
        this.group = group;
    }

    @JsonCreator
    static InputAttribute resolve(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_TYPE) String dataType,
            @JsonProperty(Constants.ID_GRANULARITY) InputGranularity granulatity,
            @JsonProperty(Constants.ID_GROUP) InputGroup group) {
        return new InputAttribute(name, ClassInfo.of(dataType), granulatity, group);
    }

    @JsonProperty(Constants.ID_TYPE)
    String getDataTypeName() {
        return dataType.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ClassInfo getDataType() {
        return dataType;
    }

    @Override
    public InputGranularity getGranulatity() {
        return granulatity;
    }

    @Override
    public InputGroup getGroup() {
        return group;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(dataType);
        result = prime * result + Objects.hashCode(granulatity);
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
        InputAttribute other = (InputAttribute) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(dataType, other.dataType)
                && Objects.equals(granulatity, other.granulatity)
                && Objects.equals(group, other.group);
    }

    @Override
    public String toString() {
        return String.format("Input(%s)", name);
    }
}
