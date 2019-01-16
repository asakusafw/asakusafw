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
package com.asakusafw.workflow.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represents a jobflow.
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
public interface JobflowInfo extends GraphElement<JobflowInfo> {

    /**
     * The flow ID of this jobflow.
     * @return the flow ID
     */
    String getId();

    /**
     * Returns tasks of this jobflow.
     * @param phase the target phase
     * @return the tasks, or an empty set if it is not found
     */
    Collection<? extends TaskInfo> getTasks(TaskInfo.Phase phase);
}