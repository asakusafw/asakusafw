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
package com.asakusafw.info.graph;

import java.util.List;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * An abstract super class of elements on graph.
 * @since 0.9.2
 */
@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
public interface Element {

    /**
     * Returns the parent element of this.
     * @return the parent element, or {@code null} if this is root element
     */
    Element getParent();

    /**
     * Returns the attributes of this element.
     * @return the attributes
     */
    List<? extends Attribute> getAttributes();

    /**
     * Adds an attribute.
     * @param attribute the attribute
     * @return this
     */
    Element withAttribute(Attribute attribute);

    /**
     * Adds attributes.
     * @param attributes the attributes
     * @return this
     */
    Element withAttributes(List<? extends Attribute> attributes);

    /**
     * Adds attributes.
     * @param attributes the attributes
     * @return this
     */
    Element withAttributes(Attribute... attributes);
}
