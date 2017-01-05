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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.util.PseudElementDescription;

/**
 * Utilities about {@link FlowGraph}.
 */
public final class FlowGraphUtil {

    /**
     * Returns all transitive connected elements from inputs/outputs.
     * The returned set will contain inputs/outputs themselves.
     * @param graph the target graph
     * @return the all connected elements
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Set<FlowElement> collectElements(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Set<FlowElement> elements = new HashSet<>();
        for (FlowIn<?> in : graph.getFlowInputs()) {
            elements.add(in.getFlowElement());
        }
        for (FlowOut<?> out : graph.getFlowOutputs()) {
            elements.add(out.getFlowElement());
        }
        collect(elements);
        return elements;
    }

    /**
     * Returns all flow-parts in the target graph.
     * The returned set will only contain the directly appeared flow-parts in the graph, in other words,
     * the all nested flow-parts will not appear in the set.
     * @param graph the target graph
     * @return the flow-parts in the flow graph
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Set<FlowElement> collectFlowParts(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<>();
        for (FlowElement element : collectElements(graph)) {
            FlowElementDescription description = element.getDescription();
            if (description.getKind() == FlowElementKind.FLOW_COMPONENT) {
                results.add(element);
            }
        }
        return results;
    }

    /**
     * Returns the all boundary elements in the target graph.
     * The returned set will only contain the directly appeared elements in the graph, in other words,
     * the elements which is appeared in flow-parts will not be appeared in the set.
     * @param graph the target graph
     * @return the boundary elements in the flow graph
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Set<FlowElement> collectBoundaries(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<>();
        for (FlowElement element : FlowGraphUtil.collectElements(graph)) {
            if (FlowGraphUtil.isBoundary(element)) {
                results.add(element);
            }
        }
        return results;
    }

    /**
     * Returns the element dependency graph of the target flow graph.
     * @param graph the target graph
     * @return the dependency graph
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Graph<FlowElement> toElementGraph(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Graph<FlowElement> results = Graphs.newInstance();
        for (FlowElement source : FlowGraphUtil.collectElements(graph)) {
            results.addEdges(source, FlowGraphUtil.getSuccessors(source));
        }
        return results;
    }

    /**
     * Returns a deep copy of the target graph.
     * @param graph the target graph
     * @return the created copy
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static FlowGraph deepCopy(FlowGraph graph) {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$

        Map<FlowElement, FlowElement> elemMapping = new HashMap<>();
        List<FlowIn<?>> flowInputs = new ArrayList<>();
        for (FlowIn<?> orig : graph.getFlowInputs()) {
            FlowIn<?> copy = FlowIn.newInstance(orig.getDescription());
            elemMapping.put(orig.getFlowElement(), copy.getFlowElement());
            flowInputs.add(copy);
        }
        List<FlowOut<?>> flowOutputs = new ArrayList<>();
        for (FlowOut<?> orig : graph.getFlowOutputs()) {
            FlowOut<?> copy = FlowOut.newInstance(orig.getDescription());
            elemMapping.put(orig.getFlowElement(), copy.getFlowElement());
            flowOutputs.add(copy);
        }
        deepCopy(
                collectElements(graph),
                elemMapping,
                new HashMap<FlowElementInput, FlowElementInput>(),
                new HashMap<FlowElementOutput, FlowElementOutput>());

        FlowGraph copy = new FlowGraph(graph.getDescription(), flowInputs, flowOutputs);
        copy.setOrigin(graph);
        return copy;
    }

    /**
     * Creates a deep copy of the elements and builds its mapping tables.
     * @param elements the target elements
     * @param elementMapping the element mapping table to be built
     * @param inputMapping the input port mapping table to be built
     * @param outputMapping the output port mapping table to be built
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static void deepCopy(
            Set<FlowElement> elements,
            Map<FlowElement, FlowElement> elementMapping,
            Map<FlowElementInput, FlowElementInput> inputMapping,
            Map<FlowElementOutput, FlowElementOutput> outputMapping) {
        Precondition.checkMustNotBeNull(elements, "elements"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(elementMapping, "elementMapping"); //$NON-NLS-1$
        // builds element mappings
        for (FlowElement orig : elements) {
            FlowElement copy = createMapping(elementMapping, orig);
            addMapping(inputMapping, orig.getInputPorts(), copy.getInputPorts());
            addMapping(outputMapping, orig.getOutputPorts(), copy.getOutputPorts());
        }
        // restore port connections
        for (Map.Entry<FlowElementInput, FlowElementInput> entry : inputMapping.entrySet()) {
            FlowElementInput origIn = entry.getKey();
            FlowElementInput copyIn = entry.getValue();
            for (FlowElementOutput origOut : origIn.getOpposites()) {
                if (elements.contains(origOut.getOwner()) == false) {
                    continue;
                }
                FlowElementOutput copyOut = outputMapping.get(origOut);
                assert copyOut != null;
                PortConnection.connect(copyOut, copyIn);
            }
        }
    }

    private static FlowElement createMapping(
            Map<FlowElement, FlowElement> elemMapping,
            FlowElement orig) {
        assert elemMapping != null;
        assert orig != null;
        FlowElement mapped = elemMapping.get(orig);
        if (mapped != null) {
            return mapped;
        }
        FlowElement copy;
        FlowElementDescription description = orig.getDescription();
        if (description.getKind() == FlowElementKind.FLOW_COMPONENT) {
            FlowPartDescription fcd = (FlowPartDescription) description;
            FlowGraph subgraph = deepCopy(fcd.getFlowGraph());
            FlowPartDescription partCopy = new FlowPartDescription(subgraph);
            copy = new FlowElement(partCopy, orig.getAttributeOverride());
        } else {
            copy = orig.copy();
        }
        elemMapping.put(orig, copy);
        return copy;
    }

    private static <T> void addMapping(
            Map<T, T> mapping,
            List<T> source,
            List<T> target) {
        assert mapping != null;
        assert source != null;
        assert target != null;
        assert source.size() == target.size();
        Iterator<T> sIter = source.iterator();
        Iterator<T> tIter = target.iterator();
        while (sIter.hasNext()) {
            assert tIter.hasNext();
            T s = sIter.next();
            T t = tIter.next();
            assert mapping.containsKey(s) == false;
            mapping.put(s, t);
        }
        assert tIter.hasNext() == false;
    }

    private static void collect(Set<FlowElement> collected) {
        assert collected != null;
        LinkedList<FlowElement> work = new LinkedList<>(collected);
        while (work.isEmpty() == false) {
            FlowElement first = work.removeFirst();
            if (collected.contains(first) == false) {
                collected.add(first);
            }
            for (FlowElement pred : FlowGraphUtil.getPredecessors(first)) {
                if (collected.contains(pred) == false) {
                    work.add(pred);
                }
            }
            for (FlowElement succ : FlowGraphUtil.getSuccessors(first)) {
                if (collected.contains(succ) == false) {
                    work.add(succ);
                }
            }
        }
    }

    /**
     * Returns whether the element has any mandatory side effects or not.
     * @param element the target element
     * @return {@code true} if the element has any mandatory side effects, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean hasMandatorySideEffect(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        ObservationCount count = element.getAttribute(ObservationCount.class);
        if (count == null) {
            return false;
        }
        return count.atLeastOnce;
    }

    /**
     * Returns whether the element has any global side effects or not.
     * @param element the target element
     * @return {@code true} if the element has any global side effects, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean hasGlobalSideEffect(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        ObservationCount count = element.getAttribute(ObservationCount.class);
        if (count == null) {
            return false;
        }
        return count.atMostOnce;
    }

    /**
     * Returns whether the element has always empty inputs (and some inputs are connected) or not.
     * @param element the target element
     * @return {@code true} if the element always has empty inputs, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isAlwaysEmpty(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        List<FlowElementInput> ports = element.getInputPorts();
        if (ports.isEmpty()) {
            return false;
        }
        for (FlowElementInput input : ports) {
            if (input.getConnected().isEmpty() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the element always has empty outputs (and some outputs are connected) or not.
     * @param element the target element
     * @return {@code true} if the element always has empty outputs, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isAlwaysStop(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        List<FlowElementOutput> ports = element.getOutputPorts();
        if (ports.isEmpty()) {
            return false;
        }
        for (FlowElementOutput output : ports) {
            if (output.getConnected().isEmpty() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the element is pseudo-element without any boundaries or not.
     * @param element the target element
     * @return {@code true} if the element is pseudo-element without any boundaries, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isIdentity(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        FlowElementDescription description = element.getDescription();
        return isBoundary(element) == false
                && description.getKind() == FlowElementKind.PSEUD
                && element.getInputPorts().size() == 1
                && element.getOutputPorts().size() == 1;
    }

    /**
     * Splits the target pseudo-element only if its inputs or outputs are connected to two or more elements.
     * Each split element will also be a pseudo-element and have at most one opposite.
     * If the target pseudo-element already has one upstream and downstream opposites, this does not nothing.
     * @param element the target element
     * @return {@code true} if this method was actually performed, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is not a pseudo-element
     */
    public static boolean splitIdentity(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        if (isIdentity(element) == false) {
            throw new IllegalArgumentException("element must be identity operator"); //$NON-NLS-1$
        }
        assert element.getInputPorts().size() == 1;
        assert element.getOutputPorts().size() == 1;
        FlowElementInput input = element.getInputPorts().get(0);
        FlowElementOutput output = element.getOutputPorts().get(0);
        Set<PortConnection> sources = Sets.from(input.getConnected());
        Set<PortConnection> targets = Sets.from(output.getConnected());
        if (sources.size() <= 1 && targets.size() <= 1) {
            return false;
        } else {
            for (PortConnection source : sources) {
                FlowElementOutput upstream = source.getUpstream();
                for (PortConnection target : targets) {
                    FlowElementInput downstream = target.getDownstream();
                    connectWithIdentity(element, upstream, downstream);
                }
            }
            disconnect(element);
            return true;
        }
    }

    /**
     * Attaches a stop operator into the target port.
     * @param output the target port
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static void stop(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        FlowElementDescription desc = new PseudElementDescription(
                "implicit-stop", //$NON-NLS-1$
                output.getDescription().getDataType(),
                true,
                false,
                FlowBoundary.STAGE);
        FlowElementResolver resolver = new FlowElementResolver(desc);
        FlowElementInput stopIn = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
        PortConnection.connect(output, stopIn);
    }

    /**
     * Reconnects the upstream outputs of the target element into the downstream inputs of the element.
     * Finally the target element will have no opposites.
     * @param element the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static void skip(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        List<FlowElementOutput> sources = new ArrayList<>();
        for (FlowElementInput input : element.getInputPorts()) {
            sources.addAll(input.disconnectAll());
        }
        List<FlowElementInput> targets = new ArrayList<>();
        for (FlowElementOutput output : element.getOutputPorts()) {
            targets.addAll(output.disconnectAll());
        }
        for (FlowElementOutput upstream : sources) {
            for (FlowElementInput downstream : targets) {
                PortConnection.connect(upstream, downstream);
            }
        }
    }

    /**
     * Returns whether the target element is a boundary element or not.
     * @param element the target element
     * @return {@code true} if the target element is a boundary element, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isBoundary(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return isStageBoundary(element) || isShuffleBoundary(element);
    }

    /**
     * Returns whether the target element is a shuffle boundary or not.
     * @param element the target element
     * @return {@code true} if the target element is a shuffle boundary, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isShuffleBoundary(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return element.getAttribute(FlowBoundary.class) == FlowBoundary.SHUFFLE;
    }

    /**
     * Returns whether the target element is a stage boundary or not.
     * @param element the target element
     * @return {@code true} if the target element is a stage boundary, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isStageBoundary(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return element.getAttribute(FlowBoundary.class) == FlowBoundary.STAGE;
    }

    /**
     * Returns whether the target element is a stage boundary and pseudo-element or not.
     * @param element the target element
     * @return {@code true} if the target element is a stage boundary and pseudo-element, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean isStagePadding(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return isStageBoundary(element) && element.getDescription().getKind() == FlowElementKind.PSEUD;
    }

    /**
     * Returns a forward path from the target element.
     * @param element the target element
     * @return the forward path
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static FlowPath getSucceedBoundaryPath(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> startings = new HashSet<>();
        startings.add(element);
        return getSuccessBoundaryPath(startings);
    }

    private static FlowPath getSuccessBoundaryPath(Set<FlowElement> startings) {
        assert startings != null;
        Set<FlowElement> passings = new HashSet<>();
        Set<FlowElement> arrivals = new HashSet<>();
        Set<FlowElement> saw = new HashSet<>();

        LinkedList<FlowElement> successors = new LinkedList<>();
        for (FlowElement starting : startings) {
            addSuccessors(successors, starting);
        }
        while (successors.isEmpty() == false) {
            FlowElement successor = successors.removeFirst();
            if (saw.contains(successor)) {
                continue;
            }
            saw.add(successor);

            if (isBoundary(successor)) {
                arrivals.add(successor);
            } else {
                passings.add(successor);
                addSuccessors(successors, successor);
            }
        }
        return new FlowPath(
                FlowPath.Direction.FORWARD,
                startings,
                passings,
                arrivals);
    }

    /**
     * Returns a backward path from the target element.
     * @param element the target element
     * @return the created path
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static FlowPath getPredeceaseBoundaryPath(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> startings = new HashSet<>();
        startings.add(element);

        return getPredeceaseBoundaryPath(startings);
    }

    private static FlowPath getPredeceaseBoundaryPath(Set<FlowElement> startings) {
        assert startings != null;
        Set<FlowElement> passings = new HashSet<>();
        Set<FlowElement> arrivals = new HashSet<>();
        Set<FlowElement> saw = new HashSet<>();

        LinkedList<FlowElement> predecessors = new LinkedList<>();
        for (FlowElement starting : startings) {
            addPredecessors(predecessors, starting);
        }
        while (predecessors.isEmpty() == false) {
            FlowElement predecessor = predecessors.removeFirst();
            if (saw.contains(predecessor)) {
                continue;
            }
            saw.add(predecessor);

            if (isBoundary(predecessor)) {
                arrivals.add(predecessor);
            } else {
                passings.add(predecessor);
                addPredecessors(predecessors, predecessor);
            }
        }
        return new FlowPath(
                FlowPath.Direction.BACKWORD,
                startings,
                passings,
                arrivals);
    }

    /**
     * Returns a union of the paths.
     * @param paths the target paths
     * @return the union
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static FlowPath union(Collection<FlowPath> paths) {
        Precondition.checkMustNotBeNull(paths, "paths"); //$NON-NLS-1$
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("paths must not be empty"); //$NON-NLS-1$
        }
        Iterator<FlowPath> iter = paths.iterator();
        assert iter.hasNext();

        FlowPath left = iter.next();
        while (iter.hasNext()) {
            FlowPath right = iter.next();
            left = left.union(right);
        }
        return left;
    }

    /**
     * Returns whether the element has any successors or not.
     * @param element the target element
     * @return {@code true} if the element has any successors, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean hasSuccessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        for (FlowElementOutput output : element.getOutputPorts()) {
            if (output.getConnected().isEmpty() == false) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the element has any predecessors or not.
     * @param element the target element
     * @return {@code true} if the element has any predecessors, otherwise {@code false}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static boolean hasPredecessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        for (FlowElementInput input : element.getInputPorts()) {
            if (input.getConnected().isEmpty() == false) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the all direct successors of the element.
     * @param element the target element
     * @return the all direct successors
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Set<FlowElement> getSuccessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<>();
        addSuccessors(results, element);
        return results;
    }

    private static void addSuccessors(
            Collection<FlowElement> target,
            FlowElement element) {
        assert target != null;
        assert element != null;
        for (FlowElementOutput output : element.getOutputPorts()) {
            for (FlowElementInput opposite : output.getOpposites()) {
                target.add(opposite.getOwner());
            }
        }
    }

    /**
     * Returns the all direct predecessors of the element.
     * @param element the target element
     * @return the all direct predecessors
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Set<FlowElement> getPredecessors(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Set<FlowElement> results = new HashSet<>();
        addPredecessors(results, element);
        return results;
    }

    private static void addPredecessors(
            Collection<FlowElement> target,
            FlowElement element) {
        assert target != null;
        assert element != null;
        for (FlowElementInput input : element.getInputPorts()) {
            for (FlowElementOutput opposite : input.getOpposites()) {
                target.add(opposite.getOwner());
            }
        }
    }

    /**
     * Returns the forward nearest boundary elements of the target output port.
     * @param output the target output port
     * @return the forward nearest boundary elements
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Set<FlowElement> getSucceedingBoundaries(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        LinkedList<FlowElement> nextSuccessors = new LinkedList<>();
        for (FlowElementInput next : output.getOpposites()) {
            nextSuccessors.add(next.getOwner());
        }
        if (nextSuccessors.isEmpty()) {
            return Collections.emptySet();
        }

        Set<FlowElement> saw = new HashSet<>();
        Set<FlowElement> results = new HashSet<>();
        while (nextSuccessors.isEmpty() == false) {
            FlowElement successor = nextSuccessors.removeFirst();
            if (saw.contains(successor)) {
                continue;
            }
            saw.add(successor);

            if (isBoundary(successor)) {
                results.add(successor);
            } else {
                addSuccessors(nextSuccessors, successor);
            }
        }
        return results;
    }

    /**
     * Returns the forward nearest connections the target connection.
     * @param start the target connection
     * @param connections the succeeding connections
     * @return the nearest connections for the succeeding them
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Set<PortConnection> getSucceedingConnections(
            PortConnection start,
            Set<PortConnection> connections) {
        Precondition.checkMustNotBeNull(start, "start"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(connections, "connections"); //$NON-NLS-1$
        LinkedList<PortConnection> next = new LinkedList<>();
        next.add(start);
        Set<PortConnection> results = new HashSet<>();
        while (next.isEmpty() == false) {
            PortConnection successor = next.removeFirst();
            if (connections.contains(successor)) {
                results.add(successor);
            } else {
                FlowElementInput nextInput = successor.getDownstream();
                for (FlowElementOutput output : nextInput.getOwner().getOutputPorts()) {
                    next.addAll(output.getConnected());
                }
            }
        }
        return results;
    }

    /**
     * Flattens the target flow-part.
     * @param element the target element
     * @param attributes the attributes for the padding elements
     * @throws IllegalArgumentException if the element is not a flow-part, or the parameters are {@code null}
     */
    public static void inlineFlowPart(FlowElement element, FlowElementAttribute... attributes) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        FlowElementDescription description = element.getDescription();
        if (description.getKind() != FlowElementKind.FLOW_COMPONENT) {
            throw new IllegalArgumentException("element must be a flow component"); //$NON-NLS-1$
        }

        FlowPartDescription component = (FlowPartDescription) description;
        FlowGraph graph = component.getFlowGraph();

        // connects between out-side and in-side inputs.
        List<FlowElementInput> externalInputs = element.getInputPorts();
        List<FlowElementOutput> internalInputs = new ArrayList<>();
        for (FlowIn<?> fin : graph.getFlowInputs()) {
            internalInputs.add(fin.toOutputPort());
        }
        bypass(externalInputs, internalInputs, attributes);

        // connects between out-side and in-side outputs.
        List<FlowElementOutput> externalOutputs = element.getOutputPorts();
        List<FlowElementInput> internalOutputs = new ArrayList<>();
        for (FlowOut<?> fout : graph.getFlowOutputs()) {
            internalOutputs.add(fout.toInputPort());
        }
        bypass(internalOutputs, externalOutputs, attributes);

        // make the flow-part orphaned
        for (FlowIn<?> fin : graph.getFlowInputs()) {
            disconnect(fin.getFlowElement());
        }
        for (FlowOut<?> fout : graph.getFlowOutputs()) {
            disconnect(fout.getFlowElement());
        }
        disconnect(element);
    }

    private static void bypass(
            List<FlowElementInput> inputs,
            List<FlowElementOutput> outputs,
            FlowElementAttribute... attributes) {
        assert inputs != null;
        assert outputs != null;
        if (inputs.size() != outputs.size()) {
            throw new IllegalArgumentException();
        }
        Iterator<FlowElementInput> inputIterator = inputs.iterator();
        Iterator<FlowElementOutput> outputIterator = outputs.iterator();
        while (inputIterator.hasNext()) {
            assert outputIterator.hasNext();
            FlowElementInput input = inputIterator.next();
            FlowElementOutput output = outputIterator.next();
            bypass(input, output, attributes);
        }
        assert outputIterator.hasNext() == false;
    }

    private static void bypass(
            FlowElementInput input,
            FlowElementOutput output,
            FlowElementAttribute...attributes) {
        assert input != null;
        assert output != null;
        assert attributes != null;

        Collection<FlowElementOutput> upstreams = input.disconnectAll();
        Collection<FlowElementInput> downstreams = output.disconnectAll();
        for (FlowElementOutput upstream : upstreams) {
            for (FlowElementInput downstream : downstreams) {
                if (attributes.length >= 1) {
                    FlowElementDescription desc = new PseudElementDescription(
                            "bypass", //$NON-NLS-1$
                            output.getDescription().getDataType(),
                            true,
                            true,
                            attributes);
                    FlowElementResolver resolver = new FlowElementResolver(desc);
                    FlowElementInput bypassIn = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
                    FlowElementOutput bypassOut = resolver.getOutput(PseudElementDescription.OUTPUT_PORT_NAME);
                    PortConnection.connect(upstream, bypassIn);
                    PortConnection.connect(bypassOut, downstream);
                } else {
                    PortConnection.connect(upstream, downstream);
                }
            }
        }
    }

    private static void connectWithIdentity(
            FlowElement element,
            FlowElementOutput upstream,
            FlowElementInput downstream) {
        assert element != null;
        assert upstream != null;
        assert downstream != null;
        assert element.getDescription().getKind() == FlowElementKind.PSEUD;
        FlowElementResolver resolver = new FlowElementResolver(element.copy());
        FlowElementInput input = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
        FlowElementOutput output = resolver.getOutput(PseudElementDescription.OUTPUT_PORT_NAME);
        PortConnection.connect(upstream, input);
        PortConnection.connect(output, downstream);
    }

    /**
     * Disconnects all connections of the element.
     * @param element the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static void disconnect(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        for (FlowElementInput input : element.getInputPorts()) {
            input.disconnectAll();
        }
        for (FlowElementOutput output : element.getOutputPorts()) {
            output.disconnectAll();
        }
    }

    /**
     * Inserts a checkpoint operator after the target output port.
     * @param output the target output port
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static void insertCheckpoint(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        insertElement(output, "implicit-checkpoint", FlowBoundary.STAGE); //$NON-NLS-1$
    }

    /**
     * Inserts an  identity operator after the target output port.
     * @param output the target output port
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static void insertIdentity(FlowElementOutput output) {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        insertElement(output, "padding"); //$NON-NLS-1$
    }

    private static void insertElement(
            FlowElementOutput output,
            String name,
            FlowElementAttribute... attributes) {
        assert output != null;
        assert name != null;
        assert attributes != null;

        Collection<FlowElementInput> originalDownstreams = output.disconnectAll();

        FlowElementDescription desc = new PseudElementDescription(
                name,
                output.getDescription().getDataType(),
                true,
                true,
                attributes);
        FlowElementResolver resolver = new FlowElementResolver(desc);

        FlowElementInput insertIn = resolver.getInput(PseudElementDescription.INPUT_PORT_NAME);
        PortConnection.connect(output, insertIn);

        FlowElementOutput insertOut = resolver.getOutput(PseudElementDescription.OUTPUT_PORT_NAME);
        for (FlowElementInput downstream : originalDownstreams) {
            PortConnection.connect(insertOut, downstream);
        }
    }

    private FlowGraphUtil() {
        return;
    }
}
