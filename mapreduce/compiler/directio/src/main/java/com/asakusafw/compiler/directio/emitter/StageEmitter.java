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
package com.asakusafw.compiler.directio.emitter;

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
import com.asakusafw.runtime.directio.DirectDataSourceConstants;
import com.asakusafw.runtime.io.util.ShuffleKey.AbstractGroupComparator;
import com.asakusafw.runtime.io.util.ShuffleKey.AbstractOrderComparator;
import com.asakusafw.runtime.io.util.ShuffleKey.Partitioner;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.runtime.stage.BaseStageClient;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.stage.directio.AbstractDirectOutputKey;
import com.asakusafw.runtime.stage.directio.AbstractDirectOutputMapper;
import com.asakusafw.runtime.stage.directio.AbstractDirectOutputValue;
import com.asakusafw.runtime.stage.directio.AbstractNoReduceDirectOutputMapper;
import com.asakusafw.runtime.stage.directio.DirectOutputReducer;
import com.asakusafw.runtime.stage.directio.DirectOutputSpec;
import com.asakusafw.runtime.stage.output.BridgeOutputFormat;
import com.asakusafw.runtime.trace.TraceLocation;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
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
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits a stage class for direct output.
 * @since 0.2.5
 */
public class StageEmitter {

    static final Logger LOG = LoggerFactory.getLogger(StageEmitter.class);

    private final FlowCompilingEnvironment environment;

    private final String moduleId;

    /**
     * Creates a new instance.
     * @param environment current environment
     * @param moduleId target module ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public StageEmitter(FlowCompilingEnvironment environment, String moduleId) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        this.environment = environment;
        this.moduleId = moduleId;
    }

    /**
     * Emits a client class.
     * @param slots target slots
     * @param outputLocation output location
     * @return the generated class name
     * @throws IOException if failed to emit class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompiledStage emit(List<Slot> slots, Location outputLocation) throws IOException {
        Precondition.checkMustNotBeNull(slots, "slots"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputLocation, "outputLocation"); //$NON-NLS-1$
        LOG.debug("Start preparing output stage for Direct I/O epilogue: batch={}, flow={}", //$NON-NLS-1$
                environment.getBatchId(),
                environment.getFlowId());
        if (requiresReducer(slots)) {
            return emitClientWithReducer(slots, outputLocation);
        } else {
            return emitClientWithoutReducer(slots, outputLocation);
        }
    }

    private boolean requiresReducer(List<Slot> slots) {
        assert slots != null;
        for (Slot slot : slots) {
            if (requiresReducer(slot)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresReducer(Slot slot) {
        assert slot != null;
        return slot.orderClass != null;
    }

    private CompiledStage emitClientWithReducer(List<Slot> slots, Location outputLocation) throws IOException {
        assert slots != null;
        assert outputLocation != null;
        LOG.debug("Emitting shuffle key for Direct I/O epilogue"); //$NON-NLS-1$
        Name key = emitKey(slots);

        LOG.debug("Emitting shuffle value for Direct I/O epilogue"); //$NON-NLS-1$
        Name value = emitValue(slots);

        LOG.debug("Emitting grouping comparator for Direct I/O epilogue"); //$NON-NLS-1$
        Name grouping = emitGrouping(key);

        LOG.debug("Emitting sort comparator for Direct I/O epilogue"); //$NON-NLS-1$
        Name ordering = emitOrdering(key);

        LOG.debug("Emitting mappers for Direct I/O epilogue"); //$NON-NLS-1$
        List<CompiledSlot> compiledSlots = emitMappers(slots, key, value);

        LOG.debug("Emitting stage client (with reducer) for Direct I/O epilogue"); //$NON-NLS-1$
        Name client = emitClient(compiledSlots, key, value, grouping, ordering, outputLocation);

        LOG.debug("Finish preparing output stage for Direct I/O epilogue: " //$NON-NLS-1$
                + "batch={}, flow={}, class={}", new Object[] { //$NON-NLS-1$
                environment.getBatchId(),
                environment.getFlowId(),
                client.toNameString(),
        });
        return new CompiledStage(client, Naming.getEpilogueName(moduleId));
    }

    private CompiledStage emitClientWithoutReducer(List<Slot> slots, Location outputLocation) throws IOException {
        assert slots != null;
        assert outputLocation != null;

        LOG.debug("Emitting mappers for Direct I/O epilogue"); //$NON-NLS-1$
        List<CompiledSlot> compiledSlots = emitMappers(slots, null, null);

        LOG.debug("Emitting stage client (without reducer) for Direct I/O epilogue"); //$NON-NLS-1$
        Name client = emitClient(compiledSlots, null, null, null, null, outputLocation);

        LOG.debug("Finish preparing output stage for Direct I/O epilogue: " //$NON-NLS-1$
                + "batch={}, flow={}, class={}", new Object[] { //$NON-NLS-1$
                environment.getBatchId(),
                environment.getFlowId(),
                client.toNameString(),
        });
        return new CompiledStage(client, Naming.getEpilogueName(moduleId));
    }

    private Name emitKey(List<Slot> slots) throws IOException {
        assert slots != null;
        return emitWithSpecs(
                Naming.getShuffleKeyClass(),
                AbstractDirectOutputKey.class,
                slots);
    }

    private Name emitValue(List<Slot> slots) throws IOException {
        assert slots != null;
        return emitWithSpecs(
                Naming.getShuffleValueClass(),
                AbstractDirectOutputValue.class,
                slots);
    }

    private Name emitGrouping(Name key) throws IOException {
        assert key != null;
        return emitWithClass(
                Naming.getShuffleGroupingComparatorClass(),
                AbstractGroupComparator.class,
                key);
    }

    private Name emitOrdering(Name key) throws IOException {
        return emitWithClass(
                Naming.getShuffleSortComparatorClass(),
                AbstractOrderComparator.class,
                key);
    }

    private List<CompiledSlot> emitMappers(List<Slot> slots, Name keyOrNull, Name valueOrNull) throws IOException {
        assert slots != null;
        List<CompiledSlot> results = new ArrayList<>();
        int index = 0;
        for (Slot slot : slots) {
            Name mapper;
            if (requiresReducer(slot)) {
                assert keyOrNull != null;
                assert valueOrNull != null;
                mapper = emitShuffleMapper(slot, index, keyOrNull, valueOrNull);
            } else {
                mapper = emitOutputMapper(slot, index);
            }
            results.add(new CompiledSlot(slot, mapper));
            index++;
        }
        return results;
    }

    private Name emitShuffleMapper(Slot slot, int index, Name key, Name value) throws IOException {
        assert slot != null;
        assert key != null;
        assert value != null;
        assert index >= 0;
        assert requiresReducer(slot);
        ModelFactory f = environment.getModelFactory();
        SimpleName className = f.newSimpleName(Naming.getMapClass(index));
        ImportBuilder importer = new ImportBuilder(
                f,
                f.newPackageDeclaration(environment.getEpiloguePackageName(moduleId)),
                Strategy.TOP_LEVEL);
        importer.resolvePackageMember(className);
        List<Expression> arguments = new ArrayList<>();
        arguments.add(Models.toLiteral(f, index));
        arguments.add(classLiteralOrNull(f, importer, key));
        arguments.add(classLiteralOrNull(f, importer, value));
        return emitConstructorClass(
                className,
                f.newParameterizedType(
                        importer.toType(AbstractDirectOutputMapper.class),
                        importer.toType(slot.valueType)),
                importer,
                arguments);
    }

    private Name emitOutputMapper(Slot slot, int index) throws IOException {
        assert slot != null;
        assert index >= 0;
        assert requiresReducer(slot) == false;
        ModelFactory f = environment.getModelFactory();
        SimpleName className = f.newSimpleName(Naming.getMapClass(index));
        ImportBuilder importer = new ImportBuilder(
                f,
                f.newPackageDeclaration(environment.getEpiloguePackageName(moduleId)),
                Strategy.TOP_LEVEL);
        importer.resolvePackageMember(className);
        List<Expression> arguments = new ArrayList<>();
        arguments.add(f.newClassLiteral(importer.toType(slot.valueType)));
        arguments.add(Models.toLiteral(f, slot.name));
        arguments.add(Models.toLiteral(f, slot.basePath));
        arguments.add(Models.toLiteral(f, slot.resourcePath));
        arguments.add(f.newClassLiteral(importer.toType(slot.formatClass)));

        return emitConstructorClass(
                className,
                f.newParameterizedType(
                        importer.toType(AbstractNoReduceDirectOutputMapper.class),
                        importer.toType(slot.valueType)),
                importer,
                arguments);
    }

    private Name emitWithSpecs(String classNameString, Class<?> baseClass, List<Slot> slots) throws IOException {
        assert classNameString != null;
        assert baseClass != null;
        assert slots != null;
        ModelFactory f = environment.getModelFactory();
        SimpleName className = f.newSimpleName(classNameString);
        ImportBuilder importer = new ImportBuilder(
                f,
                f.newPackageDeclaration(environment.getEpiloguePackageName(moduleId)),
                Strategy.TOP_LEVEL);
        importer.resolvePackageMember(className);
        List<Expression> elements = new ArrayList<>();
        for (Slot slot : slots) {
            if (requiresReducer(slot)) {
                List<Expression> arguments = new ArrayList<>();
                arguments.add(f.newClassLiteral(importer.toType(slot.valueType)));
                arguments.add(Models.toLiteral(f, slot.name));
                arguments.add(Models.toLiteral(f, slot.basePath));
                arguments.add(f.newClassLiteral(importer.toType(slot.formatClass)));
                arguments.add(f.newClassLiteral(importer.toType(slot.namingClass)));
                arguments.add(f.newClassLiteral(importer.toType(slot.orderClass)));
                elements.add(new TypeBuilder(f, importer.toType(DirectOutputSpec.class))
                    .newObject(arguments)
                    .toExpression());
            } else {
                elements.add(Models.toNullLiteral(f));
            }
        }
        return emitConstructorClass(
                className,
                importer.toType(baseClass),
                importer,
                Collections.singletonList(f.newArrayCreationExpression(
                        (ArrayType) importer.toType(DirectOutputSpec[].class),
                        f.newArrayInitializer(elements))));
    }

    private Expression classLiteralOrNull(ModelFactory f, ImportBuilder importer, Name nameOrNull) {
        assert f != null;
        assert importer != null;
        if (nameOrNull == null) {
            return Models.toNullLiteral(f);
        } else {
            return f.newClassLiteral(importer.toType(nameOrNull));
        }
    }

    private Name emitWithClass(
            String classNameString,
            Class<?> baseClass,
            Name argumentClassName) throws IOException {
        assert classNameString != null;
        assert baseClass != null;
        assert argumentClassName != null;
        ModelFactory f = environment.getModelFactory();
        SimpleName className = f.newSimpleName(classNameString);
        ImportBuilder importer = new ImportBuilder(
                f,
                f.newPackageDeclaration(environment.getEpiloguePackageName(moduleId)),
                Strategy.TOP_LEVEL);
        importer.resolvePackageMember(className);
        List<Expression> arguments = new ArrayList<>();
        arguments.add(classLiteralOrNull(f, importer, argumentClassName));
        return emitConstructorClass(className, importer.toType(baseClass), importer, arguments);
    }

    private Name emitConstructorClass(
            SimpleName className,
            Type baseClass,
            ImportBuilder importer,
            List<? extends Expression> arguments) throws IOException {
        assert className != null;
        assert importer != null;
        assert arguments != null;
        ModelFactory f = environment.getModelFactory();
        Statement ctorChain = f.newSuperConstructorInvocation(arguments);
        ConstructorDeclaration ctorDecl = f.newConstructorDeclaration(
                new JavadocBuilder(f)
                    .text("Creates a new instance.") //$NON-NLS-1$
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                className,
                Collections.emptyList(),
                Collections.singletonList(ctorChain));
        ClassDeclaration typeDecl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .annotation(importer.toType(TraceLocation.class), createTraceLocationElements())
                    .Public()
                    .Final()
                    .toAttributes(),
                className,
                importer.resolve(baseClass),
                Collections.emptyList(),
                Collections.singletonList(ctorDecl));
        CompilationUnit source = f.newCompilationUnit(
                importer.getPackageDeclaration(),
                importer.toImportDeclarations(),
                Collections.singletonList(typeDecl));
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("epilogue of \"{}\" will use {}", moduleId, name); //$NON-NLS-1$
        return name;
    }

    private Map<String, Expression> createTraceLocationElements() {
        ModelFactory factory = environment.getModelFactory();
        Map<String, Expression> results = new LinkedHashMap<>();
        results.put("batchId", Models.toLiteral(factory, environment.getBatchId())); //$NON-NLS-1$
        results.put("flowId", Models.toLiteral(factory, environment.getFlowId())); //$NON-NLS-1$
        results.put("stageId", Models.toLiteral(factory, Naming.getEpilogueName(moduleId))); //$NON-NLS-1$
        return results;
    }

    private Name emitClient(
            List<CompiledSlot> compiledSlots,
            Name keyOrNull, Name valueOrNull,
            Name groupingOrNull, Name orderingOrNull,
            Location outputLocation) throws IOException {
        assert compiledSlots != null;
        assert outputLocation != null;
        Name partitionerOrNull;
        Name reducerOrNull;
        if (keyOrNull != null) {
            partitionerOrNull = Models.toName(
                    environment.getModelFactory(), Partitioner.class.getName().replace('$', '.'));
            reducerOrNull = Models.toName(
                    environment.getModelFactory(), DirectOutputReducer.class.getName());
        } else {
            partitionerOrNull = null;
            reducerOrNull = null;
        }
        Engine engine = new Engine(
                environment,
                moduleId,
                compiledSlots,
                outputLocation,
                keyOrNull, valueOrNull,
                groupingOrNull, orderingOrNull, partitionerOrNull,
                reducerOrNull);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("\"{}\" will use {}", moduleId, name); //$NON-NLS-1$
        return name;
    }

    private static class CompiledSlot {

        final Slot original;

        final Name mapperClass;

        CompiledSlot(Slot original, Name mapperClass) {
            this.original = original;
            this.mapperClass = mapperClass;
        }
    }

    private static class Engine {

        private static final char PATH_SEPARATOR = '/';

        private final FlowCompilingEnvironment environment;

        private final String moduleId;

        private final List<CompiledSlot> slots;

        private final Location outputDirectory;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final Name key;

        private final Name value;

        private final Name grouping;

        private final Name ordering;

        private final Name partitioner;

        private final Name reducer;

        Engine(
                FlowCompilingEnvironment environment,
                String moduleId,
                List<CompiledSlot> slots,
                Location outputDirectory,
                Name key, Name value, Name grouping, Name ordering, Name partitioner, Name reducer) {
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
            this.key = key;
            this.value = value;
            this.grouping = grouping;
            this.ordering = ordering;
            this.partitioner = partitioner;
            this.reducer = reducer;
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getClientClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createIdMethods());
            members.add(createStageOutputPath());
            members.add(createStageInputsMethod());
            members.add(createStageOutputsMethod());
            if (key != null) {
                members.addAll(createShuffleMethods());
            }
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

        private MethodDeclaration createStageInputsMethod() {
            SimpleName list = factory.newSimpleName("results"); //$NON-NLS-1$
            SimpleName attributes = factory.newSimpleName("attributes"); //$NON-NLS-1$

            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageInput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageInput.class)), list));
            statements.add(new ExpressionBuilder(factory, Models.toNullLiteral(factory))
                .toLocalVariableDeclaration(t(Map.class, t(String.class), t(String.class)), attributes));

            for (CompiledSlot slot : slots) {
                Type mapperType = importer.toType(slot.mapperClass);
                for (SourceInfo info : slot.original.sources) {
                    statements.add(new ExpressionBuilder(factory, attributes)
                        .assignFrom(new TypeBuilder(factory, t(HashMap.class, t(String.class), t(String.class)))
                            .newObject()
                            .toExpression())
                        .toStatement());
                    for (Map.Entry<String, String> entry : info.getAttributes().entrySet()) {
                        statements.add(new ExpressionBuilder(factory, attributes)
                            .method("put", //$NON-NLS-1$
                                    Models.toLiteral(factory, entry.getKey()),
                                    Models.toLiteral(factory, entry.getValue()))
                            .toStatement());
                    }
                    for (Location input : info.getLocations()) {
                        statements.add(new ExpressionBuilder(factory, list)
                            .method("add", new TypeBuilder(factory, t(StageInput.class)) //$NON-NLS-1$
                                .newObject(
                                        Models.toLiteral(factory, input.toPath(PATH_SEPARATOR)),
                                        factory.newClassLiteral(t(info.getFormat())),
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

        private List<MethodDeclaration> createShuffleMethods() {
            List<MethodDeclaration> results = new ArrayList<>();
            results.add(createClassLiteralMethod(AbstractStageClient.METHOD_SHUFFLE_KEY_CLASS, key));
            results.add(createClassLiteralMethod(AbstractStageClient.METHOD_SHUFFLE_VALUE_CLASS, value));
            results.add(createClassLiteralMethod(AbstractStageClient.METHOD_GROUPING_COMPARATOR_CLASS, grouping));
            results.add(createClassLiteralMethod(AbstractStageClient.METHOD_SORT_COMPARATOR_CLASS, ordering));
            results.add(createClassLiteralMethod(AbstractStageClient.METHOD_PARTITIONER_CLASS, partitioner));
            results.add(createClassLiteralMethod(AbstractStageClient.METHOD_REDUCER_CLASS, reducer));
            return results;
        }

        private MethodDeclaration createClassLiteralMethod(
                String methodName,
                Name typeName) {
            assert methodName != null;
            Type type = importer.toType(typeName);
            return createValueMethod(methodName, t(Class.class, type), factory.newClassLiteral(type));
        }

        private MethodDeclaration createStageOutputsMethod() {
            SimpleName list = factory.newSimpleName("results"); //$NON-NLS-1$
            SimpleName attributes = factory.newSimpleName("attributes"); //$NON-NLS-1$

            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageOutput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageOutput.class)), list));
            statements.add(new ExpressionBuilder(factory, Models.toNullLiteral(factory))
                .toLocalVariableDeclaration(t(Map.class, t(String.class), t(String.class)), attributes));

            Type formatType = t(BridgeOutputFormat.class);
            for (CompiledSlot slot : slots) {
                Slot origin = slot.original;
                Expression valueType = factory.newClassLiteral(importer.toType(origin.valueType));
                statements.add(new ExpressionBuilder(factory, attributes)
                    .assignFrom(new TypeBuilder(factory, t(HashMap.class, t(String.class), t(String.class)))
                        .newObject()
                        .toExpression())
                    .toStatement());
                int index = 1;
                for (String pattern : slot.original.deletePatterns) {
                    statements.add(new ExpressionBuilder(factory, attributes)
                        .method("put", //$NON-NLS-1$
                                Models.toLiteral(factory, String.format("%s%02d", //$NON-NLS-1$
                                        DirectDataSourceConstants.PREFIX_DELETE_PATTERN, index++)),
                                Models.toLiteral(factory, pattern))
                        .toStatement());
                }
                statements.add(new ExpressionBuilder(factory, list)
                    .method("add", new TypeBuilder(factory, t(StageOutput.class)) //$NON-NLS-1$
                        .newObject(
                                Models.toLiteral(factory, origin.basePath),
                                factory.newClassLiteral(t(NullWritable.class)),
                                valueType,
                                factory.newClassLiteral(formatType),
                                attributes)
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

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("A client class for {0}.",
                        d -> d.code("\"{0}\"", moduleId)) //$NON-NLS-1$
                .toJavadoc();
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
