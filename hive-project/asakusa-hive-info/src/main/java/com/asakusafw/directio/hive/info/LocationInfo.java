/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.info;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Direct I/O location.
 * @since 0.8.1
 */
public class LocationInfo {

    private static final String K_BASE_PATH = "base";

    private static final String K_RESOURCE_PATTERN = "resource";

    private final String basePath;

    private final String resourcePattern;

    /**
     * Creates a new instance.
     * @param basePath the base path
     * @param resourcePattern the resource/output pattern
     */
    @JsonCreator
    public LocationInfo(
            @JsonProperty(value = K_BASE_PATH, required = true) String basePath,
            @JsonProperty(value = K_RESOURCE_PATTERN, required = true) String resourcePattern) {
        this.basePath = basePath;
        this.resourcePattern = resourcePattern;
    }

    /**
     * Returns the base path.
     * @return the base path
     */
    @JsonProperty(K_BASE_PATH)
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the resource/output pattern.
     * @return the resource/output pattern
     */
    @JsonProperty(K_RESOURCE_PATTERN)
    public String getResourcePattern() {
        return resourcePattern;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(basePath);
        result = prime * result + Objects.hashCode(resourcePattern);
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
        LocationInfo other = (LocationInfo) obj;
        if (!Objects.equals(basePath, other.basePath)) {
            return false;
        }
        if (!Objects.equals(resourcePattern, other.resourcePattern)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Location(base=\"%s\", resource=\"%s\")", //$NON-NLS-1$
                getBasePath(),
                getResourcePattern());
    }
}
