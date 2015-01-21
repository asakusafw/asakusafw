/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.WritableComparator;
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
import com.asakusafw.utils.java.model.syntax.IfStatement;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.ThrowStatement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.UnaryOperator;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * シャッフルフェーズで利用する順序比較器を生成する。
 */
public class ShuffleSortComparatorEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleSortComparatorEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ShuffleSortComparatorEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のモデルに対するグループ比較器を表すクラスを生成し、生成したクラスの完全限定名を返す。
     * @param model 対象のモデル
     * @param keyTypeName キー型の完全限定名
     * @return 生成したクラスの完全限定名
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name emit(
            ShuffleModel model,
            Name keyTypeName) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(keyTypeName, "keyTypeName"); //$NON-NLS-1$
        LOG.debug("start generating shuffle sort comparator: {}", model.getStageBlock()); //$NON-NLS-1$
        Engine engine = new Engine(environment, model, keyTypeName);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("finish generating shuffle sort comparator: {} ({})", model.getStageBlock(), name); //$NON-NLS-1$
        return name;
    }

    private static class Engine {

        private final ShuffleModel model;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final Type keyType;

        public Engine(
                FlowCompilingEnvironment environment,
                ShuffleModel model,
                Name keyTypeName) {
            assert environment != null;
            assert model != null;
            assert keyTypeName != null;
            this.model = model;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(model.getStageBlock().getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.keyType = importer.resolve(factory.newNamedType(keyTypeName));
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
            SimpleName name = factory.newSimpleName(Naming.getShuffleSortComparatorClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = Lists.create();
            members.add(createCompareBytes());
            members.add(createCompareObjects());
            members.add(ShuffleEmiterUtil.createCompareInts(factory));
            members.add(ShuffleEmiterUtil.createPortToElement(factory, model));
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("rawtypes")) //$NON-NLS-1$
                        .Public()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(
                            importer.resolve(factory.newParameterizedType(
                                    t(RawComparator.class),
                                    Collections.singletonList(keyType)))),
                    members);
        }

        private MethodDeclaration createCompareBytes() {
            SimpleName b1 = factory.newSimpleName("b1"); //$NON-NLS-1$
            SimpleName s1 = factory.newSimpleName("s1"); //$NON-NLS-1$
            SimpleName l1 = factory.newSimpleName("l1"); //$NON-NLS-1$
            SimpleName b2 = factory.newSimpleName("b2"); //$NON-NLS-1$
            SimpleName s2 = factory.newSimpleName("s2"); //$NON-NLS-1$
            SimpleName l2 = factory.newSimpleName("l2"); //$NON-NLS-1$

            List<Statement> statements = Lists.create();
            SimpleName segmentId1 = factory.newSimpleName("segmentId1"); //$NON-NLS-1$
            SimpleName segmentId2 = factory.newSimpleName("segmentId2"); //$NON-NLS-1$

            statements.add(new TypeBuilder(factory, t(WritableComparator.class))
                .method("readInt", b1, s1) //$NON-NLS-1$
                .toLocalVariableDeclaration(t(int.class), segmentId1));
            statements.add(new TypeBuilder(factory, t(WritableComparator.class))
                .method("readInt", b2, s2) //$NON-NLS-1$
                .toLocalVariableDeclaration(t(int.class), segmentId2));

            SimpleName diff = factory.newSimpleName("diff"); //$NON-NLS-1$
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method(ShuffleEmiterUtil.COMPARE_INT,
                        new ExpressionBuilder(factory, factory.newThis())
                            .method(ShuffleEmiterUtil.PORT_TO_ELEMENT, segmentId1)
                            .toExpression(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .method(ShuffleEmiterUtil.PORT_TO_ELEMENT, segmentId2)
                            .toExpression())
                .toLocalVariableDeclaration(t(int.class), diff));
            statements.add(createDiff(diff));

            SimpleName o1 = factory.newSimpleName("o1"); //$NON-NLS-1$
            SimpleName o2 = factory.newSimpleName("o2"); //$NON-NLS-1$
            SimpleName lim1 = factory.newSimpleName("lim1"); //$NON-NLS-1$
            SimpleName lim2 = factory.newSimpleName("lim2"); //$NON-NLS-1$
            statements.add(new ExpressionBuilder(factory, v(4)).toLocalVariableDeclaration(t(int.class), o1));
            statements.add(new ExpressionBuilder(factory, v(4)).toLocalVariableDeclaration(t(int.class), o2));
            statements.add(new ExpressionBuilder(factory, v(-1)).toLocalVariableDeclaration(t(int.class), lim1));
            statements.add(new ExpressionBuilder(factory, v(-1)).toLocalVariableDeclaration(t(int.class), lim2));

            List<Statement> cases = Lists.create();
            for (List<Segment> segments : ShuffleEmiterUtil.groupByElement(model)) {
                for (Segment segment : segments) {
                    cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                }
                for (Term term : segments.get(0).getTerms()) {
                    if (term.getArrangement() != Arrangement.GROUPING) {
                        continue;
                    }
                    cases.add(new ExpressionBuilder(factory, lim1)
                        .assignFrom(term.getSource().createBytesSize(
                                b1,
                                factory.newInfixExpression(s1, InfixOperator.PLUS, o1),
                                factory.newInfixExpression(l1, InfixOperator.MINUS, o1)))
                        .toStatement());
                    cases.add(new ExpressionBuilder(factory, lim2)
                        .assignFrom(term.getSource().createBytesSize(
                                b2,
                                factory.newInfixExpression(s2, InfixOperator.PLUS, o2),
                                factory.newInfixExpression(l2, InfixOperator.MINUS, o2)))
                        .toStatement());
                    cases.add(new ExpressionBuilder(factory, diff)
                        .assignFrom(
                                term.getSource().createBytesDiff(
                                        b1, factory.newInfixExpression(s1, InfixOperator.PLUS, o1), lim1,
                                        b2, factory.newInfixExpression(s2, InfixOperator.PLUS, o2), lim2))
                        .toStatement());
                    cases.add(createDiff(diff));
                    cases.add(new ExpressionBuilder(factory, o1)
                        .assignFrom(InfixOperator.PLUS, lim1)
                        .toStatement());
                    cases.add(new ExpressionBuilder(factory, o2)
                        .assignFrom(InfixOperator.PLUS, lim2)
                        .toStatement());
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(createAssertionError());

            statements.add(factory.newSwitchStatement(segmentId1, cases));
            statements.add(new ExpressionBuilder(factory, diff)
                .assignFrom(new ExpressionBuilder(factory, factory.newThis())
                    .method(ShuffleEmiterUtil.COMPARE_INT, segmentId1, segmentId2)
                    .toExpression())
                .toStatement());
            statements.add(createDiff(diff));
            cases = Lists.create();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                for (Term term : segment.getTerms()) {
                    if (term.getArrangement() == Arrangement.GROUPING) {
                        continue;
                    }
                    cases.add(new ExpressionBuilder(factory, lim1)
                        .assignFrom(term.getSource().createBytesSize(
                                b1,
                                factory.newInfixExpression(s1, InfixOperator.PLUS, o1),
                                factory.newInfixExpression(l1, InfixOperator.MINUS, o1)))
                        .toStatement());
                    cases.add(new ExpressionBuilder(factory, lim2)
                        .assignFrom(term.getSource().createBytesSize(
                                b2,
                                factory.newInfixExpression(s2, InfixOperator.PLUS, o2),
                                factory.newInfixExpression(l2, InfixOperator.MINUS, o2)))
                        .toStatement());
                    cases.add(new ExpressionBuilder(factory, diff)
                        .assignFrom(
                                term.getSource().createBytesDiff(
                                        b1, factory.newInfixExpression(s1, InfixOperator.PLUS, o1), lim1,
                                        b2, factory.newInfixExpression(s2, InfixOperator.PLUS, o2), lim2))
                        .toStatement());
                    cases.add(createDiff(diff, term.getArrangement() == Arrangement.DESCENDING));
                    cases.add(new ExpressionBuilder(factory, o1)
                        .assignFrom(InfixOperator.PLUS, lim1)
                        .toStatement());
                    cases.add(new ExpressionBuilder(factory, o2)
                        .assignFrom(InfixOperator.PLUS, lim2)
                        .toStatement());
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(createAssertionError());
            statements.add(factory.newSwitchStatement(segmentId1, cases));

            statements.add(new ExpressionBuilder(factory, v(0))
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName("compare"), //$NON-NLS-1$
                    Arrays.asList(new FormalParameterDeclaration[] {
                            factory.newFormalParameterDeclaration(t(byte[].class), b1),
                            factory.newFormalParameterDeclaration(t(int.class), s1),
                            factory.newFormalParameterDeclaration(t(int.class), l1),
                            factory.newFormalParameterDeclaration(t(byte[].class), b2),
                            factory.newFormalParameterDeclaration(t(int.class), s2),
                            factory.newFormalParameterDeclaration(t(int.class), l2),
                    }),
                    statements);
        }

        private ThrowStatement createAssertionError() {
            return new TypeBuilder(factory, t(AssertionError.class)).newObject().toThrowStatement();
        }

        private IfStatement createDiff(Expression diff) {
            return factory.newIfStatement(
                    new ExpressionBuilder(factory, diff)
                        .apply(InfixOperator.NOT_EQUALS, v(0))
                        .toExpression(),
                    new ExpressionBuilder(factory, diff)
                        .toReturnStatement(),
                    null);
        }

        private IfStatement createDiff(Expression diff, boolean desc) {
            return factory.newIfStatement(
                    new ExpressionBuilder(factory, diff)
                        .apply(InfixOperator.NOT_EQUALS, v(0))
                        .toExpression(),
                    new ExpressionBuilder(factory, diff)
                        .apply(desc ? UnaryOperator.MINUS : UnaryOperator.PLUS)
                        .toReturnStatement(),
                    null);
        }

        private TypeBodyDeclaration createCompareObjects() {
            SimpleName o1 = factory.newSimpleName("o1"); //$NON-NLS-1$
            SimpleName o2 = factory.newSimpleName("o2"); //$NON-NLS-1$

            List<Statement> statements = Lists.create();
            SimpleName segmentId1 = factory.newSimpleName("segmentId1"); //$NON-NLS-1$
            SimpleName segmentId2 = factory.newSimpleName("segmentId2"); //$NON-NLS-1$
            statements.add(new ExpressionBuilder(factory, o1)
                .method(SegmentedWritable.ID_GETTER)
                .toLocalVariableDeclaration(t(int.class), segmentId1));
            statements.add(new ExpressionBuilder(factory, o2)
                .method(SegmentedWritable.ID_GETTER)
                .toLocalVariableDeclaration(t(int.class), segmentId2));

            SimpleName diff = factory.newSimpleName("diff"); //$NON-NLS-1$
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method(ShuffleEmiterUtil.COMPARE_INT,
                        new ExpressionBuilder(factory, factory.newThis())
                            .method(ShuffleEmiterUtil.PORT_TO_ELEMENT, segmentId1)
                            .toExpression(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .method(ShuffleEmiterUtil.PORT_TO_ELEMENT, segmentId2)
                            .toExpression())
                .toLocalVariableDeclaration(t(int.class), diff));
            statements.add(createDiff(diff));

            List<Statement> cases = Lists.create();
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
                    Expression rhs = term.getSource().createValueDiff(
                            new ExpressionBuilder(factory, o1)
                                .field(name)
                                .toExpression(),
                            new ExpressionBuilder(factory, o2)
                                .field(name)
                                .toExpression());
                    cases.add(new ExpressionBuilder(factory, diff)
                        .assignFrom(rhs)
                        .toStatement());
                    cases.add(createDiff(diff));
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(createAssertionError());

            statements.add(factory.newSwitchStatement(segmentId1, cases));

            statements.add(new ExpressionBuilder(factory, diff)
                .assignFrom(new ExpressionBuilder(factory, factory.newThis())
                    .method(ShuffleEmiterUtil.COMPARE_INT,
                            segmentId1,
                            segmentId2)
                    .toExpression())
                .toStatement());
            statements.add(createDiff(diff));

            cases = Lists.create();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                for (Term term : segment.getTerms()) {
                    if (term.getArrangement() == Arrangement.GROUPING) {
                        continue;
                    }
                    String name = ShuffleEmiterUtil.getPropertyName(segment, term);
                    Expression rhs = term.getSource().createValueDiff(
                            new ExpressionBuilder(factory, o1)
                                .field(name)
                                .toExpression(),
                            new ExpressionBuilder(factory, o2)
                                .field(name)
                                .toExpression());
                    cases.add(new ExpressionBuilder(factory, diff)
                        .assignFrom(rhs)
                        .toStatement());
                    cases.add(createDiff(diff, term.getArrangement() == Arrangement.DESCENDING));
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(createAssertionError());

            statements.add(factory.newSwitchStatement(segmentId1, cases));
            statements.add(new ExpressionBuilder(factory, v(0))
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName("compare"), //$NON-NLS-1$
                    Arrays.asList(new FormalParameterDeclaration[] {
                            factory.newFormalParameterDeclaration(keyType, o1),
                            factory.newFormalParameterDeclaration(keyType, o2),
                    }),
                    statements);
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text("The shuffle sort comparator class for stage <code>{0}</code>.", //$NON-NLS-1$
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
