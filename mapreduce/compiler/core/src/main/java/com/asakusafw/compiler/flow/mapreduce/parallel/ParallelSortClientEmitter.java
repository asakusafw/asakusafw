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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.stage.CompiledType;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.runtime.stage.BaseStageClient;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.stage.collector.SortableSlot;
import com.asakusafw.runtime.stage.collector.WritableSlot;
import com.asakusafw.runtime.trace.TraceLocation;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
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
 * Generates a MapReduce stage client for generating sorted data-set files.
 * @since 0.1.0
 * @version 0.5.1
 */
public class ParallelSortClientEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ParallelSortClientEmitter.class);

    /**
     * Whether emulates legacy mode.
     */
    public static final String ATTRIBUTE_LEGACY = ParallelSortClientEmitter.class.getName() + ".legacy"; //$NON-NLS-1$

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ParallelSortClientEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    static boolean legacy(FlowCompilingEnvironment environment) {
        return environment.getOptions().getExtraAttribute(ParallelSortClientEmitter.ATTRIBUTE_LEGACY) != null;
    }

    /**
     * Emits a MapReduce stage client class for generating sorted data-set files.
     * @param moduleId the external I/O module ID
     * @param slots the target slots
     * @param outputDirectory the output location
     * @return the generated client class
     * @throws IOException if error was occurred while generating the class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledStage emit(
            String moduleId,
            List<ResolvedSlot> slots,
            Location outputDirectory) throws IOException {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(slots, "slots"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputDirectory, "outputDirectory"); //$NON-NLS-1$
        LOG.debug("start generating epilogue stage client: {}", moduleId); //$NON-NLS-1$
        Engine engine = new Engine(environment, moduleId, slots, outputDirectory);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("finish generating epilogue stage client: {} ({})", moduleId, name); //$NON-NLS-1$
        return new CompiledStage(name, Naming.getEpilogueName(moduleId));
    }

    private static class Engine {

        private static final char PATH_SEPARATOR = '/';

        private final FlowCompilingEnvironment environment;

        private final String moduleId;

        private final List<ResolvedSlot> slots;

        private final Location outputDirectory;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        Engine(
                FlowCompilingEnvironment environment,
                String moduleId,
                List<ResolvedSlot> slots,
                Location outputDirectory) {
            assert environment != null;
            assert moduleId != null;
            assert slots != null;
            this.environment = environment;
            this.moduleId = moduleId;
            this.slots = slots;
            this.outputDirectory = outputDirectory;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getEpiloguePackageName(moduleId);
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
        }


        public CompilationUnit generate() throws IOException {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() throws IOException {
            SimpleName name = factory.newSimpleName(Naming.getClientClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createIdMethods());
            members.add(createStageOutputPath());
            members.add(createStageInputsMethod());
            members.add(createStageOutputsMethod());
            members.addAll(createShuffleMethods());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(TraceLocation.class), createTraceLocationElements())
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    t(AbstractStageClient.class),
                    Collections.emptyList(),
                    members);
        }

        private Map<String, Expression> createTraceLocationElements() {
            Map<String, Expression> results = new LinkedHashMap<>();
            results.put("batchId", Models.toLiteral(factory, environment.getBatchId())); //$NON-NLS-1$
            results.put("flowId", Models.toLiteral(factory, environment.getFlowId())); //$NON-NLS-1$
            results.put("stageId", Models.toLiteral(factory, Naming.getEpilogueName(moduleId))); //$NON-NLS-1$
            return results;
        }

        private List<MethodDeclaration> createIdMethods() {
            List<MethodDeclaration> results = new ArrayList<>();
            results.add(createValueMethod(
                    BaseStageClient.METHOD_BATCH_ID,
                    t(String.class),
                    Models.toLiteral(factory, environment.getBatchId())));
            results.add(createValueMethod(
                    BaseStageClient.METHOD_FLOW_ID,
                    t(String.class),
                    Models.toLiteral(factory, environment.getFlowId())));
            results.add(createValueMethod(
                    BaseStageClient.METHOD_STAGE_ID,
                    t(String.class),
                    Models.toLiteral(factory, Naming.getEpilogueName(moduleId))));
            return results;
        }

        private MethodDeclaration createStageOutputPath() {
            return createValueMethod(
                    AbstractStageClient.METHOD_STAGE_OUTPUT_PATH,
                    t(String.class),
                    Models.toLiteral(factory, outputDirectory.toPath(PATH_SEPARATOR)));
        }

        private MethodDeclaration createStageInputsMethod() throws IOException {
            SimpleName list = factory.newSimpleName("results"); //$NON-NLS-1$
            SimpleName attributes = factory.newSimpleName("attributes"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageInput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageInput.class)), list));
            statements.add(new ExpressionBuilder(factory, Models.toNullLiteral(factory))
                .toLocalVariableDeclaration(t(Map.class, t(String.class), t(String.class)), attributes));

            for (ResolvedSlot slot : slots) {
                Type mapperType = generateMapper(slot);
                statements.add(new ExpressionBuilder(factory, attributes)
                    .assignFrom(new TypeBuilder(factory, t(HashMap.class, t(String.class), t(String.class)))
                        .newObject()
                        .toExpression())
                    .toStatement());
                for (SourceInfo input : slot.getSource().getInputs()) {
                    for (Map.Entry<String, String> entry : input.getAttributes().entrySet()) {
                        statements.add(new ExpressionBuilder(factory, attributes)
                            .method("put", //$NON-NLS-1$
                                    Models.toLiteral(factory, entry.getKey()),
                                    Models.toLiteral(factory, entry.getValue()))
                            .toStatement());
                    }
                    for (Location location : input.getLocations()) {
                        statements.add(new ExpressionBuilder(factory, list)
                            .method("add", new TypeBuilder(factory, t(StageInput.class)) //$NON-NLS-1$
                                .newObject(
                                        Models.toLiteral(factory, location.toPath(PATH_SEPARATOR)),
                                        factory.newClassLiteral(t(input.getFormat())),
                                        factory.newClassLiteral(mapperType),
                                        attributes)
                                .toExpression())
                            .toStatement());
                    }
                }
            }
            statements.add(new ExpressionBuilder(factory, list)
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Protected()
                        .toAttributes(),
                    t(List.class, t(StageInput.class)),
                    factory.newSimpleName(AbstractStageClient.METHOD_STAGE_INPUTS),
                    Collections.emptyList(),
                    statements);
        }


        private MethodDeclaration createStageOutputsMethod() {
            SimpleName list = factory.newSimpleName("results"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageOutput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageOutput.class)), list));
            for (ResolvedSlot slot : slots) {
                Expression valueType = factory.newClassLiteral(t(slot.getValueClass().getType()));
                Class<?> outputFormatType = slot.getSource().getOutputFormatType();
                statements.add(new ExpressionBuilder(factory, list)
                    .method("add", new TypeBuilder(factory, t(StageOutput.class)) //$NON-NLS-1$
                        .newObject(
                                Models.toLiteral(factory, slot.getSource().getOutputName()),
                                factory.newClassLiteral(t(NullWritable.class)),
                                valueType,
                                factory.newClassLiteral(t(outputFormatType)))
                        .toExpression())
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(factory, list)
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Protected()
                        .toAttributes(),
                    t(List.class, t(StageOutput.class)),
                    factory.newSimpleName(AbstractStageClient.METHOD_STAGE_OUTPUTS),
                    Collections.emptyList(),
                    statements);
        }

        private List<MethodDeclaration> createShuffleMethods() throws IOException {
            List<MethodDeclaration> results = new ArrayList<>();
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SHUFFLE_KEY_CLASS,
                    importer.toType(SortableSlot.class)));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SHUFFLE_VALUE_CLASS,
                    importer.toType(WritableSlot.class)));
            if (doSort()) {
                Type reducer = generateReducer();
                results.add(createClassLiteralMethod(
                        AbstractStageClient.METHOD_PARTITIONER_CLASS,
                        importer.toType(SortableSlot.Partitioner.class)));
                results.add(createClassLiteralMethod(
                        AbstractStageClient.METHOD_REDUCER_CLASS,
                        reducer));
            }
            return results;
        }

        private boolean doSort() {
            if (ParallelSortClientEmitter.legacy(environment)) {
                return true;
            }
            for (ResolvedSlot slot : slots) {
                if (slot.getSortProperties().isEmpty() == false) {
                    return true;
                }
            }
            return false;
        }

        private Type generateMapper(ResolvedSlot slot) throws IOException {
            assert slot != null;
            ParallelSortMapperEmitter sub = new ParallelSortMapperEmitter(environment);
            CompiledType type = sub.emit(moduleId, slot);
            return importer.toType(type.getQualifiedName());
        }

        private Type generateReducer() throws IOException {
            ParallelSortReducerEmitter sub = new ParallelSortReducerEmitter(environment);
            CompiledType type = sub.emit(moduleId, slots);
            return importer.toType(type.getQualifiedName());
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("Hadoop client class for processing epilogue stage {0}.",
                        d -> d.code("\"{0}\"", moduleId)) //$NON-NLS-1$
                .toJavadoc();
        }

        private MethodDeclaration createClassLiteralMethod(
                String methodName,
                Type type) {
            assert methodName != null;
            assert type != null;
            return createValueMethod(
                    methodName,
                    t(Class.class, type),
                    factory.newClassLiteral(type));
        }

        private MethodDeclaration createValueMethod(
                String methodName,
                Type returnType,
                Expression expression) {
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Protected()
                        .toAttributes(),
                    returnType,
                    factory.newSimpleName(methodName),
                    Collections.emptyList(),
                    Collections.singletonList(factory.newReturnStatement(expression)));
        }

        private Type t(java.lang.reflect.Type type, Type...typeArgs) {
            assert type != null;
            assert typeArgs != null;
            Type raw = importer.toType(type);
            if (typeArgs.length == 0) {
                return raw;
            }
            return factory.newParameterizedType(raw, Arrays.asList(typeArgs));
        }
    }
}
