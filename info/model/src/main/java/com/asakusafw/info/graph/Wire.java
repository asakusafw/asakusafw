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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a wire that connects between {@link Input} and {@link Output}.
 * @since 0.9.2
 */
@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
public class Wire extends AbstractElement<Wire> {

    private final Node parent;

    private final Info info;

    Wire(Node parent, Wire.Info info) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(info);
        this.parent = parent;
        this.info = info;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    /**
     * Returns the source output.
     * @return the source output
     */
    public Output getSource() {
        return parent.resolve(info.upstreamNodeId).resolve(info.upstreamPortId);
    }

    /**
     * Returns the destination input.
     * @return the destination input
     */
    public Input getDestination() {
        return parent.resolve(info.downstreamNodeId).resolve(info.downstreamPortId);
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

        private static final int INDEX_NODE = 0;

        private static final int INDEX_PORT = 1;

        @JsonProperty(value = Constants.ID_ID, required = true)
        final Id id;

        final Node.Id upstreamNodeId;

        final Output.Id upstreamPortId;

        final Node.Id downstreamNodeId;

        final Input.Id downstreamPortId;

        @JsonProperty(Constants.ID_ATTRIBUTES)
        @JsonInclude(Include.NON_EMPTY)
        final List<Attribute> attributes = new ArrayList<>();

        Info(Wire.Id id,
                Node.Id upstreamNodeId, Output.Id upstreamPortId,
                Node.Id downstreamNodeId, Input.Id downstreamPortId) {
            this.id = id;
            this.upstreamNodeId = upstreamNodeId;
            this.upstreamPortId = upstreamPortId;
            this.downstreamNodeId = downstreamNodeId;
            this.downstreamPortId = downstreamPortId;
        }

        @JsonCreator
        Info(@JsonProperty(Constants.ID_ID) Wire.Id id,
                @JsonProperty(Constants.ID_WIRE_UPSTREAM) int[] from,
                @JsonProperty(Constants.ID_WIRE_DOWNSTREAM) int[] to,
                @JsonProperty(Constants.ID_ATTRIBUTES) List<? extends Attribute> attributes) {
            this(id,
                    new Node.Id(from[INDEX_NODE]),
                    new Output.Id(from[INDEX_PORT]),
                    new Node.Id(to[INDEX_NODE]),
                    new Input.Id(to[INDEX_PORT]));
            this.attributes.addAll(Util.normalize(attributes));
        }

        @JsonProperty(value = Constants.ID_WIRE_UPSTREAM, required = true)
        int[] getFrom() {
            return new int[] { upstreamNodeId.getValue(), upstreamPortId.getValue() };
        }

        @JsonProperty(value = Constants.ID_WIRE_DOWNSTREAM, required = true)
        int[] getTo() {
            return new int[] { downstreamNodeId.getValue(), downstreamPortId.getValue() };
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(id);
            result = prime * result + Objects.hashCode(upstreamNodeId);
            result = prime * result + Objects.hashCode(upstreamPortId);
            result = prime * result + Objects.hashCode(downstreamNodeId);
            result = prime * result + Objects.hashCode(downstreamPortId);
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
                    && Objects.equals(upstreamNodeId, other.upstreamNodeId)
                    && Objects.equals(upstreamPortId, other.upstreamPortId)
                    && Objects.equals(downstreamNodeId, other.downstreamNodeId)
                    && Objects.equals(downstreamPortId, other.downstreamPortId)
                    && Objects.equals(attributes, other.attributes);
        }
    }
}
