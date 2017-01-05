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
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
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
 * An emitter for emitting shuffle value classes.
 */
public class ShuffleValueEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleValueEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ShuffleValueEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new shuffle value class, and returns the qualified name of its class.
     * @param model the target shuffle model
     * @return qualified name of the created class
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Name emit(ShuffleModel model) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        LOG.debug("start generating shuffle value class: {}", model.getStageBlock()); //$NON-NLS-1$
        Engine engine = new Engine(environment, model);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("finish generating shuffle value class: {} ({})", model.getStageBlock(), name); //$NON-NLS-1$
        return name;
    }

    private static class Engine {

        private static final String SEGMENT_ID_FIELD_NAME = "segmentId"; //$NON-NLS-1$

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
            SimpleName name = factory.newSimpleName(Naming.getShuffleValueClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createSegmentDistinction());
            members.addAll(createProperties());
            members.addAll(createAccessors());
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
                    factory.newSimpleName(SEGMENT_ID_FIELD_NAME),
                    v(-1));
        }

        private TypeBodyDeclaration createSegmentIdGetter() {
            Statement body = new ExpressionBuilder(factory, factory.newThis())
                .field(SEGMENT_ID_FIELD_NAME)
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
            for (Segment segment : model.getSegments()) {
                results.add(createProperty(segment));
            }
            return results;
        }

        private String createPropertyName(Segment segment) {
            return String.format("%s%04d", "port", segment.getPortId()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        private FieldDeclaration createProperty(Segment segment) {
            assert segment != null;
            String name = createPropertyName(segment);
            DataClass target = segment.getTarget();
            return factory.newFieldDeclaration(
                    new JavadocBuilder(factory)
                        .inline("data model for {0} ({1}).", //$NON-NLS-1$
                                d -> d.code("{0}#{1}", //$NON-NLS-1$
                                        segment.getPort().getOwner().getDescription().getName(),
                                        segment.getPort().getDescription().getName()),
                                d -> d.code(segment.getPortId()))
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(target.getType()),
                    factory.newSimpleName(name),
                    target.createNewInstance(t(target.getType())));
        }

        private List<MethodDeclaration> createAccessors() {
            List<MethodDeclaration> results = new ArrayList<>();
            for (Segment segment : model.getSegments()) {
                results.add(createGetter(segment));
                results.add(createSetter(segment));
            }
            return results;
        }

        private MethodDeclaration createGetter(Segment segment) {
            assert segment != null;
            String methodName = Naming.getShuffleValueGetter(segment.getPortId());

            List<Statement> statements = new ArrayList<>();
            statements.add(factory.newIfStatement(
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(SEGMENT_ID_FIELD_NAME)
                        .apply(InfixOperator.NOT_EQUALS, v(segment.getPortId()))
                        .toExpression(),
                    new TypeBuilder(factory, t(AssertionError.class))
                        .newObject()
                        .toThrowStatement(),
                    null));

            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(createPropertyName(segment))
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    new JavadocBuilder(factory)
                        .inline("Return a data model object for {0}.", //$NON-NLS-1$
                                d -> d.code("{0}#{1}", //$NON-NLS-1$
                                        segment.getPort().getOwner().getDescription().getName(),
                                        segment.getPort().getDescription().getName()))
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    t(segment.getTarget().getType()),
                    factory.newSimpleName(methodName),
                    Collections.emptyList(),
                    statements);
        }

        private MethodDeclaration createSetter(Segment segment) {
            assert segment != null;
            String methodName = Naming.getShuffleValueSetter(segment.getPortId());
            DataClass type = segment.getTarget();

            SimpleName argument = factory.newSimpleName("model"); //$NON-NLS-1$

            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(factory.newSimpleName(SEGMENT_ID_FIELD_NAME))
                .assignFrom(v(segment.getPortId()))
                .toStatement());
            statements.add(type.assign(
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(createPropertyName(segment))
                        .toExpression(),
                    argument));

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
                            t(type.getType()),
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
                .field(SEGMENT_ID_FIELD_NAME)
                .toExpression();

            List<Statement> cases = new ArrayList<>();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                cases.add(new ExpressionBuilder(factory, out)
                    .method("writeInt", v(segment.getPortId())) //$NON-NLS-1$
                    .toStatement());
                String fieldName = createPropertyName(segment);
                cases.add(segment.getTarget().createWriter(
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(fieldName)
                            .toExpression(),
                        out));
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
                .field(SEGMENT_ID_FIELD_NAME)
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
                String fieldName = createPropertyName(segment);
                cases.add(segment.getTarget().createReader(
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(fieldName)
                            .toExpression(),
                        in));
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
                .inline("The shuffle value class for stage {0}.", //$NON-NLS-1$
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
