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
package com.asakusafw.info.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an output of node.
 * @since 0.9.2
 */
@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
public class Output extends Port<Output, Input> {

    private final Info info;

    Output(Node parent, Info info) {
        super(parent);
        Objects.requireNonNull(info);
        this.info = info;
    }

    @Override
    public Output connect(Input opposite, Consumer<? super Wire> configure) {
        return connect(this, opposite, configure);
    }

    @Override
    public List<Wire> getWires() {
        return wires(Wire::getSource);
    }

    @Override
    public List<Input> getOpposites() {
        return opposites(Wire::getSource, Wire::getDestination);
    }

    @Override
    Id id() {
        return info.id;
    }

    @Override
    List<Attribute> attributes() {
        return info.attributes;
    }

    static class Id extends ElementId {
        @JsonCreator
        Id(int value) {
            super(value);
        }
    }

    @JsonAutoDetect(
            creatorVisibility = Visibility.NONE,
            fieldVisibility = Visibility.NONE,
            getterVisibility = Visibility.NONE,
            isGetterVisibility = Visibility.NONE,
            setterVisibility = Visibility.NONE
    )
    static class Info {

        @JsonProperty(value = Constants.ID_ID, required = true)
        final Id id;

        @JsonProperty(Constants.ID_ATTRIBUTES)
        @JsonInclude(Include.NON_EMPTY)
        final List<Attribute> attributes = new ArrayList<>();

        Info(Id id) {
            this(id, Collections.emptyList());
        }

        @JsonCreator
        Info(
                @JsonProperty(Constants.ID_ID) Id id,
                @JsonProperty(Constants.ID_ATTRIBUTES) List<? extends Attribute> attributes) {
            this.id = id;
            this.attributes.addAll(Util.normalize(attributes));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(id);
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
            Info other = (Info) obj;
            return Objects.equals(id, other.id)
                    && Objects.equals(attributes, other.attributes);
        }
    }
}
