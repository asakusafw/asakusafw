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
package com.asakusafw.compiler.flow.stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ResourceFragment;
import com.asakusafw.compiler.flow.stage.StageModel.Sink;
import com.asakusafw.compiler.flow.stage.StageModel.Unit;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.ResultOutput;
import com.asakusafw.runtime.flow.VoidResult;
import com.asakusafw.runtime.stage.output.StageOutputDriver;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;

/**
 * Builds a Mapper/Reducer class from a fragment graph.
 */
public class FragmentFlow {

    private final FlowCompilingEnvironment environment;

    private final ImportBuilder importer;

    private final NameGenerator names;

    private final StageModel stage;

    private final List<? extends StageModel.Unit<?>> units;

    private final ShuffleModel shuffle;

    private final SimpleName stageOutputs;

    private final Map<FlowElementInput, FragmentNode> lines = new HashMap<>();

    private final Map<FlowElement, FragmentNode> rendezvous = new HashMap<>();

    private Map<ResourceFragment, SimpleName> resources = new HashMap<>();

    private final ModelFactory factory;

    private final Graph<FragmentNode> dependencies;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param importer the import declaration builder
     * @param names the unique name generator
     * @param model the target stage model
     * @param units the target operation units
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public FragmentFlow(
            FlowCompilingEnvironment environment,
            ImportBuilder importer,
            NameGenerator names,
            StageModel model,
            List<? extends StageModel.Unit<?>> units) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(names, "names"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(units, "units"); //$NON-NLS-1$
        this.environment = environment;
        this.factory = environment.getModelFactory();
        this.importer = importer;
        this.names = names;
        this.stage = model;
        this.units = units;
        this.shuffle = model.getShuffleModel();
        this.resources = createResources();
        this.dependencies = analyzeDependencies();
        resolveDependencies();
        this.stageOutputs = createStageOutputs();
    }

    private SimpleName createStageOutputs() {
        for (FragmentNode node : dependencies.getNodeSet()) {
            if (node.getKind() == Kind.OUTPUT) {
                return names.create("outputs"); //$NON-NLS-1$
            }
        }
        return null;
    }

    private Graph<FragmentNode> analyzeDependencies() {
        Map<FlowElementOutput, List<FragmentNode>> nodes = analyzeNodes();
        Graph<FragmentNode> graph = Graphs.newInstance();
        buildFragmentGraph(nodes, graph);
        buildOutputGraph(nodes, graph);
        if (shuffle != null) {
            buildShuffleGraph(nodes, graph);
        }
        return graph;
    }

    private Map<ResourceFragment, SimpleName> createResources() {
        Map<ResourceFragment, SimpleName> results = new HashMap<>();
        for (Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                for (ResourceFragment resource : fragment.getResources()) {
                    if (results.containsKey(resource)) {
                        continue;
                    }
                    results.put(resource, names.create("resource")); //$NON-NLS-1$
                }
            }
        }
        return results;
    }

    private void resolveDependencies() {
        assert dependencies != null;
        Graph<FragmentNode> tgraph = Graphs.transpose(dependencies);
        for (Graph.Vertex<FragmentNode> vertex : tgraph) {
            if (vertex.getConnected().isEmpty() == false) {
                continue;
            }
            FragmentNode node = vertex.getNode();
            collectFragment(node);
        }
    }

    private void collectFragment(FragmentNode node) throws AssertionError {
        FlowElementInput port = ((StageModel.Fragment) node.getValue()).getInputPorts().get(0);
        switch (node.getKind()) {
        case LINE:
            assert lines.containsKey(port) == false;
            lines.put(port, node);
            break;
        case RENDEZVOUS:
            assert rendezvous.containsKey(port.getOwner()) == false;
            rendezvous.put(port.getOwner(), node);
            break;
        default:
            throw new AssertionError(node.getKind());
        }
    }

    private Map<FlowElementOutput, List<FragmentNode>> analyzeNodes() {
        Set<Fragment> saw = new HashSet<>();
        Map<FlowElementOutput, List<FragmentNode>> results = new HashMap<>();
        for (StageModel.Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                if (saw.contains(fragment)) {
                    continue;
                }
                saw.add(fragment);
                FragmentNode node;
                if (fragment.isRendezvous()) {
                    node = new FragmentNode(Kind.RENDEZVOUS, fragment, names.create("rendezvous")); //$NON-NLS-1$
                } else {
                    node = new FragmentNode(Kind.LINE, fragment, names.create("line")); //$NON-NLS-1$
                }
                for (FlowElementOutput output : fragment.getOutputPorts()) {
                    Maps.addToList(results, output, node);
                }
            }
        }
        return results;
    }

    private void buildFragmentGraph(
            Map<FlowElementOutput, List<FragmentNode>> nodes,
            Graph<FragmentNode> graph) {
        assert nodes != null;
        assert graph != null;
        Set<FragmentNode> saw = new HashSet<>();
        for (Map.Entry<FlowElementOutput, List<FragmentNode>> entry : nodes.entrySet()) {
            for (FragmentNode node : entry.getValue()) {
                if (saw.contains(node)) {
                    continue;
                }
                saw.add(node);
                graph.addNode(node);
                Fragment fragment = (Fragment) node.getValue();
                for (FlowElementInput input : fragment.getInputPorts()) {
                    for (FlowElementOutput pred : input.getOpposites()) {
                        List<FragmentNode> sources = nodes.get(pred);
                        if (sources == null) {
                            continue;
                        }
                        for (FragmentNode source : sources) {
                            source.addDownstream(pred, node);
                            graph.addEdge(source, node);
                        }
                    }
                }
            }
        }
    }

    private void buildOutputGraph(
            Map<FlowElementOutput, List<FragmentNode>> nodes,
            Graph<FragmentNode> graph) {
        assert nodes != null;
        assert graph != null;
        for (Sink sink : stage.getStageResults()) {
            SimpleName name = names.create("output"); //$NON-NLS-1$
            FragmentNode node = new FragmentNode(Kind.OUTPUT, sink, name);
            for (FlowBlock.Output output : sink.getOutputs()) {
                FlowElementOutput target = output.getElementPort();
                List<FragmentNode> sources = nodes.get(target);
                if (sources == null) {
                    continue;
                }
                for (FragmentNode source : sources) {
                    source.addDownstream(target, node);
                    graph.addEdge(source, node);
                }
            }
        }
    }

    private void buildShuffleGraph(
            Map<FlowElementOutput, List<FragmentNode>> nodes,
            Graph<FragmentNode> graph) {
        assert nodes != null;
        assert graph != null;
        assert shuffle != null;
        assert stage.getStageBlock().hasReduceBlocks();

        Map<FlowElementInput, FlowBlock.Input> inputMap = new HashMap<>();
        for (FlowBlock reduceBlock : stage.getStageBlock().getReduceBlocks()) {
            for (FlowBlock.Input blockInput : reduceBlock.getBlockInputs()) {
                assert inputMap.containsKey(blockInput.getElementPort()) == false;
                inputMap.put(blockInput.getElementPort(), blockInput);
            }
        }

        for (ShuffleModel.Segment segment : shuffle.getSegments()) {
            FlowElementInput input = segment.getPort();
            FlowBlock.Input blockInput = inputMap.get(input);
            if (blockInput == null) {
                continue;
            }
            FragmentNode node = new FragmentNode(Kind.SHUFFLE, segment, names.create("shuffle")); //$NON-NLS-1$
            for (FlowBlock.Connection conn : blockInput.getConnections()) {
                FlowElementOutput shuffleOut = conn.getUpstream().getElementPort();
                List<FragmentNode> sources = nodes.get(shuffleOut);
                if (sources == null) {
                    continue;
                }
                for (FragmentNode source : sources) {
                    source.addDownstream(shuffleOut, node);
                    graph.addEdge(source, node);
                }
            }
        }
    }

    /**
     * Returns the field declarations which holds each fragment.
     * @return the field declarations
     */
    public List<FieldDeclaration> createFields() {
        List<FieldDeclaration> results = new ArrayList<>();
        if (stageOutputs != null) {
            results.add(createStageOutputsField());
        }
        for (Map.Entry<ResourceFragment, SimpleName> entry : resources.entrySet()) {
            results.add(createResourceField(entry.getKey(), entry.getValue()));
        }
        for (FragmentNode node : lines.values()) {
            results.add(createFragmentField(node, (StageModel.Fragment) node.getValue()));
        }
        for (FragmentNode node : rendezvous.values()) {
            results.add(createFragmentField(node, (StageModel.Fragment) node.getValue()));
        }
        return results;
    }

    private FieldDeclaration createResourceField(ResourceFragment resource, SimpleName name) {
        assert resource != null;
        assert name != null;
        FieldDeclaration field = factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                importer.toType(resource.getCompiled().getQualifiedName()),
                name,
                null);
        return field;
    }

    private FieldDeclaration createStageOutputsField() {
        FieldDeclaration field = factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                importer.toType(StageOutputDriver.class),
                stageOutputs,
                null);
        return field;
    }

    private FieldDeclaration createFragmentField(FragmentNode node, StageModel.Fragment value) {
        assert node != null;
        assert value != null;
        Type type = importer.resolve(factory.newNamedType(value.getCompiled().getQualifiedName()));
        return factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                type,
                node.getName(),
                null);
    }

    /**
     * Returns the statements which initializes each fragment.
     * @param context an expression which represents a Map/Reduce task context
     * @return the statements
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Statement> createSetup(Expression context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        List<Statement> results = new ArrayList<>();
        if (stageOutputs != null) {
            results.addAll(setupStageOutputs(context));
        }
        results.addAll(setupResources(context));
        results.addAll(setupFragments(context));
        return results;
    }

    private List<Statement> setupResources(Expression context) {
        assert context != null;
        List<Statement> results = new ArrayList<>();
        for (Map.Entry<ResourceFragment, SimpleName> entry : resources.entrySet()) {
            ResourceFragment resource = entry.getKey();
            SimpleName field = entry.getValue();
            results.add(new ExpressionBuilder(factory, factory.newThis())
                .field(field)
                .assignFrom(new TypeBuilder(factory, importer.toType(resource.getCompiled().getQualifiedName()))
                    .newObject()
                    .toExpression())
                .toStatement());
            results.add(new ExpressionBuilder(factory, factory.newThis())
                .field(field)
                .method("setup", context) //$NON-NLS-1$
                .toStatement());
        }
        return results;
    }

    private List<Statement> setupFragments(Expression context) {
        List<Statement> results = new ArrayList<>();
        for (FragmentNode node : Graphs.sortPostOrder(dependencies)) {
            switch (node.getKind()) {
            case LINE:
                results.add(setupLine(node, (StageModel.Fragment) node.getValue()));
                break;
            case RENDEZVOUS:
                results.add(setupRendezvous(node, (StageModel.Fragment) node.getValue()));
                break;
            case SHUFFLE:
                results.add(setupShuffle(node, context, (ShuffleModel.Segment) node.getValue()));
                break;
            case OUTPUT:
                results.add(setupOutput(node, (StageModel.Sink) node.getValue()));
                break;
            default:
                throw new AssertionError(node);
            }
        }
        return results;
    }

    private List<Statement> setupStageOutputs(Expression context) {
        assert context != null;
        List<Statement> results = new ArrayList<>();
        results.add(new ExpressionBuilder(factory, factory.newThis())
            .field(stageOutputs)
            .assignFrom(new TypeBuilder(factory, importer.toType(StageOutputDriver.class))
                .newObject(context)
                .toExpression())
            .toStatement());
        return results;
    }

    private Statement setupLine(FragmentNode node, StageModel.Fragment value) {
        assert node != null;
        assert value != null;
        assert value.getInputPorts().size() == 1;
        FlowElementInput input = value.getInputPorts().get(0);
        Type type = importer.resolve(factory.newNamedType(value.getCompiled().getQualifiedName()));
        List<Expression> arguments = resolveArguments(node, value);
        if (lines.containsKey(input)) {
            return new ExpressionBuilder(factory, factory.newThis())
                .field(node.getName())
                .assignFrom(new TypeBuilder(factory, type)
                    .newObject(arguments)
                    .toExpression())
                .toStatement();
        } else {
            return factory.newLocalVariableDeclaration(
                    new AttributeBuilder(factory)
                        .Final()
                        .toAttributes(),
                    type,
                    Collections.singletonList(factory.newVariableDeclarator(
                            node.getName(),
                            new TypeBuilder(factory, type)
                                .newObject(arguments)
                                .toExpression())));
        }
    }

    private Statement setupRendezvous(FragmentNode node, StageModel.Fragment value) {
        assert node != null;
        assert value != null;
        assert value.getInputPorts().isEmpty() == false;
        FlowElement element = value.getInputPorts().get(0).getOwner();
        Type type = importer.resolve(factory.newNamedType(value.getCompiled().getQualifiedName()));
        List<Expression> arguments = resolveArguments(node, value);
        assert rendezvous.containsKey(element);
        return new ExpressionBuilder(factory, factory.newThis())
            .field(node.getName())
            .assignFrom(new TypeBuilder(factory, type)
                .newObject(arguments)
                .toExpression())
            .toStatement();
    }

    private List<Expression> resolveArguments(FragmentNode node, StageModel.Fragment fragment) {
        assert node != null;
        assert fragment != null;
        List<Expression> results = new ArrayList<>();
        // TODO parameters must be ordered: external resources -> outputs
        for (ResourceFragment resource : fragment.getResources()) {
            results.add(resolveResouce(resource));
        }
        for (FlowElementOutput output : fragment.getOutputPorts()) {
            results.add(resolveArgument(
                    output.getDescription().getDataType(),
                    node.getDownstream(output)));
        }
        return results;
    }

    private Expression resolveResouce(ResourceFragment resource) {
        assert resource != null;
        SimpleName name = resources.get(resource);
        assert name != null;
        return name;
    }

    private Expression resolveArgument(java.lang.reflect.Type type, Set<FragmentNode> downstream) {
        assert type != null;
        assert downstream != null;
        if (downstream.isEmpty()) {
            return new TypeBuilder(factory, importer.resolve(
                    factory.newParameterizedType(
                            Models.toType(factory, VoidResult.class),
                            Collections.singletonList(Models.toType(factory, type)))))
                .newObject()
                .toExpression();
        }
        if (downstream.size() == 1) {
            FragmentNode succ = downstream.iterator().next();
            return succ.getName();
        }

        DataClass model = environment.getDataClasses().load(type);
        if (model == null) {
            throw new IllegalStateException(type.toString());
        }
        Type dataType = importer.toType(model.getType());

        SimpleName cacheName = names.create("cache"); //$NON-NLS-1$
        FieldDeclaration cache = factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                dataType,
                cacheName,
                model.createNewInstance(dataType));

        SimpleName argumentName = names.create("arg"); //$NON-NLS-1$
        List<Statement> statements = new ArrayList<>();
        Iterator<FragmentNode> iter = downstream.iterator();
        while (iter.hasNext()) {
            FragmentNode node = iter.next();
            if (iter.hasNext()) {
                statements.add(model.assign(cacheName, argumentName));
                statements.add(new ExpressionBuilder(factory, node.getName())
                    .method(FlowElementProcessor.RESULT_METHOD_NAME, cacheName)
                    .toStatement());
            } else {
                statements.add(new ExpressionBuilder(factory, node.getName())
                    .method(FlowElementProcessor.RESULT_METHOD_NAME, argumentName)
                    .toStatement());
            }
        }

        MethodDeclaration result = factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .annotation(importer.toType(Override.class))
                    .Public()
                    .toAttributes(),
                factory.newBasicType(BasicTypeKind.VOID),
                factory.newSimpleName(FlowElementProcessor.RESULT_METHOD_NAME),
                Collections.singletonList(factory.newFormalParameterDeclaration(
                        Models.toType(factory, model.getType()),
                        argumentName)),
                statements);

        return factory.newClassInstanceCreationExpression(
                null,
                Collections.emptyList(),
                importer.resolve(factory.newParameterizedType(
                        Models.toType(factory, Result.class),
                        dataType)),
                Collections.emptyList(),
                factory.newClassBody(Arrays.asList(cache, result)));
    }

    private Statement setupShuffle(
            FragmentNode node,
            Expression context,
            ShuffleModel.Segment value) {
        assert node != null;
        assert context != null;
        assert value != null;
        Type type = importer.toType(value.getCompiled().getMapOutputType().getQualifiedName());
        return factory.newLocalVariableDeclaration(
                new AttributeBuilder(factory)
                    .Final()
                    .toAttributes(),
                type,
                Collections.singletonList(factory.newVariableDeclarator(
                        node.getName(),
                        new TypeBuilder(factory, type)
                            .newObject(context)
                            .toExpression())));
    }

    private Statement setupOutput(FragmentNode node, StageModel.Sink value) {
        assert node != null;
        assert value != null;
        Type type = importer.resolve(factory.newParameterizedType(
                Models.toType(factory, Result.class),
                Models.toType(factory, value.getType())));
        return factory.newLocalVariableDeclaration(
                new AttributeBuilder(factory)
                    .Final()
                    .toAttributes(),
                type,
                Collections.singletonList(factory.newVariableDeclarator(
                        node.getName(),
                        new ExpressionBuilder(factory, stageOutputs)
                            .method("getResultSink", Models.toLiteral(factory, value.getName())) //$NON-NLS-1$
                            .toExpression())));
    }

    /**
     * Returns the statements which finalizes each fragment.
     * @param context an expression which represents a Map/Reduce task context
     * @return the statements
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Statement> createCleanup(SimpleName context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        List<Statement> results = new ArrayList<>();
        if (stageOutputs != null) {
            results.addAll(cleanStageOutputs(context));
        }
        for (Map.Entry<ResourceFragment, SimpleName> entry : resources.entrySet()) {
            results.add(factory.newIfStatement(
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(entry.getValue())
                        .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(factory))
                        .toExpression(),
                    factory.newBlock(new Statement[] {
                            new ExpressionBuilder(factory, factory.newThis())
                                .field(entry.getValue())
                                .method("cleanup", context) //$NON-NLS-1$
                                .toStatement(),
                            new ExpressionBuilder(factory, factory.newThis())
                                .field(entry.getValue())
                                .assignFrom(Models.toNullLiteral(factory))
                                .toStatement()
                    })));
        }
        for (FragmentNode node : lines.values()) {
            results.add(new ExpressionBuilder(factory, factory.newThis())
                .field(node.getName())
                .assignFrom(Models.toNullLiteral(factory))
                .toStatement());
        }
        for (FragmentNode node : rendezvous.values()) {
            results.add(new ExpressionBuilder(factory, factory.newThis())
                .field(node.getName())
                .assignFrom(Models.toNullLiteral(factory))
                .toStatement());
        }
        return results;
    }

    private List<Statement> cleanStageOutputs(SimpleName context) {
        assert context != null;
        List<Statement> results = new ArrayList<>();
        results.add(new ExpressionBuilder(factory, factory.newThis())
            .field(stageOutputs)
            .method("close") //$NON-NLS-1$
            .toStatement());
        results.add(new ExpressionBuilder(factory, factory.newThis())
            .field(stageOutputs)
            .assignFrom(Models.toNullLiteral(factory))
            .toStatement());
        return results;
    }

    /**
     * Returns the shuffle output key type.
     * @return the shuffle output key type, or a dummy type if this does not contain shuffle action
     */
    public Type getShuffleKeyType() {
        if (shuffle == null) {
            return importer.toType(NullWritable.class);
        }
        Name keyName = shuffle.getCompiled().getKeyTypeName();
        return importer.resolve(factory.newNamedType(keyName));
    }

    /**
     * Returns the shuffle output value type.
     * @return the shuffle output value type, or a dummy type if this does not contain shuffle action
     */
    public Type getShuffleValueType() {
        if (shuffle == null) {
            return importer.toType(NullWritable.class);
        }
        Name valueName = shuffle.getCompiled().getValueTypeName();
        return importer.resolve(factory.newNamedType(valueName));
    }

    /**
     * Returns an expression of the line fragment.
     * @param input the target input port
     * @return an expression of the corresponded fragment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Expression getLine(FlowElementInput input) {
        Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
        FragmentNode node = lines.get(input);
        if (node == null) {
            throw new IllegalArgumentException();
        }
        return new ExpressionBuilder(factory, factory.newThis())
            .field(node.getName())
            .toExpression();
    }

    /**
     * Returns an expression of the rendezvous fragment.
     * @param element the target element
     * @return an expression of the corresponded fragment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Expression getRendezvous(FlowElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        FragmentNode node = rendezvous.get(element);
        if (node == null) {
            throw new IllegalArgumentException();
        }
        return new ExpressionBuilder(factory, factory.newThis())
            .field(node.getName())
            .toExpression();
    }

    private static class FragmentNode {

        private final Kind kind;

        private final Object value;

        private final SimpleName name;

        private final Map<FlowElementOutput, Set<FragmentNode>> downstreams;

        FragmentNode(Kind kind, Object value, SimpleName name) {
            assert kind != null;
            assert value != null;
            assert name != null;
            this.kind = kind;
            this.value = value;
            this.name = name;
            this.downstreams = new HashMap<>();
        }

        public void addDownstream(FlowElementOutput output, FragmentNode downstream) {
            assert output != null;
            assert downstream != null;
            Maps.addToSet(downstreams, output, downstream);
        }

        public Set<FragmentNode> getDownstream(FlowElementOutput output) {
            assert output != null;
            Set<FragmentNode> set = downstreams.get(output);
            if (set == null) {
                return Collections.emptySet();
            }
            return set;
        }

        public Kind getKind() {
            return kind;
        }

        public Object getValue() {
            return value;
        }

        public SimpleName getName() {
            return name;
        }
    }

    /**
     * Represents a kind of node.
     */
    public enum Kind {

        /**
         * A line fragment.
         * The compilation result must be a sub-class of {@link Result} (from {@link StageModel.Fragment}),
         * and its constructor will accepts each external resource and output.
         */
        LINE,

        /**
         * A rendezvous fragment.
         * The compilation result must be a sub-class of {@link Rendezvous} (from {@link StageModel.Fragment}),
         * and its constructor will accepts each external resource and output.
         */
        RENDEZVOUS,

        /**
         * A shuffle fragment.
         * The compilation result must be a sub-class of {@link Result} (from {@link ShuffleModel.Segment}),
         * and its constructor will accepts just a {@link TaskInputOutputContext
         * TaskInputOutputContext&lt;?, ?, ? super K, ? super V&gt;}
         */
        SHUFFLE,

        /**
         * Stage output.
         * This kind of fragments will not be compiled, and they are represented as {@link ResultOutput}.
         */
        OUTPUT,
    }
}
