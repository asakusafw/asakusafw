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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.asakusafw.info.Attribute;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Represents a node of graph.
 * @since 0.9.2
 */
@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
public class Node extends AbstractElement<Node> {

    private final Node parent;

    @JsonProperty
    @JsonUnwrapped
    private final Info info;

    private final List<Input> inputs = new ArrayList<>();

    private final List<Output> outputs = new ArrayList<>();

    private final List<Node> elements = new ArrayList<>();

    private final List<Wire> wires = new ArrayList<>();

    /**
     * Creates a new empty instance.
     */
    public Node() {
        this(null, new Info(new Id(0)));
    }

    @JsonCreator(mode = Mode.DELEGATING)
    Node(Info info) {
        this(null, info);
    }

    Node(Node parent, Info info) {
        Objects.requireNonNull(info);
        this.parent = parent;
        this.info = info;
        resolve();
    }

    private void resolve() {
        inputs.addAll(info.inputs.stream().map(it -> new Input(this, it)).collect(Collectors.toList()));
        outputs.addAll(info.outputs.stream().map(it -> new Output(this, it)).collect(Collectors.toList()));
        elements.addAll(info.elements.stream().map(it -> new Node(this, it)).collect(Collectors.toList()));
        wires.addAll(info.wires.stream().map(it -> new Wire(this, it)).collect(Collectors.toList()));
    }

    @Override
    public Node getParent() {
        return parent;
    }

    /**
     * Returns the input ports of this node.
     * @return the input ports
     */
    public List<Input> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    /**
     * Returns the input port of this node.
     * @param index the input index
     * @return the input port
     */
    public Input getInput(int index) {
        Util.require(0 <= index && index < inputs.size(), () -> String.format(
                "index of input must be [0, %,d): %,d", //$NON-NLS-1$
                inputs.size(),
                index));
        return inputs.get(index);
    }

    /**
     * Adds a new input port.
     * @return the created port
     */
    public Input newInput() {
        Input.Info pInfo = new Input.Info(new Input.Id(info.inputs.size()));
        info.inputs.add(pInfo);
        Input result = new Input(this, pInfo);
        inputs.add(result);
        assert inputs.size() == info.inputs.size();
        return result;
    }

    /**
     * Adds a new input port.
     * @param configure configures the created port
     * @return this
     */
    public Node withInput(Consumer<? super Input> configure) {
        newInput().configure(configure);
        return this;
    }

    /**
     * Returns the output ports of this node.
     * @return the output ports
     */
    public List<Output> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    /**
     * Returns the output port of this node.
     * @param index the output index
     * @return the output port
     */
    public Output getOutput(int index) {
        Util.require(0 <= index && index < outputs.size(), () -> String.format(
                "index of output must be [0, %,d): %,d", //$NON-NLS-1$
                outputs.size(),
                index));
        return outputs.get(index);
    }

    /**
     * Adds a new output port.
     * @return the created port
     */
    public Output newOutput() {
        Output.Info pInfo = new Output.Info(new Output.Id(info.outputs.size()));
        info.outputs.add(pInfo);
        Output result = new Output(this, pInfo);
        outputs.add(result);
        assert outputs.size() == info.outputs.size();
        return result;
    }

    /**
     * Adds a new output port.
     * @param configure configures the created port
     * @return this
     */
    public Node withOutput(Consumer<? super Output> configure) {
        newOutput().configure(configure);
        return this;
    }

    /**
     * Returns the element nodes of this node.
     * @return the element nodes
     */
    public List<Node> getElements() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * Returns the element node of this node.
     * @param index the element index
     * @return the element node
     */
    public Node getElement(int index) {
        Util.require(0 <= index && index < elements.size(), () -> String.format(
                "index of element must be [0, %,d): %,d", //$NON-NLS-1$
                elements.size(),
                index));
        return elements.get(index);
    }

    /**
     * Adds a new element node.
     * @return the created port
     */
    public Node newElement() {
        Node.Info pInfo = new Node.Info(new Node.Id(info.elements.size()));
        info.elements.add(pInfo);
        Node result = new Node(this, pInfo);
        elements.add(result);
        assert elements.size() == info.elements.size();
        return result;
    }

    /**
     * Adds a new node port.
     * @param configure configures the created port
     * @return this
     */
    public Node withElement(Consumer<? super Node> configure) {
        newElement().configure(configure);
        return this;
    }

    /**
     * Returns the wires on this node.
     * @return the wires
     * @see Input#connect(Output, Consumer)
     * @see Output#connect(Input, Consumer)
     */
    public List<Wire> getWires() {
        return Collections.unmodifiableList(wires);
    }

    /**
     * Returns the wire on this node.
     * @param index the wire index
     * @return the wire
     */
    public Wire getWire(int index) {
        Util.require(0 <= index && index < wires.size(), () -> String.format(
                "index of wire must be [0, %,d): %,d", //$NON-NLS-1$
                wires.size(),
                index));
        return wires.get(index);
    }

    @Override
    Id id() {
        return info.id;
    }

    @Override
    List<Attribute> attributes() {
        return info.attributes;
    }

    /**
     * Returns the entity (only for testing).
     * @return the entity
     */
    Info info() {
        return info;
    }

    Input resolve(Input.Id id) {
        return inputs.get(id.getValue());
    }

    Output resolve(Output.Id id) {
        return outputs.get(id.getValue());
    }

    Node resolve(Node.Id id) {
        return elements.get(id.getValue());
    }

    Wire resolve(Wire.Id id) {
        return wires.get(id.getValue());
    }

    Wire connect(Output upstream, Input downstream) {
        assert upstream.getParent().getParent() == this;
        assert downstream.getParent().getParent() == this;
        Wire.Id wId = new Wire.Id(info.wires.size());
        Wire.Info wInfo = new Wire.Info(wId,
                upstream.getParent().id(), upstream.id(),
                downstream.getParent().id(), downstream.id());
        info.wires.add(wInfo);
        Wire result = new Wire(this, wInfo);
        wires.add(result);
        assert wires.size() == info.wires.size();
        return result;
    }

    @JsonAutoDetect(
            creatorVisibility = Visibility.NONE,
            fieldVisibility = Visibility.NONE,
            getterVisibility = Visibility.NONE,
            isGetterVisibility = Visibility.NONE,
            setterVisibility = Visibility.NONE
    )
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

        @JsonProperty(Constants.ID_NODE_INPUTS)
        @JsonInclude(Include.NON_EMPTY)
        final List<Input.Info> inputs = new ArrayList<>();

        @JsonProperty(Constants.ID_NODE_OUTPUTS)
        @JsonInclude(Include.NON_EMPTY)
        final List<Output.Info> outputs = new ArrayList<>();

        @JsonProperty(Constants.ID_NODE_WIRES)
        @JsonInclude(Include.NON_EMPTY)
        final List<Wire.Info> wires = new ArrayList<>();

        @JsonProperty(Constants.ID_NODE_ELEMENTS)
        @JsonInclude(Include.NON_EMPTY)
        final List<Node.Info> elements = new ArrayList<>();

        @JsonProperty(Constants.ID_ATTRIBUTES)
        @JsonInclude(Include.NON_EMPTY)
        final List<Attribute> attributes = new ArrayList<>();

        Info(Id id) {
            this.id = id;
        }

        @JsonCreator
        Info(
                @JsonProperty(Constants.ID_ID) Id id,
                @JsonProperty(Constants.ID_NODE_INPUTS) List<Input.Info> inputs,
                @JsonProperty(Constants.ID_NODE_OUTPUTS) List<Output.Info> outputs,
                @JsonProperty(Constants.ID_NODE_WIRES) List<Wire.Info> wires,
                @JsonProperty(Constants.ID_NODE_ELEMENTS) List<Node.Info> elements,
                @JsonProperty(Constants.ID_ATTRIBUTES) List<? extends Attribute> attributes) {
            this.id = id;
            this.inputs.addAll(validate(inputs, it -> it.id));
            this.outputs.addAll(validate(outputs, it -> it.id));
            this.wires.addAll(validate(wires, it -> it.id));
            this.elements.addAll(validate(elements, it -> it.id));
            this.attributes.addAll(Util.normalize(attributes));
        }

        static <T> List<T> validate(List<T> list, Function<T, ElementId> identifier) {
            if (list == null || list.isEmpty()) {
                return Collections.emptyList();
            }
            int index = 0;
            for (T value : list) {
                ElementId id = identifier.apply(value);
                if (id.getValue() != index) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "invalid index {0}: {1}",
                            index, value));
                }
                index++;
            }
            return list;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(id);
            result = prime * result + Objects.hashCode(inputs);
            result = prime * result + Objects.hashCode(outputs);
            result = prime * result + Objects.hashCode(wires);
            result = prime * result + Objects.hashCode(elements);
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
                    && Objects.equals(inputs, other.inputs)
                    && Objects.equals(outputs, other.outputs)
                    && Objects.equals(wires, other.wires)
                    && Objects.equals(elements, other.elements)
                    && Objects.equals(attributes, other.attributes);
        }
    }
}
