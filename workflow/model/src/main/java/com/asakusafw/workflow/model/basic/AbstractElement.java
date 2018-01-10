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
package com.asakusafw.workflow.model.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.asakusafw.workflow.model.Element;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An abstract implementation of {@link Element}.
 * @since 0.10.0
 */
public abstract class AbstractElement implements Element {

    @JsonProperty("attributes")
    @JsonInclude(Include.NON_EMPTY)
    private final List<Attribute> attributes = new ArrayList<>();

    @Override
    public <A extends Attribute> Stream<A> getAttributes(Class<A> type) {
        return attributes.stream()
                .filter(type::isInstance)
                .map(type::cast);
    }

    /**
     * Adds an attribute.
     * @param attribute the attribute
     */
    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    /**
     * Sets the attributes.
     * @param attributes the attributes
     */
    protected void setAttributes(Collection<? extends Attribute> attributes) {
        this.attributes.clear();
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
    }
}
