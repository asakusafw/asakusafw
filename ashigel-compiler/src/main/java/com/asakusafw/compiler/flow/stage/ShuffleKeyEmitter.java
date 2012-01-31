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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Arrangement;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.InfixOperator;
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
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * シャッフルフェーズで利用するキーを生成する。
 */
public class ShuffleKeyEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleKeyEmitter.class);

    private FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleKeyEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のモデルに対するキーを表すクラスを生成し、生成したクラスの完全限定名を返す。
     * @param model 対象のモデル
     * @return 生成したクラスの完全限定名
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name emit(ShuffleModel model) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        LOG.debug("{}に対するシャッフルキーを生成します", model.getStageBlock());
        Engine engine = new Engine(environment, model);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("{}のシャッフルキーには{}が利用されます",
                model.getStageBlock(),
                name);
        return name;
    }

    private static class Engine {

        private static final String PORT_ID_FIELD_NAME = "portId";

        private ShuffleModel model;

        private ModelFactory factory;

        private ImportBuilder importer;

        public Engine(FlowCompilingEnvironment environment, ShuffleModel model) {
            assert environment != null;
            assert model != null;
            this.model = model;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(model.getStageBlock().getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
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
            SimpleName name = factory.newSimpleName(Naming.getShuffleKeyClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.addAll(createSegmentDistinction());
            members.addAll(createProperties());
            members.addAll(createConverters());
            members.add(createCopier());
            members.addAll(createWritables());
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
                    Collections.singletonList(t(SegmentedWritable.class)),
                    members);
        }

        private List<TypeBodyDeclaration> createSegmentDistinction() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
            results.add(createSegmentIdField());
            results.add(createSegmentIdGetter());
            return results;
        }

        private FieldDeclaration createSegmentIdField() {
            return factory.newFieldDeclaration(
                    new JavadocBuilder(factory)
                        .text("シャッフルフェーズを通した演算子のポート番号。")
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName(PORT_ID_FIELD_NAME),
                    v(-1));
        }

        private TypeBodyDeclaration createSegmentIdGetter() {
            Statement body = new ExpressionBuilder(factory, factory.newThis())
                .field(PORT_ID_FIELD_NAME)
                .toReturnStatement();
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName(SegmentedWritable.ID_GETTER),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Collections.singletonList(body));
        }

        private List<FieldDeclaration> createProperties() {
            List<FieldDeclaration> results = new ArrayList<FieldDeclaration>();
            for (List<Segment> segments : ShuffleEmiterUtil.groupByElement(model)) {
                Segment first = segments.get(0);
                for (Term term : first.getTerms()) {
                    if (term.getArrangement() != Arrangement.GROUPING) {
                        continue;
                    }
                    results.add(createProperty(first, term));
                }
                for (Segment segment : segments) {
                    for (Term term : segment.getTerms()) {
                        if (term.getArrangement() == Arrangement.GROUPING) {
                            continue;
                        }
                        results.add(createProperty(segment, term));
                    }
                }
            }
            return results;
        }

        private FieldDeclaration createProperty(Segment segment, Term term) {
            assert segment != null;
            assert term != null;
            Property source = term.getSource();
            String name = ShuffleEmiterUtil.getPropertyName(segment, term);
            return factory.newFieldDeclaration(
                    new JavadocBuilder(factory)
                        .text("{0}#{1}が利用するキー ({2})",
                                segment.getPort().getOwner().getDescription().getName(),
                                segment.getPort().getDescription().getName(),
                                source.getName())
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(source.getType()),
                    factory.newSimpleName(name),
                    source.createNewInstance(t(source.getType())));
        }

        private List<MethodDeclaration> createConverters() {
            List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
            for (Segment segment : model.getSegments()) {
                results.add(createConverter(segment));
            }
            return results;
        }

        private MethodDeclaration createConverter(Segment segment) {
            assert segment != null;
            String methodName = Naming.getShuffleKeySetter(segment.getPortId());
            SimpleName argument = factory.newSimpleName("source");

            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(PORT_ID_FIELD_NAME)
                .assignFrom(v(segment.getPortId()))
                .toStatement());
            for (Term term : segment.getTerms()) {
                String name = ShuffleEmiterUtil.getPropertyName(segment, term);
                statements.add(term.getSource().createGetter(
                        argument,
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(name)
                            .toExpression()));
            }
            return factory.newMethodDeclaration(
                    new JavadocBuilder(factory)
                        .text("{0}#{1}のキーの元になるモデルオブジェクトを設定する",
                                segment.getPort().getOwner().getDescription().getName(),
                                segment.getPort().getDescription().getName())
                        .param(argument)
                            .text("設定するモデルオブジェクト")
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName(methodName),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            t(segment.getTarget().getType()),
                            argument)),
                    statements);
        }

        private MethodDeclaration createCopier() {
            SimpleName argument = factory.newSimpleName("original");
            List<Statement> cases = new ArrayList<Statement>();
            for (List<Segment> segments : ShuffleEmiterUtil.groupByElement(model)) {
                for (Segment segment : segments) {
                    cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                }
                Segment segment = segments.get(0);
                for (Term term : segment.getTerms()) {
                    if (term.getArrangement() != Arrangement.GROUPING) {
                        continue;
                    }
                    String name = ShuffleEmiterUtil.getPropertyName(segment, term);
                    cases.add(term.getSource().assign(
                            new ExpressionBuilder(factory, factory.newThis())
                                .field(name)
                                .toExpression(),
                            new ExpressionBuilder(factory, argument)
                                .field(name)
                                .toExpression()));

                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject(new ExpressionBuilder(factory, factory.newThis())
                    .field(factory.newSimpleName(PORT_ID_FIELD_NAME))
                    .assignFrom(new ExpressionBuilder(factory, argument)
                        .field(factory.newSimpleName(PORT_ID_FIELD_NAME))
                        .toExpression())
                    .toExpression())
                .toThrowStatement());

            SimpleName typeName = factory.newSimpleName(Naming.getShuffleKeyClass());
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(factory.newSimpleName(PORT_ID_FIELD_NAME))
                .assignFrom(new ExpressionBuilder(factory, argument)
                    .field(factory.newSimpleName(PORT_ID_FIELD_NAME))
                    .toExpression())
                .toStatement());
            statements.add(factory.newIfStatement(
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(factory.newSimpleName(PORT_ID_FIELD_NAME))
                        .apply(InfixOperator.LESS, v(0))
                        .toExpression(),
                    factory.newBlock(factory.newReturnStatement()),
                    null));
            statements.add(factory.newSwitchStatement(
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(factory.newSimpleName(PORT_ID_FIELD_NAME))
                        .toExpression(),
                        cases));

            return factory.newMethodDeclaration(
                    new JavadocBuilder(factory)
                        .text("指定のキーのグループ情報をこのオブジェクトに複製する")
                        .param(argument)
                            .text("コピーするキー")
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName(Naming.getShuffleKeyGroupCopier()),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            factory.newNamedType(typeName),
                            argument)),
                    statements);
        }

        private List<MethodDeclaration> createWritables() {
            return Arrays.asList(
                    createWriteMethod(),
                    createReadFieldsMethod());
        }

        private MethodDeclaration createWriteMethod() {
            SimpleName out = factory.newSimpleName("out");

            Expression segmentId = new ExpressionBuilder(factory, factory.newThis())
                .field(PORT_ID_FIELD_NAME)
                .toExpression();

            List<Statement> cases = new ArrayList<Statement>();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                cases.add(new ExpressionBuilder(factory, out)
                    .method("writeInt", v(segment.getPortId()))
                    .toStatement());
                for (Term term : segment.getTerms()) {
                    String fieldName = ShuffleEmiterUtil.getPropertyName(segment, term);
                    cases.add(term.getSource().createWriter(
                            new ExpressionBuilder(factory, factory.newThis())
                                .field(fieldName)
                                .toExpression(),
                            out));
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject(segmentId)
                .toThrowStatement());

            List<Statement> statements = new ArrayList<Statement>();
            statements.add(factory.newSwitchStatement(segmentId, cases));

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    t(void.class),
                    factory.newSimpleName("write"),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            t(DataOutput.class),
                            out)),
                    0,
                    Collections.singletonList(t(IOException.class)),
                    factory.newBlock(statements));
        }

        private MethodDeclaration createReadFieldsMethod() {
            SimpleName in = factory.newSimpleName("in");

            Expression segmentId = new ExpressionBuilder(factory, factory.newThis())
                .field(PORT_ID_FIELD_NAME)
                .toExpression();

            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new ExpressionBuilder(factory, segmentId)
                .assignFrom(new ExpressionBuilder(factory, in)
                    .method("readInt")
                    .toExpression())
                .toStatement());

            List<Statement> cases = new ArrayList<Statement>();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                for (Term term : segment.getTerms()) {
                    String fieldName = ShuffleEmiterUtil.getPropertyName(segment, term);
                    cases.add(term.getSource().createReader(
                            new ExpressionBuilder(factory, factory.newThis())
                                .field(fieldName)
                                .toExpression(),
                            in));
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject(segmentId)
                .toThrowStatement());

            statements.add(factory.newSwitchStatement(segmentId, cases));

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    t(void.class),
                    factory.newSimpleName("readFields"),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            t(DataInput.class),
                            in)),
                    0,
                    Collections.singletonList(t(IOException.class)),
                    factory.newBlock(statements));
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text("ステージ#{0}シャッフルで利用するKeyクラス。",
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
