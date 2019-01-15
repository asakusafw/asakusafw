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
package com.asakusafw.info.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
abstract class AbstractElement<TSelf extends AbstractElement<TSelf>> implements Element {

    @SuppressWarnings("unchecked")
    TSelf self() {
        return (TSelf) this;
    }

    abstract ElementId id();

    abstract List<Attribute> attributes();

    /**
     * Configures this object.
     * @param configure the configurator
     * @return this
     */
    public TSelf configure(Consumer<? super TSelf> configure) {
        if (configure != null) {
            configure.accept(self());
        }
        return self();
    }

    @Override
    public List<? extends Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes());
    }

    @Override
    public TSelf withAttribute(Attribute attribute) {
        attributes().add(attribute);
        return self();
    }

    @Override
    public TSelf withAttributes(List<? extends Attribute> attributes) {
        attributes().addAll(attributes);
        return self();
    }

    @Override
    public TSelf withAttributes(Attribute... attributes) {
        return withAttributes(Arrays.asList(attributes));
    }

    @Override
    public String toString() {
        return id().toString();
    }
}
