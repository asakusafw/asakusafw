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
package com.asakusafw.workflow.model;

import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents an executable element.
 * @since 0.10.0
 */
public interface Element {

    /**
     * Returns an attribute of this task.
     * @param <A> the attribute type
     * @param type the attribute type
     * @return an attribute, or {@code empty} if it is not found
     */
    default <A extends Attribute> Optional<A> findAttribute(Class<A> type) {
        return getAttributes(type).findFirst();
    }

    /**
     * Returns attributes of this task.
     * @param <A> the attribute type
     * @param type the attribute type
     * @return attribute stream
     */
    <A extends Attribute> Stream<A> getAttributes(Class<A> type);

    /**
     * Represents an attribute of {@link Element}.
     * @since 0.10.0
     */
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.MINIMAL_CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "class",
            visible = false
    )
    interface Attribute {
        // no special elements
    }
}
