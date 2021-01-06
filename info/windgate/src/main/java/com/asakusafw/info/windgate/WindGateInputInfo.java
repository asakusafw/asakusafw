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
package com.asakusafw.info.windgate;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information of WindGate input.
 * @since 0.9.2
 */
public class WindGateInputInfo extends WindGatePortInfo {

    /**
     * Creates a new instance.
     * @param name the port name
     * @param descriptionClass the description class
     * @param profileName the profile name
     * @param resourceName the resource name
     * @param configuration the driver configuration
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public WindGateInputInfo(
            @JsonProperty(ID_NAME) String name,
            @JsonProperty(ID_DESCRIPTION) String descriptionClass,
            @JsonProperty(ID_PROFILE_NAME) String profileName,
            @JsonProperty(ID_RESOURCE_NAME) String resourceName,
            @JsonProperty(ID_CONFIGURATION) Map<String, String> configuration) {
        super(name, descriptionClass, profileName, resourceName, configuration);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 1;
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
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("WindGateInput(name=%s)", getName()); //$NON-NLS-1$
    }
}
