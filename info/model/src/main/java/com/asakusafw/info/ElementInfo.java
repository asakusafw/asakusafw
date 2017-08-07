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

import java.util.List;
import java.util.Optional;

/**
 * Represents a DSL element.
 * @since 0.9.1
 */
public interface ElementInfo {

    /**
     * Returns the element ID.
     * @return the element ID
     */
    String getId();

    /**
     * Returns the description class name.
     * @return the description class name, or {@code null} if it is not sure
     */
    String getDescriptionClass();

    /**
     * Returns the attributes of this element.
     * @return the attributes
     */
    List<? extends Attribute> getAttributes();

    /**
     * Returns an attribute of this elements.
     * @param <T> the attribute type
     * @param type the attribute type
     * @return the attribute, or empty if it is not found
     * @since 0.9.2
     */
    default <T extends Attribute> Optional<T> findAttribute(Class<T> type) {
        return getAttributes().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }
}
