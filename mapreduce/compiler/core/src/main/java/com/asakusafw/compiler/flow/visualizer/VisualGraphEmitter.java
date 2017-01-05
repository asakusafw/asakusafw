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
package com.asakusafw.compiler.flow.visualizer;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.visualizer.VisualNode.Kind;
import com.asakusafw.utils.java.internal.model.util.LiteralAnalyzer;
import com.asakusafw.utils.java.model.util.NoThrow;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Emits {@link VisualGraph} object as Graphviz dot format.
 * @since 0.1.0
 * @version 0.4.0
 */
public final class VisualGraphEmitter {

    static final Charset ENCODING = StandardCharsets.UTF_8;

    static final Logger LOG = LoggerFactory.getLogger(VisualGraphEmitter.class);

    private VisualGraphEmitter() {
        throw new AssertionError();
    }

    /**
     * Outputs a {@link VisualGraph} object to target file as Graphviz dot format.
     * @param graph target graph
     * @param partial {@code true} if target graph is partial, otherwise {@code false}
     * @param stream output target
     * @throws IOException if failed to output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void emit(VisualGraph graph, boolean partial, OutputStream stream) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stream, "stream"); //$NON-NLS-1$
        LOG.debug("Emitting a visual graph: {}", graph.getId()); //$NON-NLS-1$
        try (EmitContext context = new EmitContext(stream)) {
            List<Relation> relations = analyzeRelations(graph, partial);
            dump(context, graph.getNodes(), relations);
        }
    }

    /**
     * Outputs a {@link VisualGraph} object to target file as Graphviz dot format.
     * @param graph target graph
     * @param partial {@code true} if target graph is partial, otherwise {@code false}
     * @param file output target
     * @throws IOException if failed to output
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public static void emit(VisualGraph graph, boolean partial, File file) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(file, "file"); //$NON-NLS-1$
        try (OutputStream output = new FileOutputStream(file)) {
            emit(graph, partial, output);
        }
    }

    private static List<Relation> analyzeRelations(VisualGraph graph, boolean partial) {
        assert graph != null;
        LOG.debug("Analyzing element relations"); //$NON-NLS-1$
        List<Relation> result = RelationCollector.collect(Collections.singleton(graph), partial);
        return result;
    }

    private static void dump(EmitContext context, Set<VisualNode> nodes, List<Relation> relations) {
        assert context != null;
        assert nodes != null;
        assert relations != null;
        LOG.debug("Emitting graph structure"); //$NON-NLS-1$
        context.put("digraph {"); //$NON-NLS-1$
        context.push();
        dumpStructure(context, nodes);
        dumpLabels(context, relations);
        dumpRelations(context, relations);
        context.pop();
        context.put("}"); //$NON-NLS-1$
    }

    private static void dumpLabels(EmitContext context, List<Relation> relations) {
        assert relations != null;
        Set<UUID> saw = new HashSet<>();
        for (Relation relation : relations) {
            if (saw.contains(relation.source.getResolved().getId()) == false) {
                dumpLabel(context, relation.source.getResolved());
                saw.add(relation.source.getResolved().getId());
            }
            if (saw.contains(relation.source.getResolved().getId()) == false) {
                dumpLabel(context, relation.source.getResolved());
                saw.add(relation.source.getResolved().getId());
            }
            dumpLabel(context, relation.sink.getResolved());
        }
    }

    private static void dumpLabel(EmitContext context, VisualNode node) {
        assert node != null;
        if (node.getKind() == Kind.LABEL) {
            StructureEmitter emitter = new StructureEmitter();
            node.accept(emitter, context);
        }
    }

    private static void dumpStructure(EmitContext context, Set<VisualNode> nodes) {
        assert context != null;
        assert nodes != null;
        StructureEmitter emitter = new StructureEmitter();
        for (VisualNode node : nodes) {
            node.accept(emitter, context);
        }
    }

    private static void dumpRelations(EmitContext context, List<Relation> relations) {
        assert context != null;
        assert relations != null;
        for (Relation relation : relations) {
            context.put("{0} -> {1} [label={2}];", //$NON-NLS-1$
                    toLiteral(relation.source.getResolved().getId().toString()),
                    toLiteral(relation.sink.getResolved().getId().toString()),
                    toLiteral(MessageFormat.format(
                            "{0}>{1}", //$NON-NLS-1$
                            relation.source.name,
                            relation.sink.name)));
        }
    }

    static String toLiteral(String string) {
        assert string != null;
        return LiteralAnalyzer.stringLiteralOf(string);
    }

    private static class RelationCollector extends VisualNodeVisitor<Void, Void, NoThrow> {

        private final boolean partial;

        private final Set<Relation> saw = new HashSet<>();

        final List<Relation> relations = new ArrayList<>();

        final Map<FlowElement, VisualNode> resolveMap = new HashMap<>();

        RelationCollector(boolean partial) {
            this.partial = partial;
        }

        static List<Relation> collect(Iterable<? extends VisualNode> nodes, boolean partial) {
            RelationCollector engine = new RelationCollector(partial);
            engine.acceptAll(null, nodes);
            Iterator<Relation> iter = engine.relations.iterator();
            while (iter.hasNext()) {
                Relation relation = iter.next();
                boolean removed = false;
                if (engine.resolve(relation.source) == false) {
                    if (partial == false) {
                        resolveFailed(relation.source);
                    }
                    iter.remove();
                    removed = true;
                }
                if (engine.resolve(relation.sink) == false) {
                    if (partial == false) {
                        resolveFailed(relation.sink);
                    }
                    if (removed == false) {
                        iter.remove();
                    }
                }
            }
            return engine.relations;
        }

        private static void resolveFailed(Port port) {
            assert port != null;
            LOG.warn(MessageFormat.format(
                    Messages.getString("VisualGraphEmitter.warnInvalidPort"), //$NON-NLS-1$
                    port.element));
        }

        @Override
        protected Void visitGraph(Void context, VisualGraph node) {
            acceptAll(context, node.getNodes());
            return null;
        }

        @Override
        protected Void visitBlock(Void context, VisualBlock node) {
            if (partial) {
                for (FlowBlock.Input input : node.getInputs()) {
                    Port sink = toPort(input.getElementPort());
                    for (FlowBlock.Connection conn : input.getConnections()) {
                        Port source = toPort(conn.getUpstream().getElementPort());
                        related(source, sink);
                    }
                }
            }

            for (FlowBlock.Output output : node.getOutputs()) {
                Port source = toPort(output.getElementPort());
                for (FlowBlock.Connection conn : output.getConnections()) {
                    FlowElementInput downstream = conn.getDownstream().getElementPort();
                    connect(source, downstream);
                }
            }
            acceptAll(context, node.getNodes());
            return null;
        }

        @Override
        protected Void visitFlowPart(Void context, VisualFlowPart node) throws NoThrow {
            register(node, node.getElement());
            connectSuccessors(node.getElement());
            acceptAll(context, node.getNodes());
            return null;
        }

        @Override
        protected Void visitElement(Void context, VisualElement node) {
            register(node, node.getElement());
            connectSuccessors(node.getElement());
            return null;
        }

        private void connectSuccessors(FlowElement element) {
            assert element != null;
            if (element.getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
                FlowPartDescription desc = (FlowPartDescription) element.getDescription();
                for (FlowElementOutput output : element.getOutputPorts()) {
                    FlowOut<?> internal = desc.getInternalOutputPort(output.getDescription());
                    Port source = toPort(internal.toInputPort());
                    for (FlowElementInput downstream : output.getOpposites()) {
                        connect(source, downstream);
                    }
                }
            } else {
                for (FlowElementOutput output : element.getOutputPorts()) {
                    Port source = toPort(output);
                    for (FlowElementInput downstream : output.getOpposites()) {
                        connect(source, downstream);
                    }
                }
            }
        }

        private void connect(Port source, FlowElementInput downstream) {
            assert source != null;
            assert downstream != null;
            if (downstream.getOwner().getDescription().getKind() == FlowElementKind.FLOW_COMPONENT) {
                FlowPartDescription desc = (FlowPartDescription) downstream.getOwner().getDescription();
                FlowIn<?> internal = desc.getInternalInputPort(downstream.getDescription());
                Port sink = toPort(internal.toOutputPort());
                related(source, sink);
            } else {
                Port sink = toPort(downstream);
                related(source, sink);
            }
        }

        private Port toPort(FlowElementOutput port) {
            assert port != null;
            return new Port(port.getOwner(), port.getDescription().getName());
        }

        private Port toPort(FlowElementInput port) {
            assert port != null;
            return new Port(port.getOwner(), port.getDescription().getName());
        }

        private boolean resolve(Port port) {
            assert port != null;
            VisualNode node = resolveMap.get(port.element);
            if (node != null) {
                port.setResolved(node);
                return true;
            }
            if (partial) {
                port.setResolved(new VisualLabel(null));
                return true;
            }
            return false;
        }

        private void register(VisualNode node, FlowElement element) {
            assert node != null;
            assert element != null;
            resolveMap.put(element, node);
        }

        private void related(Port source, Port sink) {
            assert source != null;
            assert sink != null;
            Relation relation = new Relation(source, sink);
            if (saw.contains(relation) == false) {
                relations.add(relation);
                saw.add(relation);
            }
        }

        private void acceptAll(Void context, Iterable<? extends VisualNode> nodes) {
            for (VisualNode node : nodes) {
                node.accept(this, context);
            }
        }
    }

    private static class Relation {

        final Port source;

        final Port sink;

        Relation(Port source, Port sink) {
            assert source != null;
            assert sink != null;
            this.source = source;
            this.sink = sink;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + sink.hashCode();
            result = prime * result + source.hashCode();
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
            Relation other = (Relation) obj;
            if (!sink.equals(other.sink)) {
                return false;
            }
            if (!source.equals(other.source)) {
                return false;
            }
            return true;
        }
    }

    private static class Port {

        final FlowElement element;

        final String name;

        private VisualNode resolved;

        Port(FlowElement element, String name) {
            assert element != null;
            assert name != null;
            this.element = element;
            this.name = name;
        }

        public VisualNode getResolved() {
            assert resolved != null;
            return resolved;
        }

        public void setResolved(VisualNode resolved) {
            assert resolved != null;
            assert this.resolved == null || this.resolved == resolved;
            this.resolved = resolved;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + element.hashCode();
            result = prime * result + name.hashCode();
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
            Port other = (Port) obj;
            if (!element.equals(other.element)) {
                return false;
            }
            if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }
    }

    private static class StructureEmitter extends VisualNodeVisitor<Void, EmitContext, NoThrow> {

        StructureEmitter() {
            return;
        }

        @Override
        public Void visitGraph(EmitContext context, VisualGraph node) {
            if (node.getLabel() != null) {
                context.put("subgraph {0} '{'", //$NON-NLS-1$
                        toLiteral("cluster_" + node.getId().toString())); //$NON-NLS-1$
                context.push();
                context.put("label = {0};", toLiteral(node.getLabel())); //$NON-NLS-1$
                context.put("style = bold;"); //$NON-NLS-1$
            }
            for (VisualNode element : node.getNodes()) {
                element.accept(this, context);
            }
            if (node.getLabel() != null) {
                context.pop();
                context.put("}"); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        protected Void visitBlock(EmitContext context, VisualBlock node) throws NoThrow {
            if (node.getLabel() != null) {
                context.put("subgraph {0} '{'", //$NON-NLS-1$
                        toLiteral("cluster_" + node.getId().toString())); //$NON-NLS-1$
                context.push();
                context.put("label = {0};", toLiteral(node.getLabel())); //$NON-NLS-1$
            }
            for (VisualNode element : node.getNodes()) {
                element.accept(this, context);
            }
            if (node.getLabel() != null) {
                context.pop();
                context.put("}"); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        protected Void visitFlowPart(EmitContext context, VisualFlowPart node) throws NoThrow {
            context.put("subgraph {0} '{'", //$NON-NLS-1$
                    toLiteral("cluster_" + node.getId().toString())); //$NON-NLS-1$
            context.push();
            context.put("label = {0};", //$NON-NLS-1$
                    toLiteral(node.getElement().getDescription().getName()));
            for (VisualNode element : node.getNodes()) {
                element.accept(this, context);
            }
            context.pop();
            context.put("}"); //$NON-NLS-1$
            return null;
        }

        @Override
        protected Void visitElement(EmitContext context, VisualElement node) {
            FlowElement element = node.getElement();
            switch (element.getDescription().getKind()) {
            case INPUT:
            case OUTPUT:
                context.put("{0} [shape=invhouse, label={1}];", //$NON-NLS-1$
                        toLiteral(node.getId().toString()),
                        toLiteral(element.getDescription().getName()));
                break;
            case OPERATOR:
                context.put("{0} [shape=box, label={1}];", //$NON-NLS-1$
                        toLiteral(node.getId().toString()),
                        toLiteral(toOperatorName(node)));
                break;
            case FLOW_COMPONENT:
                context.put("{0} [shape=component, label={1}];", //$NON-NLS-1$
                        toLiteral(node.getId().toString()),
                        toLiteral(element.getDescription().getName()));
                break;
            default:
                context.put("{0} [shape=point];", //$NON-NLS-1$
                        toLiteral(node.getId().toString()));
                break;
            }
            return null;
        }

        @Override
        protected Void visitLabel(EmitContext context, VisualLabel node) {
            if (node.getLabel() == null) {
                context.put("{0} [shape=point];", toLiteral(node.getId().toString())); //$NON-NLS-1$
            } else {
                context.put("{0} [shape=ellipse, label={1}];", //$NON-NLS-1$
                        toLiteral(node.getId().toString()),
                        toLiteral(node.getLabel()));
            }
            return super.visitLabel(context, node);
        }

        static String toOperatorName(VisualElement node) {
            assert node != null;
            FlowElement element = node.getElement();
            assert element.getDescription().getKind() == FlowElementKind.OPERATOR;
            OperatorDescription desc = (OperatorDescription) element.getDescription();
            StringBuilder buf = new StringBuilder();
            buf.append("@"); //$NON-NLS-1$
            buf.append(desc.getDeclaration().getAnnotationType().getSimpleName());
            buf.append("\n"); //$NON-NLS-1$
            buf.append(desc.getName());
            return buf.toString();
        }
    }

    private static class EmitContext implements Closeable {

        private static final int INDENT_UNIT = 4;

        private final PrintWriter writer;

        private int indent = 0;

        EmitContext(OutputStream output) {
            assert output != null;
            writer = new PrintWriter(new OutputStreamWriter(output, ENCODING));
        }

        public void push() {
            indent++;
        }

        public void pop() {
            assert indent >= 1;
            indent--;
        }

        public void put(String pattern, Object... arguments) {
            assert pattern != null;
            assert arguments != null;
            StringBuilder buf = new StringBuilder();
            insertIndent(buf);
            if (arguments.length == 0) {
                buf.append(pattern);
            } else {
                buf.append(MessageFormat.format(pattern, arguments));
            }
            String text = buf.toString();
            writer.println(text);
            LOG.debug(text);
        }

        private void insertIndent(StringBuilder buf) {
            for (int i = 0, n = indent * INDENT_UNIT; i < n; i++) {
                buf.append(' ');
            }
        }

        @Override
        public void close() {
            writer.close();
        }
    }
}
