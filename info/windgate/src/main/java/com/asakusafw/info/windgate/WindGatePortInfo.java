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
package com.asakusafw.info.windgate;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An abstract implementation of WindGate port information.
 * @since 0.9.2
 */
public abstract class WindGatePortInfo {

    static final String ID_NAME = "name";

    static final String ID_DESCRIPTION = "description";

    static final String ID_PROFILE_NAME = "profile";

    static final String ID_RESOURCE_NAME = "resource";

    static final String ID_CONFIGURATION = "configuration";

    private final String name;

    private final String descriptionClass;

    private final String profileName;

    private final String resourceName;

    private final Map<String, String> configuration;

    /**
     * Creates a new instance.
     * @param name the port name
     * @param descriptionClass the description class
     * @param profileName the profile name
     * @param resourceName the resource name
     * @param configuration the driver configuration
     */
    protected WindGatePortInfo(
            String name,
            String descriptionClass,
            String profileName,
            String resourceName,
            Map<String, String> configuration) {
        this.name = name;
        this.descriptionClass = descriptionClass;
        this.profileName = profileName;
        this.resourceName = resourceName;
        this.configuration = Util.freeze(configuration);
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
     * Returns the profile name.
     * @return the profile name
     */
    @JsonProperty(ID_PROFILE_NAME)
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the resource name.
     * @return the resource name
     */
    @JsonProperty(ID_RESOURCE_NAME)
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the driver configuration.
     * @return the driver configuration
     */
    @JsonProperty(ID_CONFIGURATION)
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(descriptionClass);
        result = prime * result + Objects.hashCode(profileName);
        result = prime * result + Objects.hashCode(resourceName);
        result = prime * result + Objects.hashCode(configuration);
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
        WindGatePortInfo other = (WindGatePortInfo) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(descriptionClass, other.descriptionClass)
                && Objects.equals(profileName, other.profileName)
                && Objects.equals(resourceName, other.resourceName)
                && Objects.equals(configuration, other.configuration);
    }
}
