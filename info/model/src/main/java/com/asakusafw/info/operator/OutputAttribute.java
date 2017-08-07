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
package com.asakusafw.info.operator;

import java.util.Objects;

import com.asakusafw.info.Attribute;
import com.asakusafw.info.value.ClassInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An {@link Attribute} which represents details of operator outputs.
 * @since 0.9.2
 */
public class OutputAttribute implements Attribute, OutputInfo {

    @JsonProperty(Constants.ID_NAME)
    private final String name;

    @JsonIgnore
    private final ClassInfo dataType;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param dataType the data type
     */
    public OutputAttribute(String name, ClassInfo dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    @JsonCreator
    static OutputAttribute resolve(
            @JsonProperty(Constants.ID_NAME) String name,
            @JsonProperty(Constants.ID_TYPE) String dataType) {
        return new OutputAttribute(name, ClassInfo.of(dataType));
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(dataType);
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
        OutputAttribute other = (OutputAttribute) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(dataType, other.dataType);
    }

    @Override
    public String toString() {
        return String.format("Output(%s)", name);
    }
}
