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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.mapreduce.Partitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Arrangement;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
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

/**
 * シャッフルフェーズで利用するパーティショナーを生成する。
 */
public class ShufflePartitionerEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShufflePartitionerEmitter.class);

    private FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShufflePartitionerEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のモデルに対するパーティショナーを表すクラスを生成し、生成したクラスの完全限定名を返す。
     * @param model 対象のモデル
     * @param keyTypeName キー型の完全限定名
     * @param valueTypeName 値型の完全限定名
     * @return 生成したクラスの完全限定名
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name emit(
            ShuffleModel model,
            Name keyTypeName,
            Name valueTypeName) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(keyTypeName, "keyTypeName"); //$NON-NLS-1$
        LOG.debug("{}に対するパーティショナーを生成します", model.getStageBlock());
        Engine engine = new Engine(environment, model, keyTypeName, valueTypeName);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("{}のパーティショニングには{}が利用されます",
                model.getStageBlock(),
                name);
        return name;
    }

    private static class Engine {

        private static final String HASH_CODE_METHOD_NAME = "getHashCode";

        private ShuffleModel model;

        private ModelFactory factory;

        private ImportBuilder importer;

        private Type keyType;

        private Type valueType;

        public Engine(
                FlowCompilingEnvironment environment,
                ShuffleModel model,
                Name keyTypeName,
                Name valueTypeName) {
            assert environment != null;
            assert model != null;
            assert keyTypeName != null;
            assert valueTypeName != null;
            this.model = model;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(model.getStageBlock().getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.keyType = importer.resolve(factory.newNamedType(keyTypeName));
            this.valueType = importer.resolve(factory.newNamedType(valueTypeName));
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
            SimpleName name = factory.newSimpleName(Naming.getShufflePartitionerClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = Lists.create();
            members.add(createPartition());
            members.add(createHashCode());
            members.add(ShuffleEmiterUtil.createPortToElement(factory, model));
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation"))
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    importer.resolve(factory.newParameterizedType(
                            t(Partitioner.class),
                            Arrays.asList(keyType, valueType))),
                    Collections.<Type>emptyList(),
                    members);
        }

        private MethodDeclaration createPartition() {
            SimpleName key = factory.newSimpleName("key");
            SimpleName value = factory.newSimpleName("value");
            SimpleName partitions = factory.newSimpleName("numPartitions");

            List<Statement> statements = Lists.create();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method(HASH_CODE_METHOD_NAME, key)
                .apply(InfixOperator.AND, new TypeBuilder(factory, t(Integer.class))
                    .field("MAX_VALUE")
                    .toExpression())
                .apply(InfixOperator.REMAINDER, partitions)
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName("getPartition"),
                    Arrays.asList(new FormalParameterDeclaration[] {
                            factory.newFormalParameterDeclaration(keyType, key),
                            factory.newFormalParameterDeclaration(valueType, value),
                            factory.newFormalParameterDeclaration(t(int.class), partitions),
                    }),
                    statements);
        }

        private MethodDeclaration createHashCode() {
            SimpleName key = factory.newSimpleName("key");
            List<Statement> statements = Lists.create();
            SimpleName portId = factory.newSimpleName("portId");
            SimpleName result = factory.newSimpleName("result");

            statements.add(new ExpressionBuilder(factory, key)
                .method(SegmentedWritable.ID_GETTER)
                .toLocalVariableDeclaration(t(int.class), portId));
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method(ShuffleEmiterUtil.PORT_TO_ELEMENT, portId)
                .toLocalVariableDeclaration(t(int.class), result));

            List<Statement> cases = Lists.create();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                for (Term term : segment.getTerms()) {
                    if (term.getArrangement() != Arrangement.GROUPING) {
                        continue;
                    }
                    Expression hash = term.getSource().createHashCode(
                            new ExpressionBuilder(factory, key)
                                .field(ShuffleEmiterUtil.getPropertyName(segment, term))
                                .toExpression());
                    cases.add(new ExpressionBuilder(factory, result)
                        .assignFrom(
                                new ExpressionBuilder(factory, result)
                                    .apply(InfixOperator.TIMES, v(31))
                                    .apply(InfixOperator.PLUS, hash)
                                    .toExpression())
                        .toStatement());
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject(portId)
                .toThrowStatement());

            statements.add(factory.newSwitchStatement(portId, cases));
            statements.add(new ExpressionBuilder(factory, result)
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName(HASH_CODE_METHOD_NAME),
                    Collections.singletonList(
                            factory.newFormalParameterDeclaration(keyType, key)),
                    statements);
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text("ステージ#{0}シャッフルで利用するパーティショナー。",
                    model.getStageBlock().getStageNumber())
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
