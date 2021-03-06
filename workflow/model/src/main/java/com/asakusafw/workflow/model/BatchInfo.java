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
package com.asakusafw.workflow.model;

import java.util.Collection;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represents a batch.
 * @since 0.10.0
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class",
        visible = false
)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        scope = BatchInfo.class,
        property = "oid"
)
public interface BatchInfo extends Element {

    /**
     * Returns the batch ID of this batch.
     * @return the batch ID
     */
    String getId();

    /**
     * Returns the element jobflows.
     * @return the element jobflows
     */
    Collection<? extends JobflowInfo> getElements();

    /**
     * Returns an element.
     * @param flowId the target flow ID
     * @return the element, or {@code null} if it is not found
     */
    Optional<? extends JobflowInfo> findElement(String flowId);
}