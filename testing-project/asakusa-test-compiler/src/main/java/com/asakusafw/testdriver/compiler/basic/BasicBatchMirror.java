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
package com.asakusafw.testdriver.compiler.basic;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.asakusafw.testdriver.compiler.BatchMirror;
import com.asakusafw.testdriver.compiler.JobflowMirror;

/**
 * A basic implementation of {@link BatchMirror}.
 * @since 0.8.0
 */
public class BasicBatchMirror extends AbstractMirror implements BatchMirror {

    private final String batchId;

    private final Map<String, JobflowMirror> elements = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     * @param batchId the batch ID
     */
    public BasicBatchMirror(String batchId) {
        Objects.requireNonNull(batchId);
        this.batchId = batchId;
    }

    @Override
    public String getId() {
        return batchId;
    }

    @Override
    public Collection<JobflowMirror> getElements() {
        return elements.values();
    }

    @Override
    public Optional<JobflowMirror> findElement(String flowId) {
        return Optional.ofNullable(elements.get(flowId));
    }

    /**
     * Adds an element.
     * @param element the element
     */
    public void addElement(JobflowMirror element) {
        this.elements.put(element.getId(), element);
    }
}
