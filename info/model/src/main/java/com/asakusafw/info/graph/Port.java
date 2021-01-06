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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * An abstract super class of I/O port of {@link Node}.
 * @param <TSelf> the implementation type
 * @param <TOpposite> the opposite port type
 * @since 0.9.2
 */
@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
public abstract class Port<
        TSelf extends Port<TSelf, TOpposite>,
        TOpposite extends Port<TOpposite, TSelf>> extends AbstractElement<TSelf> {

    private final Node parent;

    Port(Node parent) {
        Objects.requireNonNull(parent);
        this.parent = parent;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    /**
     * Connects to the given opposite port.
     * @param opposite the target port
     * @return this
     */
    public TSelf connect(TOpposite opposite) {
        return connect(opposite, null);
    }

    /**
     * Connects to the given opposite port.
     * @param opposite the target port
     * @param configure the wire configurator
     * @return this
     */
    public abstract TSelf connect(TOpposite opposite, Consumer<? super Wire> configure);

    /**
     * Returns the connected wires.
     * @return the connected wires
     */
    public abstract List<Wire> getWires();

    /**
     * Returns the connected opposites.
     * @return the connected opposites
     */
    public abstract List<TOpposite> getOpposites();

    TSelf connect(Output upstream, Input downstream, Consumer<? super Wire> configure) {
        Util.require(upstream.getParent().getParent() != null, () -> String.format(
                "port of the root node must not be connected: %s",
                upstream));
        Util.require(upstream.getParent().getParent() == downstream.getParent().getParent(), () -> String.format(
                "both %s and %s must be on the same node",
                upstream.getParent(),
                downstream.getParent()));
        upstream.getParent().getParent().connect(upstream, downstream).configure(configure);
        return self();
    }

    List<Wire> wires(Function<? super Wire, ? extends TSelf> extractor) {
        Node base = getParent().getParent();
        if (base == null) {
            return Collections.emptyList();
        }
        return base.getWires().stream()
                .filter(wire -> extractor.apply(wire) == this)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList));
    }

    List<TOpposite> opposites(
            Function<? super Wire, ? extends TSelf> self,
            Function<? super Wire, ? extends TOpposite> opposite) {
        Node base = getParent().getParent();
        if (base == null) {
            return Collections.emptyList();
        }
        return base.getWires().stream()
                .filter(wire -> self.apply(wire) == this)
                .map(opposite)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList));
    }
}
