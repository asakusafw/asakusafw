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
package com.asakusafw.info.operator.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.asakusafw.info.graph.Input;
import com.asakusafw.info.graph.Node;
import com.asakusafw.info.graph.Output;
import com.asakusafw.info.operator.NamedOperatorSpec;
import com.asakusafw.info.operator.OperatorGraphAttribute;
import com.asakusafw.info.operator.OperatorSpec.OperatorKind;
import com.asakusafw.info.plan.PlanAttribute;

/**
 * A view of operator graph.
 * @since 0.9.2
 */
public class OperatorGraphView {

    private final Node root;

    private final Map<Node, OperatorView> vertices = new HashMap<>();

    private final Map<Input, InputView> inputs = new HashMap<>();

    private final Map<Output, OutputView> outputs = new HashMap<>();

    /**
     * Creates a new instance.
     * @param graph the source graph
     */
    public OperatorGraphView(OperatorGraphAttribute graph) {
        this(graph.getRoot());
    }

    /**
     * Creates a new instance.
     * @param plan the source execution plan
     */
    public OperatorGraphView(PlanAttribute plan) {
        this(plan.getRoot());
    }

    OperatorGraphView(Node entity) {
        this.root = entity;
    }

    /**
     * Returns the root node.
     * @return the root node
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Returns the element operators.
     * @return the operators, including inputs and outputs
     */
    public Collection<OperatorView> getOperators() {
        return all().collect(Collectors.toList());
    }

    /**
     * Returns the element operators.
     * @param kind the operator kind
     * @return the operators
     */
    public Collection<OperatorView> getOperators(OperatorKind kind) {
        return all()
                .filter(it -> it.getSpec().getOperatorKind() == kind)
                .collect(Collectors.toList());
    }

    /**
     * Returns the element operators.
     * @param kind the operator kind
     * @return the pairs of name and its operator
     */
    public Map<String, OperatorView> getOperatorMap(OperatorKind kind) {
        return all()
                .filter(it -> it.getSpec().getOperatorKind() == kind)
                .filter(it -> it.getSpec() instanceof NamedOperatorSpec)
                .collect(Collectors.toMap(
                        it -> ((NamedOperatorSpec) it.getSpec()).getName(),
                        Function.identity()));
    }

    private Stream<OperatorView> all() {
        return root.getElements().stream().map(this::resolve);
    }

    OperatorView resolve(Node node) {
        assert node.getParent() == root;
        return vertices.computeIfAbsent(node, it -> new OperatorView(this, it));
    }

    InputView resolve(Input port) {
        assert port.getParent().getParent() == root;
        return inputs.computeIfAbsent(port, it -> new InputView(this, it));
    }

    OutputView resolve(Output port) {
        assert port.getParent().getParent() == root;
        return outputs.computeIfAbsent(port, it -> new OutputView(this, it));
    }
}
