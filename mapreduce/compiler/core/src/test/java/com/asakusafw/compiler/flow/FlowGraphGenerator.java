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
package com.asakusafw.compiler.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.compiler.flow.plan.FlowPath;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.graph.PortDirection;
import com.asakusafw.vocabulary.flow.util.PseudElementDescription;
import com.asakusafw.vocabulary.operator.Identity;

/**
 * Flow graph generator for testing.
 */
public class FlowGraphGenerator {

    private static final Class<String> TYPE = String.class;

    private final List<FlowIn<?>> flowInputs = new ArrayList<>();

    private final List<FlowOut<?>> flowOutputs = new ArrayList<>();

    private final Map<String, FlowElement> elements = new HashMap<>();

    /**
     * Adds an input.
     * @param name the element name
     * @return the added element
     */
    public FlowElement defineInput(String name) {
        InputDescription desc = new InputDescription(name, TYPE);
        FlowIn<?> node = new FlowIn<>(desc);
        flowInputs.add(node);
        return register(name, node.getFlowElement());
    }

    /**
     * Adds an output.
     * @param name the element name
     * @return the added element
     */
    public FlowElement defineOutput(String name) {
        OutputDescription desc = new OutputDescription(name, TYPE);
        FlowOut<?> node = new FlowOut<>(desc);
        flowOutputs.add(node);
        return register(name, node.getFlowElement());
    }

    /**
     * Adds an operator.
     * @param name the element name
     * @param inputList the space separated input port names
     * @param outputList the space separated output port names
     * @param attributes the attributes
     * @return the added element
     */
    public FlowElement defineOperator(
            String name,
            String inputList,
            String outputList,
            FlowElementAttribute... attributes) {
        Class<String> type = TYPE;
        return defineOperator(type, name, inputList, outputList, attributes);
    }

    /**
     * Defines a new operator and registers into this generator.
     * @param type target operator type
     * @param name target name
     * @param inputList target input names
     * @param outputList target output names
     * @param attributes operator attributes
     * @return the defined element
     */
    public FlowElement defineOperator(
            Class<?> type, String name,
            String inputList, String outputList,
            FlowElementAttribute... attributes) {
        List<FlowElementPortDescription> inputs = parsePorts(PortDirection.INPUT, inputList);
        List<FlowElementPortDescription> outputs = parsePorts(PortDirection.OUTPUT, outputList);
        FlowElementDescription desc = new OperatorDescription(
                new OperatorDescription.Declaration(
                        Identity.class,
                        type,
                        type,
                        name,
                        Collections.emptyList()),
                inputs,
                outputs,
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(attributes));
        return register(name, desc);
    }

    /**
     * Adds an flow-part.
     * @param name the element name
     * @param graph the flow graph
     * @return the added element
     */
    public FlowElement defineFlowPart(
            String name,
            FlowGraph graph) {
        FlowElementDescription desc = new FlowPartDescription(graph);
        return register(name, desc);
    }

    /**
     * Adds an empty operator.
     * @param name the element name
     * @return the added element
     */
    public FlowElement defineEmpty(String name) {
        return register(name, new PseudElementDescription(
                name,
                TYPE,
                false,
                true,
                FlowBoundary.STAGE));
    }

    /**
     * Adds .
     * @param name the element name
     * @return the added element
     */
    public FlowElement defineStop(String name) {
        return register(name, new PseudElementDescription(
                name,
                TYPE,
                true,
                false,
                FlowBoundary.STAGE));
    }

    /**
     * Adds a pseudo-element.
     * @param name the element name
     * @param attributes the attributes
     * @return the added element
     */
    public FlowElement definePseud(
            String name,
            FlowElementAttribute... attributes) {
        return register(name, new PseudElementDescription(
                name,
                TYPE,
                true,
                true,
                attributes));
    }

    /**
     * Connects ports.
     * The port should be described as {@code "element-name.port-name"}, or
     * {@code "element-name"} for single port elements.
     * @param upstream the upstream
     * @param downstream the downstream
     * @return this
     */
    public FlowGraphGenerator connect(String upstream, String downstream) {
        FlowElementOutput output = findOutput(upstream);
        FlowElementInput input = findInput(downstream);
        PortConnection.connect(output, input);
        return this;
    }

    /**
     * Returns an element.
     * @param name the target name
     * @return the target element
     */
    public FlowElement get(String name) {
        FlowElement found = elements.get(name);
        if (found == null) {
            throw new AssertionError(name + elements.keySet());
        }
        return found;
    }

    /**
     * Returns an element description.
     * @param name the target name
     * @return the target element
     */
    public FlowElementDescription desc(String name) {
        FlowElement found = elements.get(name);
        if (found == null) {
            throw new AssertionError(name + elements.keySet());
        }
        return found.getDescription();
    }

    /**
     * Returns an input.
     * @param input the input name
     * @return the target port
     */
    public FlowElementInput input(String input) {
        return findInput(input);
    }

    /**
     * Returns inputs.
     * @param inputs the input names
     * @return the target ports
     */
    public Set<FlowElementInput> inputs(String... inputs) {
        Set<FlowElementInput> results = new HashSet<>();
        for (String input : inputs) {
            results.add(input(input));
        }
        return results;
    }

    /**
     * Returns an output.
     * @param output the output name
     * @return the target port
     */
    public FlowElementOutput output(String output) {
        return findOutput(output);
    }

    /**
     * Returns outputs.
     * @param outputs the output names
     * @return the target ports
     */
    public Set<FlowElementOutput> outputs(String... outputs) {
        Set<FlowElementOutput> results = new HashSet<>();
        for (String output : outputs) {
            results.add(output(output));
        }
        return results;
    }

    /**
     * Returns the set of elements.
     * @param names the element names
     * @return the elements
     */
    public Set<FlowElement> getAsSet(String... names) {
        Set<FlowElement> results = new HashSet<>();
        for (String name : names) {
            results.add(get(name));
        }
        return results;
    }

    /**
     * Returns the all elements.
     * @return the all elements
     */
    public Set<FlowElement> all() {
        return new HashSet<>(elements.values());
    }

    /**
     * Returns the flow graph.
     * @return the flow graph
     */
    public FlowGraph toGraph() {
        return new FlowGraph(Testing.class, flowInputs, flowOutputs);
    }

    /**
     * Returns the flow path.
     * @param direction the path direction
     * @return the flow path
     */
    public FlowPath toPath(FlowPath.Direction direction) {
        Set<FlowElement> inputs = new HashSet<>();
        Set<FlowElement> passings = new HashSet<>();
        Set<FlowElement> outputs = new HashSet<>();

        for (FlowIn<?> node : flowInputs) {
            inputs.add(node.getFlowElement());
        }
        for (FlowOut<?> node : flowOutputs) {
            outputs.add(node.getFlowElement());
        }
        passings.removeAll(inputs);
        passings.removeAll(outputs);
        return new FlowPath(
                direction,
                direction == FlowPath.Direction.FORWARD ? inputs : outputs,
                passings,
                direction == FlowPath.Direction.FORWARD ? outputs : inputs);
    }

    private static final Pattern PORT = Pattern.compile("(.+?)(\\.(.+?))?");

    private FlowElementInput findInput(String spec) {
        Matcher matcher = PORT.matcher(spec);
        if (matcher.matches() == false) {
            throw new AssertionError(spec);
        }
        String elementName = matcher.group(1);
        FlowElement element = elements.get(elementName);
        if (element == null) {
            throw new AssertionError(elementName + elements.keySet());
        }

        String portName = matcher.group(3);
        if (portName == null) {
            if (element.getInputPorts().size() != 1) {
                throw new AssertionError(element.getInputPorts());
            }
            return element.getInputPorts().get(0);
        }

        FlowElementInput port = null;
        for (FlowElementInput finding : element.getInputPorts()) {
            if (portName.equals(finding.getDescription().getName())) {
                port = finding;
                break;
            }
        }
        if (port == null) {
            throw new AssertionError(elementName + "." + portName + elements.keySet());

        }
        return port;
    }

    private FlowElementOutput findOutput(String spec) {
        Matcher matcher = PORT.matcher(spec);
        if (matcher.matches() == false) {
            throw new AssertionError(spec);
        }
        String elementName = matcher.group(1);
        FlowElement element = elements.get(elementName);
        if (element == null) {
            throw new AssertionError(elementName + elements.keySet());
        }

        String portName = matcher.group(3);
        if (portName == null) {
            if (element.getOutputPorts().size() != 1) {
                throw new AssertionError(element.getOutputPorts());
            }
            return element.getOutputPorts().get(0);
        }

        FlowElementOutput port = null;
        for (FlowElementOutput finding : element.getOutputPorts()) {
            if (portName.equals(finding.getDescription().getName())) {
                port = finding;
                break;
            }
        }
        if (port == null) {
            throw new AssertionError(elementName + "." + portName + elements.keySet());

        }
        return port;
    }

    private FlowElement register(String name, FlowElementDescription desc) {
        FlowElement element = new FlowElement(desc);
        return register(name, element);
    }

    private FlowElement register(String name, FlowElement element) {
        if (elements.containsKey(name)) {
            throw new AssertionError(name + elements.keySet());
        }
        elements.put(name, element);
        return element;
    }

    private List<FlowElementPortDescription> parsePorts(
            PortDirection direction,
            String nameList) {
        String[] names = nameList.trim().split("\\s+");
        List<FlowElementPortDescription> results = new ArrayList<>();
        for (String name : names) {
            results.add(new FlowElementPortDescription(name, TYPE, direction));
        }
        return results;
    }

    private static class Testing extends FlowDescription {
        @Override
        protected void describe() {
            return;
        }
    }
}
