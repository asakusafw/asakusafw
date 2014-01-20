/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.Collections;
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
import com.asakusafw.runtime.flow.VoidResult;
import com.asakusafw.runtime.stage.output.StageOutputDriver;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
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
 * 処理断片を依存関係に従って再構築する。
 */
public class FragmentFlow {

    private final FlowCompilingEnvironment environment;

    private final ImportBuilder importer;

    private final NameGenerator names;

    private final StageModel stage;

    private final List<? extends StageModel.Unit<?>> units;

    private final ShuffleModel shuffle;

    private final SimpleName stageOutputs;

    private final Map<FlowElementInput, FragmentNode> lines = Maps.create();

    private final Map<FlowElement, FragmentNode> rendezvous = Maps.create();

    private Map<ResourceFragment, SimpleName> resources = Maps.create();

    private final ModelFactory factory;

    private final Graph<FragmentNode> dependencies;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param importer インポート宣言を生成する
     * @param names 衝突しない名前を生成する
     * @param model 対象のステージを表すモデル
     * @param units 対象の処理単位群
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
                return names.create("outputs");
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
        Map<ResourceFragment, SimpleName> results = Maps.create();
        for (Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                for (ResourceFragment resource : fragment.getResources()) {
                    if (results.containsKey(resource)) {
                        continue;
                    }
                    results.put(resource, names.create("resource"));
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
        Set<Fragment> saw = Sets.create();
        Map<FlowElementOutput, List<FragmentNode>> results = Maps.create();
        for (StageModel.Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                if (saw.contains(fragment)) {
                    continue;
                }
                saw.add(fragment);
                FragmentNode node;
                if (fragment.isRendezvous()) {
                    node = new FragmentNode(Kind.RENDEZVOUS, fragment, names.create("rendezvous"));
                } else {
                    node = new FragmentNode(Kind.LINE, fragment, names.create("line"));
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
        Set<FragmentNode> saw = Sets.create();
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
            SimpleName name = names.create("output");
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

        Map<FlowElementInput, FlowBlock.Input> inputMap = Maps.create();
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
            FragmentNode node = new FragmentNode(Kind.SHUFFLE, segment, names.create("shuffle"));
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
     * 処理断片の先頭要素を参照するためのフィールド群を構築して返す。
     * @return 処理断片の先頭要素を参照するためのフィールド群
     */
    public List<FieldDeclaration> createFields() {
        List<FieldDeclaration> results = Lists.create();
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
     * 処理断片を初期化する文の一覧を返す。
     * @param context コンテキストオブジェクトを参照するための式
     * @return 構築した文の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Statement> createSetup(Expression context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        List<Statement> results = Lists.create();
        if (stageOutputs != null) {
            results.addAll(setupStageOutputs(context));
        }
        results.addAll(setupResources(context));
        results.addAll(setupFragments(context));
        return results;
    }

    private List<Statement> setupResources(Expression context) {
        assert context != null;
        List<Statement> results = Lists.create();
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
                .method("setup", new ExpressionBuilder(factory, context)
                    .method("getConfiguration")
                    .toExpression())
                .toStatement());
        }
        return results;
    }

    private List<Statement> setupFragments(Expression context) {
        List<Statement> results = Lists.create();
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
        List<Statement> results = Lists.create();
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
        List<Expression> results = Lists.create();
        // TODO 引数の並び順に暗黙の前提: リソース一覧 -> 出力一覧
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

        SimpleName cacheName = names.create("cache");
        FieldDeclaration cache = factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                dataType,
                cacheName,
                model.createNewInstance(dataType));

        SimpleName argumentName = names.create("arg");
        List<Statement> statements = Lists.create();
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
                Collections.<Type>emptyList(),
                importer.resolve(factory.newParameterizedType(
                        Models.toType(factory, Result.class),
                        dataType)),
                Collections.<Expression>emptyList(),
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
                            .method("getResultSink", Models.toLiteral(factory, value.getName()))
                            .toExpression())));
    }

    /**
     * 処理断片を破棄する文の一覧を返す。
     * @param context コンテキストオブジェクトを参照するための式
     * @return 構築した文の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Statement> createCleanup(SimpleName context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        List<Statement> results = Lists.create();
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
                                .method("cleanup", new ExpressionBuilder(factory, context)
                                    .method("getConfiguration")
                                    .toExpression())
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
        List<Statement> results = Lists.create();
        results.add(new ExpressionBuilder(factory, factory.newThis())
            .field(stageOutputs)
            .method("close")
            .toStatement());
        results.add(new ExpressionBuilder(factory, factory.newThis())
            .field(stageOutputs)
            .assignFrom(Models.toNullLiteral(factory))
            .toStatement());
        return results;
    }

    /**
     * シャッフル出力に関するキーの型を返す。
     * @return シャッフル出力に関するキーの型、シャッフルしない場合はダミーの型
     */
    public Type getShuffleKeyType() {
        if (shuffle == null) {
            return importer.toType(NullWritable.class);
        }
        Name keyName = shuffle.getCompiled().getKeyTypeName();
        return importer.resolve(factory.newNamedType(keyName));
    }

    /**
     * シャッフル出力に関する値の型を返す。
     * @return シャッフル出力に関する値の型、シャッフルしない場合はダミーの型
     */
    public Type getShuffleValueType() {
        if (shuffle == null) {
            return importer.toType(NullWritable.class);
        }
        Name valueName = shuffle.getCompiled().getValueTypeName();
        return importer.resolve(factory.newNamedType(valueName));
    }

    /**
     * 指定の入力に関連するライン断片を参照するための式を返す。
     * @param input 対象の入力
     * @return 対応する式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * 指定の要素に関連する合流断片を参照するための式を返す。
     * @param element 対象の要素
     * @return 対応する式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
            this.downstreams = Maps.create();
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
     * ノードの種類。
     */
    public enum Kind {

        /**
         * 1入力多出力のライン。
         * <p>
         * コンパイル結果が{@link Result}型を表す{@link StageModel.Fragment}。
         * コンストラクターの引数に、出力の順にノードを取る。
         * </p>
         */
        LINE,

        /**
         * 合流を表す演算子。
         * <p>
         * コンパイル結果が{@link Rendezvous}型を表す{@link StageModel.Fragment}。
         * コンストラクターの引数に、出力の順にノードを取る。
         * </p>
         */
        RENDEZVOUS,

        /**
         * 1入力をシャッフルに出力する。
         * <p>
         * コンパイル結果が{@link Result}型を表す{@link ShuffleModel.Segment}。
         * コンストラクターの引数に、{@link TaskInputOutputContext
         * TaskInputOutputContext<?, ?, ? super K, ? super V>}を取る。
         * </p>
         */
        SHUFFLE,

        /**
         * 1入力を既定のファイルシステムに出力する。
         * <p>
         * シャッフル以外へ出力する{@link StageModel.Sink}。
         * </p>
         */
        OUTPUT,
    }
}
