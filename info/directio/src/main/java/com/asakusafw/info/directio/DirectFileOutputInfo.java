/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.directio;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information of direct file output.
 * @since 0.9.1
 */
public class DirectFileOutputInfo extends DirectFilePortInfo {

    private final List<String> order;

    private final List<String> deletePatterns;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param descriptionClass the description class
     * @param basePath the base path
     * @param resourcePattern the resource pattern
     * @param dataType the data type
     * @param formatClass the data format class
     * @param order the record order
     * @param deletePatterns the delete patterns
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DirectFileOutputInfo(
            @JsonProperty(ID_NAME) String name,
            @JsonProperty(ID_DESCRIPTION) String descriptionClass,
            @JsonProperty(ID_BASE_PATH) String basePath,
            @JsonProperty(ID_RESOURCE_PATTERN) String resourcePattern,
            @JsonProperty(ID_DATA_TYPE) String dataType,
            @JsonProperty(ID_FORMAT) String formatClass,
            @JsonProperty("order") List<String> order,
            @JsonProperty("deletePatterns") List<String> deletePatterns) {
        super(name, descriptionClass, basePath, resourcePattern, dataType, formatClass);
        this.order = Util.freeze(order);
        this.deletePatterns = Util.freeze(deletePatterns);
    }

    /**
     * Returns the record order.
     * @return the record order
     */
    @JsonProperty("order")
    public List<String> getOrder() {
        return order;
    }

    /**
     * Returns the delete patterns.
     * @return the delete patterns
     */
    @JsonProperty("deletePatterns")
    public List<String> getDeletePatterns() {
        return deletePatterns;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(order);
        result = prime * result + Objects.hashCode(deletePatterns);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DirectFileOutputInfo other = (DirectFileOutputInfo) obj;
        return super.equals(other)
                && Objects.equals(order, other.order)
                && Objects.equals(deletePatterns, other.deletePatterns);
    }

    @Override
    public String toString() {
        return String.format("DirectFileOutput(name=%s)", getName()); //$NON-NLS-1$
    }
}
