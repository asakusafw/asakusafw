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
package com.asakusafw.compiler.directio.emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import com.asakusafw.runtime.io.util.ShuffleKey.AbstractGroupComparator;
import com.asakusafw.runtime.io.util.ShuffleKey.AbstractOrderComparator;
import com.asakusafw.runtime.io.util.ShuffleKey.Partitioner;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.stage.directio.AbstractDirectOutputKey;
import com.asakusafw.runtime.stage.directio.AbstractDirectOutputMapper;
import com.asakusafw.runtime.stage.directio.AbstractDirectOutputValue;
import com.asakusafw.runtime.stage.directio.DirectOutputReducer;
import com.asakusafw.runtime.stage.directio.DirectOutputSpec;
import com.asakusafw.runtime.stage.output.BridgeOutputFormat;
import com.ashigeru.lang.java.model.syntax.ArrayType;
import com.ashigeru.lang.java.model.syntax.ClassDeclaration;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ConstructorDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.Javadoc;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.syntax.QualifiedName;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder.Strategy;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

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
        LOG.debug("Start preparing output stage for Direct I/O epilogue: batch={}, flow={}",
                environment.getBatchId(),
                environment.getFlowId());

        LOG.debug("Emitting shuffle key for Direct I/O epilogue");
        Name key = emitKey(slots);

        LOG.debug("Emitting shuffle value for Direct I/O epilogue");
        Name value = emitValue(slots);

        LOG.debug("Emitting grouping comparator for Direct I/O epilogue");
        Name grouping = emitGrouping(key);

        LOG.debug("Emitting sort comparator for Direct I/O epilogue");
        Name ordering = emitOrdering(key);

        LOG.debug("Emitting mappers for Direct I/O epilogue");
        List<CompiledSlot> compiledSlots = emitMappers(slots, key, value);

        LOG.debug("Emitting stage client for Direct I/O epilogue");
        Name client = emitClient(compiledSlots, key, value, grouping, ordering, outputLocation);

        LOG.debug("Finish preparing output stage for Direct I/O epilogue: batch={}, flow={}, class={}", new Object[] {
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

    private List<CompiledSlot> emitMappers(List<Slot> slots, Name key, Name value) throws IOException {
        List<CompiledSlot> results = new ArrayList<CompiledSlot>();
        int index = 0;
        for (Slot slot : slots) {
            Name mapper = emitMapper(slot, index, key, value);
            results.add(new CompiledSlot(slot, mapper));
            index++;
        }
        return results;
    }

    private Name emitMapper(Slot slot, int index, Name key, Name value) throws IOException {
        assert slot != null;
        assert key != null;
        assert value != null;
        ModelFactory f = environment.getModelFactory();
        SimpleName className = f.newSimpleName(Naming.getMapClass(index));
        ImportBuilder importer = new ImportBuilder(
                f,
                f.newPackageDeclaration(environment.getEpiloguePackageName(moduleId)),
                Strategy.TOP_LEVEL);
        importer.resolvePackageMember(className);
        List<Expression> arguments = new ArrayList<Expression>();
        arguments.add(Models.toLiteral(f, index));
        arguments.add(f.newClassLiteral(importer.toType(key)));
        arguments.add(f.newClassLiteral(importer.toType(value)));
        return emitConstructorClass(
                className,
                f.newParameterizedType(
                        importer.toType(AbstractDirectOutputMapper.class),
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
        List<Expression> elements = new ArrayList<Expression>();
        for (Slot slot : slots) {
            List<Expression> arguments = new ArrayList<Expression>();
            arguments.add(f.newClassLiteral(importer.toType(slot.valueType)));
            arguments.add(Models.toLiteral(f, slot.path));
            arguments.add(f.newClassLiteral(importer.toType(slot.formatClass)));
            arguments.add(f.newClassLiteral(importer.toType(slot.namingClass)));
            arguments.add(f.newClassLiteral(importer.toType(slot.orderClass)));
            elements.add(new TypeBuilder(f, importer.toType(DirectOutputSpec.class))
                .newObject(arguments)
                .toExpression());
        }
        return emitConstructorClass(
                className,
                importer.toType(baseClass),
                importer,
                Collections.singletonList(f.newArrayCreationExpression(
                        (ArrayType) importer.toType(DirectOutputSpec[].class),
                        f.newArrayInitializer(elements))));
    }

    private Name emitWithClass(String classNameString, Class<?> baseClass, Name argumentClassName) throws IOException {
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
        List<Expression> arguments = new ArrayList<Expression>();
        arguments.add(f.newClassLiteral(importer.toType(argumentClassName)));
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
                    .text("Creates a new instance.")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                className,
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(ctorChain));
        ClassDeclaration typeDecl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .Final()
                    .toAttributes(),
                className,
                importer.resolve(baseClass),
                Collections.<Type>emptyList(),
                Collections.singletonList(ctorDecl));
        CompilationUnit source = f.newCompilationUnit(
                importer.getPackageDeclaration(),
                importer.toImportDeclarations(),
                Collections.singletonList(typeDecl),
                Collections.<Comment>emptyList());
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("epilogue of \"{}\" will use {}", moduleId, name);
        return name;
    }

    private Name emitClient(
            List<CompiledSlot> compiledSlots,
            Name key, Name value,
            Name grouping, Name ordering, Location outputLocation) throws IOException {
        assert compiledSlots != null;
        assert key != null;
        assert value != null;
        assert grouping != null;
        assert ordering != null;
        assert outputLocation != null;
        Name partitioner = Models.toName(environment.getModelFactory(), Partitioner.class.getName().replace('$', '.'));
        Name reducer = Models.toName(environment.getModelFactory(), DirectOutputReducer.class.getName());
        Engine engine = new Engine(
                environment,
                moduleId,
                compiledSlots,
                outputLocation,
                key, value,
                grouping, ordering, partitioner,
                reducer);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("\"{}\" will use {}", moduleId, name);
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
                    Collections.singletonList(type),
                    Collections.<Comment>emptyList());
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getClientClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.addAll(createIdMethods());
            members.add(createStageOutputPath());
            members.add(createStageInputsMethod());
            members.add(createStageOutputsMethod());
            members.addAll(createShuffleMethods());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    t(AbstractStageClient.class),
                    Collections.<Type>emptyList(),
                    members);
        }

        private List<MethodDeclaration> createIdMethods() {
            List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
            results.add(createValueMethod(
                    AbstractStageClient.METHOD_BATCH_ID,
                    t(String.class),
                    Models.toLiteral(factory, environment.getBatchId())));
            results.add(createValueMethod(
                    AbstractStageClient.METHOD_FLOW_ID,
                    t(String.class),
                    Models.toLiteral(factory, environment.getFlowId())));
            results.add(createValueMethod(
                    AbstractStageClient.METHOD_STAGE_ID,
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
            SimpleName list = factory.newSimpleName("results");
            SimpleName attributes = factory.newSimpleName("attributes");

            List<Statement> statements = new ArrayList<Statement>();
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
                            .method("put",
                                    Models.toLiteral(factory, entry.getKey()),
                                    Models.toLiteral(factory, entry.getValue()))
                            .toStatement());
                    }
                    for (Location input : info.getLocations()) {
                        statements.add(new ExpressionBuilder(factory, list)
                            .method("add", new TypeBuilder(factory, t(StageInput.class))
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
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private List<MethodDeclaration> createShuffleMethods() {
            List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SHUFFLE_KEY_CLASS,
                    importer.toType(key)));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SHUFFLE_VALUE_CLASS,
                    importer.toType(value)));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_GROUPING_COMPARATOR_CLASS,
                    importer.toType(grouping)));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SORT_COMPARATOR_CLASS,
                    importer.toType(ordering)));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_PARTITIONER_CLASS,
                    importer.toType(partitioner)));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_REDUCER_CLASS,
                    importer.toType(reducer)));
            return results;
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

        private MethodDeclaration createStageOutputsMethod() {
            SimpleName list = factory.newSimpleName("results");

            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageOutput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageOutput.class)), list));

            Type formatType = t(BridgeOutputFormat.class);
            for (CompiledSlot slot : slots) {
                Slot origin = slot.original;
                Expression valueType = factory.newClassLiteral(importer.toType(origin.valueType));
                statements.add(new ExpressionBuilder(factory, list)
                    .method("add", new TypeBuilder(factory, t(StageOutput.class))
                        .newObject(
                                Models.toLiteral(factory, origin.path),
                                factory.newClassLiteral(t(NullWritable.class)),
                                valueType,
                                factory.newClassLiteral(formatType))
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
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text("A client class for \"{0}\".", moduleId)
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
                    Collections.<FormalParameterDeclaration>emptyList(),
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
