/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.compiler.flow.LinePartProcessor.Context;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Identity;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ConstructorDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.Javadoc;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.WildcardBoundKind;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * Shuffleに対するフラグメントクラスを生成するエミッタ。
 */
public class ShuffleFragmentEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleFragmentEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleFragmentEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のセグメントに対するクラスを生成し、生成したクラスの情報を返す。
     * @param segment 処理対象のセグメント
     * @param keyTypeName キー型の完全限定名
     * @param valueTypeName 値型の完全限定名
     * @param stageBlock 対象のセグメントが存在するステージ
     * @return コンパイル結果
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledShuffleFragment emit(
            ShuffleModel.Segment segment,
            Name keyTypeName,
            Name valueTypeName,
            StageBlock stageBlock) throws IOException {
        Precondition.checkMustNotBeNull(segment, "segment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        LOG.debug("{}に対するフラグメントクラスを生成します", segment);

        CompiledType mapOut = emitMapOutput(segment, keyTypeName, valueTypeName, stageBlock);
        CompiledType combineOut = emitCombineOutput(segment, keyTypeName, valueTypeName, stageBlock);

        LOG.debug("{}の出力処理には{}, {}が利用されます", new Object[] {
                segment,
                mapOut.getQualifiedName().toNameString(),
                combineOut.getQualifiedName().toNameString(),
        });
        return new CompiledShuffleFragment(mapOut, combineOut);
    }

    private CompiledType emitMapOutput(
            ShuffleModel.Segment segment,
            Name keyTypeName,
            Name valueTypeName,
            StageBlock stageBlock) throws IOException {
        assert segment != null;
        assert keyTypeName != null;
        assert valueTypeName != null;
        assert stageBlock != null;
        Engine engine = new MapOutputEngine(environment, stageBlock, segment, keyTypeName, valueTypeName);
        return generate(segment, engine);
    }

    private CompiledType emitCombineOutput(
            ShuffleModel.Segment segment,
            Name keyTypeName,
            Name valueTypeName,
            StageBlock stageBlock) throws IOException {
        assert segment != null;
        assert keyTypeName != null;
        assert valueTypeName != null;
        assert stageBlock != null;
        Engine engine = new CombineOutputEngine(environment, stageBlock, segment, keyTypeName, valueTypeName);
        return generate(segment, engine);
    }

    private CompiledType generate(ShuffleModel.Segment segment, Engine engine) throws IOException {
        assert segment != null;
        assert engine != null;
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name typeName = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);

        LOG.debug("{}のMap出力処理には{}が利用されます", segment, typeName);
        CompiledType compiled = new CompiledType(typeName);
        return compiled;
    }

    private static class MapOutputEngine extends Engine {

        public MapOutputEngine(
                FlowCompilingEnvironment environment,
                StageBlock stageBlock,
                Segment segment,
                Name keyTypeName,
                Name valueTypeName) {
            super(environment, stageBlock, segment, keyTypeName, valueTypeName);
        }

        @Override
        SimpleName getClassSimpleName() {
            return factory.newSimpleName(Naming.getMapOutputFragmentClass(segment.getPortId()));
        }

        @Override
        Type getInputType() {
            return importer.toType(segment.getSource().getType());
        }

        @Override
        Expression preprocess(Context context, List<Statement> results) {
            LinePartProcessor processor = segment.getDescription().getConverter();
            if (processor == null) {
                return context.getInput();
            }

            processor.emitLinePart(context);
            LOG.debug("{}に{}を適用しています", segment, processor);

            results.addAll(context.getGeneratedStatements());
            extraFields.addAll(context.getGeneratedFields());

            return context.getOutput();
        }
    }

    private static class CombineOutputEngine extends Engine {

        public CombineOutputEngine(
                FlowCompilingEnvironment environment,
                StageBlock stageBlock,
                Segment segment,
                Name keyTypeName,
                Name valueTypeName) {
            super(environment, stageBlock, segment, keyTypeName, valueTypeName);
        }

        @Override
        SimpleName getClassSimpleName() {
            return factory.newSimpleName(Naming.getCombineOutputFragmentClass(segment.getPortId()));
        }

        @Override
        Type getInputType() {
            return importer.toType(segment.getTarget().getType());
        }
    }

    private abstract static class Engine {

        final FlowCompilingEnvironment environment;

        final Segment segment;

        final ModelFactory factory;

        final ImportBuilder importer;

        final NameGenerator names;

        final SimpleName collector;

        final Type keyType;

        final Type valueType;

        final SimpleName keyModel;

        final SimpleName valueModel;

        final List<FieldDeclaration> extraFields = new ArrayList<FieldDeclaration>();

        Engine(
                FlowCompilingEnvironment environment,
                StageBlock stageBlock,
                Segment segment,
                Name keyTypeName,
                Name valueTypeName) {
            assert environment != null;
            assert stageBlock != null;
            assert segment != null;
            assert keyTypeName != null;
            assert valueTypeName != null;
            this.environment = environment;
            this.segment = segment;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(stageBlock.getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.names = new NameGenerator(factory);
            this.collector = names.create("collector");
            this.keyType = importer.toType(keyTypeName);
            this.valueType = importer.toType(valueTypeName);
            this.keyModel = names.create("key");
            this.valueModel = names.create("value");
        }

        abstract SimpleName getClassSimpleName();

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type),
                    Collections.<Comment>emptyList());
        }

        private TypeDeclaration createType() {
            SimpleName name = getClassSimpleName();
            importer.resolvePackageMember(name);

            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.addAll(createFields());
            ConstructorDeclaration ctor = createConstructor();
            MethodDeclaration method = createBody();
            members.addAll(extraFields);
            members.add(ctor);
            members.add(method);
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation"))
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(importer.resolve(
                            factory.newParameterizedType(
                                    t(Result.class),
                                    getInputType()))),
                    members);
        }

        abstract Type getInputType();

        private List<FieldDeclaration> createFields() {
            List<FieldDeclaration> results = new ArrayList<FieldDeclaration>();
            results.add(createCollectorField());
            results.add(createKeyField());
            results.add(createValueField());
            return results;
        }

        private FieldDeclaration createCollectorField() {
            return factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .Final()
                        .toAttributes(),
                    createContextType(),
                    collector,
                    null);
        }

        private FieldDeclaration createKeyField() {
            return factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .Final()
                        .toAttributes(),
                    keyType,
                    keyModel,
                    new TypeBuilder(factory, keyType)
                        .newObject()
                        .toExpression());
        }

        private FieldDeclaration createValueField() {
            return factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .Final()
                        .toAttributes(),
                    valueType,
                    valueModel,
                    new TypeBuilder(factory, valueType)
                        .newObject()
                        .toExpression());
        }

        private ConstructorDeclaration createConstructor() {
            SimpleName name = getClassSimpleName();
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(collector)
                .assignFrom(collector)
                .toStatement());
            return factory.newConstructorDeclaration(
                    new JavadocBuilder(factory)
                        .text("インスタンスを生成する。")
                        .param(collector)
                            .text("実際の出力先")
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    name,
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            createContextType(),
                            collector)),
                    statements);
        }

        private Type createContextType() {
            return importer.resolve(factory.newParameterizedType(
                    t(TaskInputOutputContext.class),
                    factory.newWildcard(),
                    factory.newWildcard(),
                    factory.newWildcard(WildcardBoundKind.LOWER_BOUNDED, keyType),
                    factory.newWildcard(WildcardBoundKind.LOWER_BOUNDED, valueType)));
        }

        private MethodDeclaration createBody() {
            SimpleName argument = names.create("result");
            List<Statement> statements = createStatements(argument);
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName(FlowElementProcessor.RESULT_METHOD_NAME),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            getInputType(),
                            argument)),
                    statements);
        }

        private List<Statement> createStatements(SimpleName argument) {
            assert argument != null;
            List<Statement> results = new ArrayList<Statement>();

            LinePartProcessor.Context context = createPartConext(argument);
            Expression shuffleInput = preprocess(context, results);

            results.add(new ExpressionBuilder(factory, factory.newThis())
                .field(keyModel)
                .method(Naming.getShuffleKeySetter(segment.getPortId()),
                        shuffleInput)
                .toStatement());
            results.add(new ExpressionBuilder(factory, factory.newThis())
                .field(valueModel)
                .method(Naming.getShuffleValueSetter(segment.getPortId()),
                        shuffleInput)
                .toStatement());

            SimpleName exception = names.create("exception");
            results.add(factory.newTryStatement(
                    factory.newBlock(
                            new ExpressionBuilder(factory, factory.newThis())
                                .field(collector)
                                .method("write",
                                        new ExpressionBuilder(factory, factory.newThis())
                                            .field(keyModel)
                                            .toExpression(),
                                        new ExpressionBuilder(factory, factory.newThis())
                                            .field(valueModel)
                                            .toExpression())
                                .toStatement()),
                    Collections.singletonList(factory.newCatchClause(
                            factory.newFormalParameterDeclaration(t(Exception.class), exception),
                            factory.newBlock(
                                    new TypeBuilder(factory, t(Result.OutputException.class))
                                        .newObject(exception)
                                        .toThrowStatement()))),
                    null));
            return results;
        }

        Expression preprocess(LinePartProcessor.Context context, List<Statement> results) {
            return context.getInput();
        }

        private LinePartProcessor.Context createPartConext(Expression input) {
            assert input != null;
            OperatorDescription description = new OperatorDescription.Builder(Identity.class)
                .declare(Void.class, Void.class, "")
                .addInput("input", Object.class)
                .addOutput("output", Object.class)
                .toDescription();
            return new LinePartProcessor.Context(
                    environment,
                    importer,
                    names,
                    description,
                    input,
                    Collections.<FlowResourceDescription, Expression>emptyMap());
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .code("{0}", segment.getPort())
                .text("へのシャッフル処理を担当するプログラムの断片。")
                .toJavadoc();
        }

        private Type t(java.lang.reflect.Type type) {
            return importer.resolve(Models.toType(factory, type));
        }

        private Expression v(Object value) {
            return Models.toLiteral(factory, value);
        }
    }
}
