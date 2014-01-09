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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedCombiner;
import com.asakusafw.runtime.flow.SegmentedReducer;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;

/**
 * Combinerプログラムを出力するエミッタ。
 */
public class CombinerEmitter {

    static final Logger LOG = LoggerFactory.getLogger(CombinerEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CombinerEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のステージ内のCombinerに対するクラスを生成し、生成したクラスの完全限定名を返す。
     * @param model 対象のステージ
     * @return 生成したクラスの完全限定名、不要の場合は{@code null}
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledType emit(StageModel model) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        if (canCombine(model) == false) {
            LOG.debug("{}に対してコンバイナークラスは不要です", model);
            return null;
        }
        LOG.debug("{}に対するコンバイナークラスを生成します", model);
        Engine engine = new Engine(environment, model);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();

        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);

        LOG.debug("{}のコンバイン処理には{}が利用されます", model, name);
        return new CompiledType(name);
    }

    private boolean canCombine(StageModel model) {
        assert model != null;
        for (ReduceUnit unit : model.getReduceUnits()) {
            if (unit.canCombine()) {
                return true;
            }
        }
        return false;
    }

    private static class Engine {

        private final List<ReduceUnit> reduceUnits;

        private final ShuffleModel shuffle;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final NameGenerator names;

        private final SimpleName context;

        private final Map<ShuffleModel.Segment, SimpleName> shuffleNames;

        private final Map<Fragment, SimpleName> rendezvousNames;

        Engine(FlowCompilingEnvironment environment, StageModel model) {
            assert environment != null;
            assert model != null;
            this.reduceUnits = model.getReduceUnits();
            this.shuffle = model.getShuffleModel();
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(
                    model.getStageBlock().getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.names = new NameGenerator(factory);
            this.context = names.create("context");
            this.shuffleNames = Maps.create();
            this.rendezvousNames = Maps.create();
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type),
                    Collections.<Comment>emptyList());
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getCombineClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = Lists.create();
            members.addAll(prepareFields());
            members.add(createSetup());
            members.add(createCleanup());
            members.add(createGetRendezvous());
            return factory.newClassDeclaration(
                    new JavadocBuilder(factory)
                        .text("ステージ{0}の処理を担当するコンバイナープログラム。",
                                shuffle.getStageBlock().getStageNumber())
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation"))
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    importer.resolve(factory.newParameterizedType(
                            Models.toType(factory, SegmentedCombiner.class),
                            Arrays.asList(
                                    importer.toType(shuffle.getCompiled().getKeyTypeName()),
                                    importer.toType(shuffle.getCompiled().getValueTypeName())))),
                    Collections.<Type>emptyList(),
                    members);
        }

        private List<FieldDeclaration> prepareFields() {
            List<FieldDeclaration> fields = Lists.create();

            // shufles
            for (ShuffleModel.Segment segment : shuffle.getSegments()) {
                SimpleName shuffleName = names.create("shuffle");
                shuffleNames.put(segment, shuffleName);
                Name shuffleTypeName = segment.getCompiled().getCombineOutputType().getQualifiedName();
                fields.add(factory.newFieldDeclaration(
                        null,
                        new AttributeBuilder(factory)
                            .Private()
                            .toAttributes(),
                        importer.toType(shuffleTypeName),
                        shuffleName,
                        null));
            }

            // rendezvous
            for (ReduceUnit unit : reduceUnits) {
                if (unit.canCombine() == false) {
                    continue;
                }
                Fragment first = unit.getFragments().get(0);
                SimpleName rendezvousName = names.create("combine");
                rendezvousNames.put(first, rendezvousName);
                fields.add(factory.newFieldDeclaration(
                        null,
                        new AttributeBuilder(factory)
                            .Private()
                            .toAttributes(),
                        importer.toType(first.getCompiled().getQualifiedName()),
                        rendezvousName,
                        null));
            }
            return fields;
        }

        private MethodDeclaration createSetup() {
            Map<FlowElementInput, Segment> segments = Maps.create();
            List<Statement> statements = Lists.create();

            // shuffle outputs
            for (Map.Entry<ShuffleModel.Segment, SimpleName> entry : shuffleNames.entrySet()) {
                ShuffleModel.Segment segment = entry.getKey();
                SimpleName name = entry.getValue();
                Name shuffleTypeName = segment.getCompiled().getCombineOutputType().getQualifiedName();
                statements.add(new ExpressionBuilder(factory, factory.newThis())
                    .field(name)
                    .assignFrom(new TypeBuilder(factory, importer.toType(shuffleTypeName))
                        .newObject(context)
                        .toExpression())
                    .toStatement());
                segments.put(segment.getPort(), segment);
            }

            // rendezvous
            for (Map.Entry<Fragment, SimpleName> entry : rendezvousNames.entrySet()) {
                Fragment fragment = entry.getKey();
                Type rendezvousType = importer.toType(fragment.getCompiled().getQualifiedName());
                List<Expression> arguments = Lists.create();
                for (FlowElementInput input : fragment.getInputPorts()) {
                    Segment segment = segments.get(input);
                    assert segment != null;
                    SimpleName shuffleName = shuffleNames.get(segment);
                    assert shuffleName != null;
                    arguments.add(new ExpressionBuilder(factory, factory.newThis())
                        .field(shuffleName)
                        .toExpression());
                }
                SimpleName name = entry.getValue();
                statements.add(new ExpressionBuilder(factory, factory.newThis())
                    .field(name)
                    .assignFrom(new TypeBuilder(factory, rendezvousType)
                        .newObject(arguments)
                        .toExpression())
                    .toStatement());
            }

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    t(void.class),
                    factory.newSimpleName("setup"),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            factory.newNamedType(factory.newSimpleName("Context")),
                            context)),
                    0,
                    Arrays.asList(t(IOException.class), t(InterruptedException.class)),
                    factory.newBlock(statements));
        }

        private MethodDeclaration createCleanup() {
            List<Statement> statements = Lists.create();
            for (SimpleName name : shuffleNames.values()) {
                statements.add(new ExpressionBuilder(factory, factory.newThis())
                    .field(name)
                    .assignFrom(Models.toNullLiteral(factory))
                    .toStatement());
            }
            for (SimpleName name : rendezvousNames.values()) {
                statements.add(new ExpressionBuilder(factory, factory.newThis())
                    .field(name)
                    .assignFrom(Models.toNullLiteral(factory))
                    .toStatement());
            }
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    t(void.class),
                    factory.newSimpleName("cleanup"),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            factory.newNamedType(factory.newSimpleName("Context")),
                            context)),
                    0,
                    Arrays.asList(t(IOException.class), t(InterruptedException.class)),
                    factory.newBlock(statements));
        }

        private MethodDeclaration createGetRendezvous() {
            Map<FlowElement, SimpleName> fragments = Maps.create();
            for (Map.Entry<Fragment, SimpleName> entry : rendezvousNames.entrySet()) {
                fragments.put(entry.getKey().getFactors().get(0).getElement(), entry.getValue());
            }

            List<Statement> cases = Lists.create();
            for (List<ShuffleModel.Segment> group : ShuffleEmiterUtil.groupByElement(shuffle)) {
                for (ShuffleModel.Segment segment : group) {
                    cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                }
                FlowElement element = group.get(0).getPort().getOwner();
                SimpleName rendezvousName = fragments.get(element);
                if (rendezvousName == null) {
                    cases.add(new ExpressionBuilder(factory, Models.toNullLiteral(factory))
                        .toReturnStatement());
                } else {
                    cases.add(new ExpressionBuilder(factory, factory.newThis())
                        .field(rendezvousName)
                        .toReturnStatement());
                }
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject()
                .toThrowStatement());

            SimpleName argument = names.create("nextKey");
            List<Statement> statements = Lists.create();
            statements.add(factory.newSwitchStatement(
                    new ExpressionBuilder(factory, argument)
                        .method(SegmentedWritable.ID_GETTER)
                        .toExpression(),
                    cases));

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Protected()
                        .toAttributes(),
                    importer.resolve(factory.newParameterizedType(
                            Models.toType(factory, Rendezvous.class),
                            importer.toType(shuffle.getCompiled().getValueTypeName()))),
                    factory.newSimpleName(SegmentedReducer.GET_RENDEZVOUS),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            importer.toType(shuffle.getCompiled().getKeyTypeName()),
                            argument)),
                    statements);
        }

        private Type t(java.lang.reflect.Type type) {
            return importer.resolve(Models.toType(factory, type));
        }

        private Expression v(Object value) {
            return Models.toLiteral(factory, value);
        }
    }
}
