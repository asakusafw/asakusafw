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
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
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
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * An emitter for emitting shuffle key classes.
 */
public class ShuffleKeyEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleKeyEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ShuffleKeyEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new shuffle key class, and returns the qualified name of its class.
     * @param model the target shuffle model
     * @return qualified name of the created class
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Name emit(ShuffleModel model) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        LOG.debug("start generating shuffle key model: {}", model.getStageBlock()); //$NON-NLS-1$
        Engine engine = new Engine(environment, model);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("finish generating shuffle key model: {} ({})", model.getStageBlock(), name); //$NON-NLS-1$
        return name;
    }

    private static class Engine {

        private static final String PORT_ID_FIELD_NAME = "portId"; //$NON-NLS-1$

        private final ShuffleModel model;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        Engine(FlowCompilingEnvironment environment, ShuffleModel model) {
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
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getShuffleKeyClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createSegmentDistinction());
            members.addAll(createProperties());
            members.addAll(createConverters());
            members.add(createCopier());
            members.addAll(createWritables());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation")) //$NON-NLS-1$
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    null,
                    Collections.singletonList(t(SegmentedWritable.class)),
                    members);
        }

        private List<TypeBodyDeclaration> createSegmentDistinction() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.add(createSegmentIdField());
            results.add(createSegmentIdGetter());
            return results;
        }

        private FieldDeclaration createSegmentIdField() {
            return factory.newFieldDeclaration(
                    new JavadocBuilder(factory)
                        .text("the shuffle segment ID.") //$NON-NLS-1$
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
                    Collections.emptyList(),
                    Collections.singletonList(body));
        }

        private List<FieldDeclaration> createProperties() {
            List<FieldDeclaration> results = new ArrayList<>();
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
                        .inline("shuffle key value for {0} ({1}).", //$NON-NLS-1$
                                d -> d.code("{0}#{1}",
                                        segment.getPort().getOwner().getDescription().getName(),
                                        segment.getPort().getDescription().getName()),
                                d -> d.code(source.getName()))
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(source.getType()),
                    factory.newSimpleName(name),
                    source.createNewInstance(t(source.getType())));
        }

        private List<MethodDeclaration> createConverters() {
            List<MethodDeclaration> results = new ArrayList<>();
            for (Segment segment : model.getSegments()) {
                results.add(createConverter(segment));
            }
            return results;
        }

        private MethodDeclaration createConverter(Segment segment) {
            assert segment != null;
            String methodName = Naming.getShuffleKeySetter(segment.getPortId());
            SimpleName argument = factory.newSimpleName("source"); //$NON-NLS-1$

            List<Statement> statements = new ArrayList<>();
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
                        .inline("Sets a data model object for the successing operator input {0}.", //$NON-NLS-1$
                                d -> d.code("{0}#{1}", //$NON-NLS-1$
                                        segment.getPort().getOwner().getDescription().getName(),
                                        segment.getPort().getDescription().getName()))
                        .param(argument)
                            .text("the target data model object") //$NON-NLS-1$
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
            SimpleName argument = factory.newSimpleName("original"); //$NON-NLS-1$
            List<Statement> cases = new ArrayList<>();
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
            List<Statement> statements = new ArrayList<>();
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
                        .text("Copies the shuffle key information into this object.") //$NON-NLS-1$
                        .param(argument)
                            .text("the source key object") //$NON-NLS-1$
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
            SimpleName out = factory.newSimpleName("out"); //$NON-NLS-1$

            Expression segmentId = new ExpressionBuilder(factory, factory.newThis())
                .field(PORT_ID_FIELD_NAME)
                .toExpression();

            List<Statement> cases = new ArrayList<>();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                cases.add(new ExpressionBuilder(factory, out)
                    .method("writeInt", v(segment.getPortId())) //$NON-NLS-1$
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

            List<Statement> statements = new ArrayList<>();
            statements.add(factory.newSwitchStatement(segmentId, cases));

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    t(void.class),
                    factory.newSimpleName("write"), //$NON-NLS-1$
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            t(DataOutput.class),
                            out)),
                    0,
                    Collections.singletonList(t(IOException.class)),
                    factory.newBlock(statements));
        }

        private MethodDeclaration createReadFieldsMethod() {
            SimpleName in = factory.newSimpleName("in"); //$NON-NLS-1$

            Expression segmentId = new ExpressionBuilder(factory, factory.newThis())
                .field(PORT_ID_FIELD_NAME)
                .toExpression();

            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, segmentId)
                .assignFrom(new ExpressionBuilder(factory, in)
                    .method("readInt") //$NON-NLS-1$
                    .toExpression())
                .toStatement());

            List<Statement> cases = new ArrayList<>();
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
                    Collections.emptyList(),
                    t(void.class),
                    factory.newSimpleName("readFields"), //$NON-NLS-1$
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            t(DataInput.class),
                            in)),
                    0,
                    Collections.singletonList(t(IOException.class)),
                    factory.newBlock(statements));
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("The shuffle key class for stage {0}.", //$NON-NLS-1$
                        d -> d.code(model.getStageBlock().getStageNumber()))
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
