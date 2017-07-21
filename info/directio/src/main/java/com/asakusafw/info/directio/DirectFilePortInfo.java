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
package com.asakusafw.info.directio;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An abstract implementation of Direct file I/O port information.
 * @since 0.9.1
 */
public abstract class DirectFilePortInfo {

    static final String ID_NAME = "name";

    static final String ID_DESCRIPTION = "description";

    static final String ID_BASE_PATH = "basePath";

    static final String ID_RESOURCE_PATTERN = "resourcePattern";

    static final String ID_DATA_TYPE = "dataType";

    static final String ID_FORMAT = "format";

    private final String name;

    private final String descriptionClass;

    private final String basePath;

    private final String resourcePattern;

    private final String dataType;

    private final String formatClass;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param descriptionClass the description class
     * @param basePath the base path
     * @param resourcePattern the resource pattern
     * @param dataType the data type
     * @param formatClass the data format class
     */
    protected DirectFilePortInfo(
            String name,
            String descriptionClass,
            String basePath, String resourcePattern,
            String dataType, String formatClass) {
        this.name = name;
        this.descriptionClass = descriptionClass;
        this.basePath = basePath;
        this.resourcePattern = resourcePattern;
        this.dataType = dataType;
        this.formatClass = formatClass;
    }

    /**
     * Returns the port name.
     * @return the port name
     */
    @JsonProperty(ID_NAME)
    public String getName() {
        return name;
    }

    /**
     * Returns the description class name.
     * @return the description class name
     */
    @JsonProperty(ID_DESCRIPTION)
    public String getDescriptionClass() {
        return descriptionClass;
    }

    /**
     * Returns the base path.
     * @return the base path
     */
    @JsonProperty(ID_BASE_PATH)
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the resource pattern.
     * @return the resource pattern
     */
    @JsonProperty(ID_RESOURCE_PATTERN)
    public String getResourcePattern() {
        return resourcePattern;
    }

    /**
     * Returns the data type name.
     * @return the data type name
     */
    @JsonProperty(ID_DATA_TYPE)
    public String getDataType() {
        return dataType;
    }

    /**
     * Returns the data format class name.
     * @return the data format class name
     */
    @JsonProperty(ID_FORMAT)
    public String getFormatClass() {
        return formatClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(descriptionClass);
        result = prime * result + Objects.hashCode(basePath);
        result = prime * result + Objects.hashCode(resourcePattern);
        result = prime * result + Objects.hashCode(dataType);
        result = prime * result + Objects.hashCode(formatClass);
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
        DirectFilePortInfo other = (DirectFilePortInfo) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(descriptionClass, other.descriptionClass)
                && Objects.equals(basePath, other.basePath)
                && Objects.equals(resourcePattern, other.resourcePattern)
                && Objects.equals(dataType, other.dataType)
                && Objects.equals(formatClass, other.formatClass);
    }
}
