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
import com.asakusafw.runtime.flow.ResultOutput;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Identity;

/**
 * An emitter which emits fragments of a shuffle action.
 */
public class ShuffleFragmentEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShuffleFragmentEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ShuffleFragmentEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new shuffle fragment class, and returns qualified name of its class.
     * @param segment the target shuffle segment
     * @param keyTypeName the qualified name of the target shuffle key class
     * @param valueTypeName the qualified name of the target shuffle value class
     * @param stageBlock the target stage which contains the target shuffle segment
     * @return the compiled fragment
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledShuffleFragment emit(
            ShuffleModel.Segment segment,
            Name keyTypeName,
            Name valueTypeName,
            StageBlock stageBlock) throws IOException {
        Precondition.checkMustNotBeNull(segment, "segment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        LOG.debug("start generating shuffle fragment: {}", segment); //$NON-NLS-1$

        CompiledType mapOut = emitMapOutput(segment, keyTypeName, valueTypeName, stageBlock);
        CompiledType combineOut = emitCombineOutput(segment, keyTypeName, valueTypeName, stageBlock);

        LOG.debug("finish generating shuffle fragment: {} ({}, {})", new Object[] { //$NON-NLS-1$
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

        CompiledType compiled = new CompiledType(typeName);
        return compiled;
    }

    private static class MapOutputEngine extends Engine {

        MapOutputEngine(
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
            LOG.debug("applying {}: {}", processor, segment); //$NON-NLS-1$

            results.addAll(context.getGeneratedStatements());
            extraFields.addAll(context.getGeneratedFields());

            return context.getOutput();
        }
    }

    private static class CombineOutputEngine extends Engine {

        CombineOutputEngine(
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

        final List<FieldDeclaration> extraFields = new ArrayList<>();

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
            this.collector = names.create("collector"); //$NON-NLS-1$
            this.keyType = importer.toType(keyTypeName);
            this.valueType = importer.toType(valueTypeName);
            this.keyModel = names.create("key"); //$NON-NLS-1$
            this.valueModel = names.create("value"); //$NON-NLS-1$
        }

        abstract SimpleName getClassSimpleName();

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = getClassSimpleName();
            importer.resolvePackageMember(name);

            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createFields());
            ConstructorDeclaration ctor = createConstructor();
            MethodDeclaration method = createBody();
            members.addAll(extraFields);
            members.add(ctor);
            members.add(method);
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
                    Collections.singletonList(importer.resolve(
                            factory.newParameterizedType(
                                    t(Result.class),
                                    getInputType()))),
                    members);
        }

        abstract Type getInputType();

        private List<FieldDeclaration> createFields() {
            List<FieldDeclaration> results = new ArrayList<>();
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
            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(collector)
                .assignFrom(collector)
                .toStatement());
            return factory.newConstructorDeclaration(
                    new JavadocBuilder(factory)
                        .text("Creates a new instance.") //$NON-NLS-1$
                        .param(collector)
                            .text("output collector") //$NON-NLS-1$
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
            SimpleName argument = names.create("result"); //$NON-NLS-1$
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
            List<Statement> results = new ArrayList<>();

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

            results.add(new TypeBuilder(factory, t(ResultOutput.class))
                .method("write", //$NON-NLS-1$
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(collector)
                            .toExpression(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(keyModel)
                            .toExpression(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(valueModel)
                            .toExpression())
                .toStatement());
            return results;
        }

        Expression preprocess(LinePartProcessor.Context context, List<Statement> results) {
            return context.getInput();
        }

        private LinePartProcessor.Context createPartConext(Expression input) {
            assert input != null;
            OperatorDescription description = new OperatorDescription.Builder(Identity.class)
                .declare(Void.class, Void.class, "") //$NON-NLS-1$
                .addInput("input", Object.class) //$NON-NLS-1$
                .addOutput("output", Object.class) //$NON-NLS-1$
                .toDescription();
            return new LinePartProcessor.Context(
                    environment,
                    description,
                    importer,
                    names,
                    description,
                    input,
                    Collections.emptyMap());
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("A shuffle fragment for processing {0}.",
                        d -> d.code(segment.getPort()))
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
