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
package com.asakusafw.compiler.flow.plan;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.PortConnection;


/**
 * Represents element paths on flow graph.
 */
public class FlowPath {

    private final Direction direction;

    private final Set<FlowElement> startings;

    private final Set<FlowElement> passings;

    private final Set<FlowElement> arrivals;

    /**
     * Creates a new instance.
     * @param direction the path direction
     * @param startings the head elements
     * @param passings the body elements
     * @param arrivals the tail elements
     */
    public FlowPath(
            Direction direction,
            Set<FlowElement> startings, Set<FlowElement> passings, Set<FlowElement> arrivals) {
        Precondition.checkMustNotBeNull(direction, "direction"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(startings, "startings"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(passings, "passings"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arrivals, "arrivals"); //$NON-NLS-1$
        this.direction = direction;
        this.startings = Sets.freeze(startings);
        this.passings = Sets.freeze(passings);
        this.arrivals = Sets.freeze(arrivals);
    }

    /**
     * Returns the path direction.
     * @return the path direction
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * Returns the head elements.
     * @return the head elements
     */
    public Set<FlowElement> getStartings() {
        return this.startings;
    }

    /**
     * Returns the body elements (excludes head and tail elements).
     * @return the head elements
     */
    public Set<FlowElement> getPassings() {
        return this.passings;
    }

    /**
     * Returns the tail elements.
     * @return the tail elements
     */
    public Set<FlowElement> getArrivals() {
        return this.arrivals;
    }

    /**
     * Creates a new {@link FlowBlock} object from this path (must be a forward path).
     * @param graph the original flow graph
     * @param blockSequence the block serial number
     * @param includeStartings {@code true} to include the {@link #getStartings() head elements},
     *     otherwise {@code false}
     * @param includeArrivals {@code true} to include the {@link #getArrivals() tail elements},
     *     otherwise {@code false}
     * @return the created block
     * @throws IllegalStateException if this is not a {@link Direction#FORWARD forward} path
     * @throws IllegalArgumentException if the resulting block will be empty, or the parameters are {@code null}
     */
    public FlowBlock createBlock(
            FlowGraph graph, int blockSequence,
            boolean includeStartings, boolean includeArrivals) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        if (direction != Direction.FORWARD) {
            throw new IllegalStateException("direction must be FORWARD"); //$NON-NLS-1$
        }
        if (includeStartings == false && includeArrivals == false && passings.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Set<FlowElement> elements = createBlockElements(includeStartings, includeArrivals);
        List<PortConnection> inputs = createBlockInputs(includeStartings);
        List<PortConnection> outputs = createBlockOutputs(includeArrivals);
        return new FlowBlock(blockSequence, graph, inputs, outputs, elements);
    }

    private List<PortConnection> createBlockInputs(boolean includeStartings) {
        List<PortConnection> results = new ArrayList<>();
        if (includeStartings) {
            for (FlowElement element : startings) {
                for (FlowElementInput input : element.getInputPorts()) {
                    results.addAll(input.getConnected());
                }
            }
        } else {
            for (FlowElement element : startings) {
                for (FlowElementOutput output : element.getOutputPorts()) {
                    for (PortConnection conn : output.getConnected()) {
                        FlowElement target = conn.getDownstream().getOwner();
                        if (passings.contains(target) || arrivals.contains(target)) {
                            results.add(conn);
                        }
                    }
                }
            }
        }
        return results;
    }

    private List<PortConnection> createBlockOutputs(boolean includeArrivals) {
        List<PortConnection> results = new ArrayList<>();
        if (includeArrivals) {
            for (FlowElement element : arrivals) {
                for (FlowElementOutput output : element.getOutputPorts()) {
                    results.addAll(output.getConnected());
                }
            }
        } else {
            for (FlowElement element : arrivals) {
                for (FlowElementInput input : element.getInputPorts()) {
                    for (PortConnection conn : input.getConnected()) {
                        FlowElement target = conn.getUpstream().getOwner();
                        if (passings.contains(target) || startings.contains(target)) {
                            results.add(conn);
                        }
                    }
                }
            }
        }
        return results;
    }

    private Set<FlowElement> createBlockElements(boolean includeStartings,
            boolean includeArrivals) {
        Set<FlowElement> elements = new HashSet<>();
        elements.addAll(passings);
        if (includeStartings) {
            elements.addAll(startings);
        }
        if (includeArrivals) {
            elements.addAll(arrivals);
        }
        return elements;
    }

    /**
     * Returns a new union path consists of this and the specified path.
     * @param other the target path
     * @return the created path
     * @throws IllegalArgumentException if the two paths have the different directions,
     *     or the parameter is {@code null}
     */
    public FlowPath union(FlowPath other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (this.direction != other.direction) {
            throw new IllegalArgumentException("other must have same direction"); //$NON-NLS-1$
        }
        Set<FlowElement> newStartings = Sets.from(startings);
        newStartings.addAll(other.startings);

        Set<FlowElement> newPassings = Sets.from(passings);
        newPassings.addAll(other.passings);

        Set<FlowElement> newArrivals = Sets.from(arrivals);
        newArrivals.addAll(other.arrivals);

        return new FlowPath(
                direction,
                newStartings,
                newPassings,
                newArrivals);
    }

    /**
     * Returns a new intersect path which consists of this path and the transposed specified path.
     * @param other the target path
     * @return the created path
     * @throws IllegalArgumentException if the two paths have the different directions,
     *     or the parameter is {@code null}
     */
    public FlowPath transposeIntersect(FlowPath other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (this.direction == other.direction) {
            throw new IllegalArgumentException("other must have different direction"); //$NON-NLS-1$
        }
        Set<FlowElement> newStartings = Sets.from(startings);
        newStartings.retainAll(other.arrivals);

        Set<FlowElement> newPassings = Sets.from(passings);
        newPassings.retainAll(other.passings);

        Set<FlowElement> newArrivals = Sets.from(arrivals);
        newArrivals.retainAll(other.startings);

        return new FlowPath(
                direction,
                newStartings,
                newPassings,
                newArrivals);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}: {1}->{2}", //$NON-NLS-1$
                direction,
                startings,
                arrivals);
    }

    /**
     * Represents directions of {@link FlowPath}.
     */
    public enum Direction {

        /**
         * The forward direction.
         */
        FORWARD,

        /**
         * The backward direction.
         */
        BACKWORD,
    }
}
