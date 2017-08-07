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
package com.asakusafw.info;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a batch.
 * @since 0.9.1
 */
public class BatchInfo implements ElementInfo {

    /**
     * The schema version.
     */
    public static final String VERSION = "1.0.0";

    private final String id;

    private final String description;

    private final String comment;

    private final List<JobflowInfo> jobflows;

    private final List<Attribute> attributes;

    /**
     * Creates a new instance.
     * @param id the batch ID
     * @param description the batch description class (nullable)
     * @param comment the batch comment (nullable)
     * @param jobflows the jobflows
     * @param attributes the attributes
     */
    public BatchInfo(
            String id,
            String description,
            String comment,
            Collection<? extends JobflowInfo> jobflows,
            Collection<? extends Attribute> attributes) {
        this.id = id;
        this.description = description;
        this.comment = comment;
        this.jobflows = Util.freeze(jobflows);
        this.attributes = Util.freeze(attributes);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    static BatchInfo of(
            @JsonProperty(value = "version", defaultValue = VERSION) String version,
            @JsonProperty("id") String id,
            @JsonProperty("description") String description,
            @JsonProperty("comment") String comment,
            @JsonProperty("jobflows") Collection<? extends JobflowInfo> jobflows,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes) {
        if (Objects.equals(version, VERSION) == false) {
            throw new IllegalArgumentException();
        }
        return new BatchInfo(id, description, comment, jobflows, attributes);
    }

    /**
     * Returns the schema version.
     * @return the schema version
     */
    @JsonProperty("version")
    public String getVersion() {
        return VERSION;
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
     * Returns the comment.
     * @return the comment, or {@code null} if it is not defined
     */
    @JsonProperty
    public String getComment() {
        return comment;
    }

    /**
     * Returns the jobflows.
     * @return the jobflows
     */
    @JsonProperty
    public List<? extends JobflowInfo> getJobflows() {
        return jobflows;
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
        result = prime * result + Objects.hashCode(comment);
        result = prime * result + Objects.hashCode(jobflows);
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
        BatchInfo other = (BatchInfo) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(description, other.description)
                && Objects.equals(comment, other.comment)
                && Objects.equals(jobflows, other.jobflows)
                && Objects.equals(attributes, other.attributes);
    }

    @Override
    public String toString() {
        return String.format("batch(id=%s)", getId()); //$NON-NLS-1$
    }
}
