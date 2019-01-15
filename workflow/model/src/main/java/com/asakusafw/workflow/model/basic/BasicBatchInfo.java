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
package com.asakusafw.workflow.model.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.asakusafw.workflow.model.BatchInfo;
import com.asakusafw.workflow.model.JobflowInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A basic implementation of {@link BatchInfo}.
 * @since 0.10.0
 */
public class BasicBatchInfo extends AbstractElement implements BatchInfo {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("elements")
    private final List<JobflowInfo> elements = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param id the batch ID
     */
    public BasicBatchInfo(String id) {
        this.id = id;
    }

    @JsonCreator
    static BasicBatchInfo restore(
            @JsonProperty("id") String id,
            @JsonProperty("elements") Collection<? extends JobflowInfo> elements,
            @JsonProperty("attributes") Collection<? extends Attribute> attributes) {
        BasicBatchInfo result = new BasicBatchInfo(id);
        Optional.ofNullable(elements).ifPresent(it -> it.forEach(result::addElement));
        result.setAttributes(attributes);
        return result;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<? extends JobflowInfo> getElements() {
        return elements;
    }

    @Override
    public Optional<? extends JobflowInfo> findElement(String flowId) {
        return getElements().stream()
                .filter(it -> it.getId().equals(flowId))
                .findFirst();
    }

    /**
     * Adds an element.
     * @param element the element
     */
    public void addElement(JobflowInfo element) {
        this.elements.add(element);
    }
}
