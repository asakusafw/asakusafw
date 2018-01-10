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
package com.asakusafw.info.directio;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information of direct file input.
 * @since 0.9.1
 */
public class DirectFileInputInfo extends DirectFilePortInfo {

    private final String filterClass;

    private final boolean optional;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param descriptionClass the description class
     * @param basePath the base path
     * @param resourcePattern the resource pattern
     * @param dataType the data type
     * @param formatClass the data format class
     * @param filterClass the filter class
     * @param optional whether or not this is optional input
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DirectFileInputInfo(
            @JsonProperty(ID_NAME) String name,
            @JsonProperty(ID_DESCRIPTION) String descriptionClass,
            @JsonProperty(ID_BASE_PATH) String basePath,
            @JsonProperty(ID_RESOURCE_PATTERN) String resourcePattern,
            @JsonProperty(ID_DATA_TYPE) String dataType,
            @JsonProperty(ID_FORMAT) String formatClass,
            @JsonProperty("filter") String filterClass,
            @JsonProperty("optional") boolean optional) {
        super(name, descriptionClass, basePath, resourcePattern, dataType, formatClass);
        this.filterClass = filterClass;
        this.optional = optional;
    }

    /**
     * Returns the filter class name.
     * @return the filter class name
     */
    @JsonProperty("filter")
    public String getFilterClass() {
        return filterClass;
    }

    /**
     * Returns whether or not this input is optional.
     * @return {@code true} if this is optional, otherwise {@code false}
     */
    @JsonProperty("optional")
    public boolean isOptional() {
        return optional;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(filterClass);
        result = prime * result + Boolean.hashCode(optional);
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
        DirectFileInputInfo other = (DirectFileInputInfo) obj;
        return super.equals(other)
                && Objects.equals(filterClass, other.filterClass)
                && optional == other.optional;
    }

    @Override
    public String toString() {
        return String.format("DirectFileInput(name=%s)", getName()); //$NON-NLS-1$
    }
}
