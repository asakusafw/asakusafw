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
package com.asakusafw.info.cli.draw;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.asakusafw.info.graph.Element;
import com.asakusafw.info.graph.Input;
import com.asakusafw.info.graph.Node;
import com.asakusafw.info.graph.Output;
import com.asakusafw.info.graph.Port;

class Drawer {

    private final Map<Element, Draw> elements = new HashMap<>();

    private final List<Connect> connections = new ArrayList<>();

    private int nodeCount = 0;

    Drawer add(Node node, Shape shape, List<String> label) {
        if (elements.containsKey(node)) {
            throw new IllegalStateException();
        }
        Shape s = shape;
        if (s == Shape.GRAPH && node.getElements().isEmpty()) {
            s = Shape.BOX;
        }
        elements.put(node, new Draw(new Id(nodeCount++), s, label, null));
        return this;
    }

    Drawer add(Node node, Shape shape, String label) {
        return add(node, shape, Collections.singletonList(label));
    }

    Drawer add(Node node, Shape shape, Optional<String> label) {
        return add(node, shape, label.map(Collections::singletonList).orElse(Collections.emptyList()));
    }

    Drawer add(Input port, String label) {
        return add(port, Collections.singletonList(label));
    }

    Drawer add(Input port, List<String> label) {
        int index = port.getParent().getInputs().indexOf(port);
        return addPort(index, port, label);
    }

    Drawer add(Output port, String label) {
        return add(port, Collections.singletonList(label));
    }

    Drawer add(Output port, List<String> label) {
        int index = port.getParent().getInputs().size() + port.getParent().getOutputs().indexOf(port);
        return addPort(index, port, label);
    }

    Drawer addPort(int index, Port<?, ?> port, List<String> label) {
        if (elements.containsKey(port)) {
            throw new IllegalStateException();
        }
        elements.put(port, new Draw(new Id(index), Shape.MEMBER, label, null));
        return this;
    }

    Drawer redirect(Element from, Element to) {
        if (elements.containsKey(from)) {
            throw new IllegalStateException();
        }
        elements.put(from, new Draw(new Id(-1), Shape.REDIRECT, Collections.emptyList(), to));
        return this;
    }

    Drawer connect(Element from, Element to) {
        connections.add(new Connect(from, to));
        return this;
    }

    void dump(PrintWriter writer, Node root, Map<String, ?> options) {
        validateNode(root);
        Draw draw = elements.get(root);
        if (draw.shape != Shape.GRAPH) {
            throw new IllegalStateException();
        }
        writer.println("digraph {");
        options.forEach((k, v) -> {
            if (v instanceof Map<?, ?>) {
                writer.printf("%s [%n%s];%n", k, ((Map<?, ?>) v).entrySet().stream()
                        .map(it -> String.format("    %s = %s,%n",
                                it.getKey(),
                                literal(String.valueOf(it.getValue()))))
                        .collect(Collectors.joining()));
            } else {
                writer.printf("%s = %s;%n", k, literal(String.valueOf(v)));
            }
        });
        if (draw.label.isEmpty() == false) {
            writer.printf("label = %s;%n", literal(draw.label));
        }
        dumpVertices(writer, root);
        dumpEdges(writer);
        writer.println("}");
    }

    private void validateNode(Node node) {
        Draw draw = elements.get(node);
        if (draw == null) {
            throw new IllegalStateException();
        }
        switch (draw.shape) {
        case GRAPH:
            node.getElements().forEach(this::validateNode);
            node.getInputs().forEach(this::validateNonMember);
            node.getOutputs().forEach(this::validateNonMember);
            break;
        case RECORD:
            node.getInputs().forEach(this::validateMember);
            node.getOutputs().forEach(this::validateMember);
            break;
        case REDIRECT:
        case MEMBER:
            throw new IllegalStateException();
        default:
            node.getInputs().forEach(this::validateNonMember);
            node.getOutputs().forEach(this::validateNonMember);
            break;
        }
    }

    private void validateMember(Port<?, ?> port) {
        Draw draw = elements.get(port);
        if (draw == null || draw.shape != Shape.MEMBER) {
            throw new IllegalStateException();
        }
    }

    private void validateNonMember(Port<?, ?> port) {
        Draw draw = elements.get(port);
        if (draw != null && draw.shape != Shape.REDIRECT) {
            throw new IllegalStateException();
        }
    }

    private void dumpVertices(PrintWriter writer, Node graph) {
        for (Node element : graph.getElements()) {
            Draw draw = elements.get(element);
            switch (draw.shape) {
            case POINT:
            case BOX:
            case INPUT:
            case OUTPUT:
            case ROUNDED_BOX:
                writer.printf("%s [shape=%s, style=%s, label=%s];%n",
                        getSimpleId(draw),
                        literal(draw.shape.symbol),
                        literal(draw.shape.style),
                        literal(draw.label));
                break;
            case RECORD:
                writer.printf("%s [shape=%s, label=%s];%n",
                        getSimpleId(draw),
                        literal(draw.shape.symbol),
                        literal(Stream.of(
                                analyzeRecordMembers(element.getInputs()),
                                draw.label.stream()
                                    .map(Drawer::escapeForRecord)
                                    .collect(Collectors.joining("\n")),
                                analyzeRecordMembers(element.getOutputs()))
                                .collect(Collectors.joining("|", "{", "}"))));
                break;
            case GRAPH:
                writer.printf("subgraph %s {%n", toClusterId(element).get());
                if (draw.label.isEmpty() == false) {
                    writer.printf("label = %s;%n", literal(draw.label));
                }
                dumpVertices(writer, element);
                writer.println("}");
                break;
            default:
                throw new IllegalStateException(draw.shape.toString());
            }
        }
    }

    private String analyzeRecordMembers(List<? extends Port<?, ?>> ports) {
        return ports.stream()
                .map(elements::get)
                .map(draw -> String.format("<%s>%s", getSimpleId(draw), draw.label.stream()
                        .map(Drawer::escapeForRecord)
                        .collect(Collectors.joining("\n"))))
                .collect(Collectors.joining("|", "{", "}"));
    }

    private void dumpEdges(PrintWriter writer) {
        for (Connect c : connections) {
            String upstream = toQualifiedId(c.source, false);
            String downstream = toQualifiedId(c.destination, true);

            List<String> attributes = new ArrayList<>();
            toClusterId(c.source)
                .map(it -> "ltail=" + it)
                .ifPresent(attributes::add);
            toClusterId(c.destination)
                .map(it -> "lhead=" + it)
                .ifPresent(attributes::add);

            if (attributes.isEmpty()) {
                writer.printf("%s -> %s;%n", upstream, downstream);
            } else {
                writer.printf("%s -> %s [%s];%n", upstream, downstream, String.join(", ", attributes));
            }
        }
    }

    private String getSimpleId(Element element) {
        return getSimpleId(elements.get(element));
    }

    private String getSimpleId(Draw draw) {
        if (draw == null) {
            throw new IllegalStateException();
        } else if (draw.shape == Shape.REDIRECT) {
            return getSimpleId(draw.destination);
        } else {
            return '_' + Integer.toString(draw.id.value, 36);
        }
    }

    private String toQualifiedId(Element element, boolean incoming) {
        Draw draw = elements.get(element);
        if (element instanceof Port<?, ?>) {
            if (draw == null) {
                return toQualifiedId(element.getParent(), incoming);
            } else if (draw.shape == Shape.REDIRECT) {
                return toQualifiedId(draw.destination, incoming);
            } else if (draw.shape == Shape.MEMBER) {
                return String.format(
                        "%s:%s",
                        getSimpleId(element.getParent()),
                        getSimpleId(draw));
            } else {
                throw new IllegalStateException();
            }
        } else if (element instanceof Node) {
            if (draw == null) {
                throw new IllegalStateException();
            } else if (draw.shape == Shape.GRAPH) {
                // use element ID instead of graph ID
                Node node = (Node) element;
                return node.getElements().stream()
                        .filter(it -> (incoming ? it.getInputs() : it.getOutputs()).stream()
                                .allMatch(p -> p.getOpposites().isEmpty()))
                        .map(elements::get)
                        .map(this::getSimpleId)
                        .findAny()
                        .orElseGet(() -> node.getElements().stream()
                                .map(elements::get)
                                .map(this::getSimpleId)
                                .findAny()
                                .get());
            } else {
                return getSimpleId(draw);
            }
        } else {
            return getSimpleId(draw);
        }
    }

    private Optional<String> toClusterId(Element element) {
        if (elements.containsKey(element) == false && element instanceof Port<?, ?>) {
            return toClusterId(element.getParent());
        }
        return Optional.ofNullable(elements.get(element))
                .filter(d -> d.shape == Shape.GRAPH)
                .map(it -> "cluster" + getSimpleId(it));
    }

    private static String literal(List<String> label) {
        return literal(String.join("\n", label));
    }

    static String literal(String string) {
        StringBuilder buf = new StringBuilder();
        buf.append('"');
        for (char c : string.toCharArray()) {
            if (c == '\\' || c == '"') {
                buf.append('\\');
                buf.append(c);
            } else if (c == '\n') {
                buf.append('\\');
                buf.append('n');
            } else {
                buf.append(c);
            }
        }
        buf.append('"');
        return buf.toString();
    }

    static String escapeForRecord(CharSequence string) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (c == '{' || c == '<') {
                buf.append('(');
            } else if (c == '}' || c == '>') {
                buf.append(')');
            } else if (c == '|') {
                buf.append('/');
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private static class Draw {

        final Id id;

        final Shape shape;

        final List<String> label;

        final Element destination;

        Draw(Id id, Shape shape, List<String> label, Element destination) {
            this.id = id;
            this.shape = shape;
            this.label = label;
            this.destination = destination;
        }
    }

    private static class Connect {

        final Element source;

        final Element destination;

        Connect(Element source, Element destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    private static class Id {

        final int value;

        Id(int value) {
            this.value = value;
        }
    }

    enum Shape {

        POINT("point"),

        BOX("box"),

        ROUNDED_BOX("box", "rounded"),

        INPUT("invhouse"),

        OUTPUT("invhouse"),

        RECORD("record"),

        GRAPH(null),

        MEMBER(null),

        REDIRECT(null),
        ;

        final String symbol;

        final String style;

        Shape(String symbol) {
            this(symbol, "solid");
        }

        Shape(String symbol, String style) {
            this.symbol = symbol;
            this.style = style;
        }
    }
}
