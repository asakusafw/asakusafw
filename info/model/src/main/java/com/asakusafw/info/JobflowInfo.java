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
package com.asakusafw.info;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a jobflow.
 * @since 0.9.1
 */
public class JobflowInfo implements ElementInfo {

    private final String id;

    private final String description;

    private final Set<String> blockerIds;

    private final List<Attribute> attributes;

    /**
     * Creates a new instance.
     * @param id the flow ID
     * @param description the batch description class (nullable)
     * @param blockerIds the blocker flow IDs
     * @param attributes the attributes
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public JobflowInfo(
            @JsonProperty("id") String id,
            @JsonProperty("description") String description,
            @JsonProperty("blockers") Collection<String> blockerIds,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes) {
        this.id = id;
        this.description = description;
        this.blockerIds = Util.freezeToSet(blockerIds);
        this.attributes = Util.freeze(attributes);
    }

    @JsonProperty
    @Override
    public String getId() {
        return id;
    }

    @JsonProperty("description")
    @Override
    public String getDescriptionClass() {
        return description;
    }

    /**
     * Returns the blocker flow IDs.
     * @return the blocker flow IDs
     */
    @JsonProperty("blockers")
    public Set<String> getBlockerIds() {
        return blockerIds;
    }

    @JsonProperty
    @Override
    public List<? extends Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(id);
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(blockerIds);
        result = prime * result + Objects.hashCode(attributes);
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
        JobflowInfo other = (JobflowInfo) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(description, other.description)
                && Objects.equals(blockerIds, other.blockerIds)
                && Objects.equals(attributes, other.attributes);
    }

    @Override
    public String toString() {
        return String.format("jobflow(id=%s)", getId()); //$NON-NLS-1$
    }
}
