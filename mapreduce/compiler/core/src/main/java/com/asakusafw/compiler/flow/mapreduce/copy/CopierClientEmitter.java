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
package com.asakusafw.compiler.flow.mapreduce.copy;

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
 * Generates a class which copies original inputs into temporary area.
 * @since 0.2.5
 * @version 0.5.1
 */
public class CopierClientEmitter {

    static final Logger LOG = LoggerFactory.getLogger(CopierClientEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment current environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CopierClientEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Emits a client for the specified slots.
     * @param moduleId target module ID
     * @param slots target slots
     * @param outputDirectory output directory location
     * @return the created class symbol
     * @throws IOException if failed to create a class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompiledStage emitPrologue(
            String moduleId,
            List<CopyDescription> slots,
            Location outputDirectory) throws IOException {
        return emit(moduleId, slots, outputDirectory, true);
    }

    /**
     * Emits a client for the specified slots.
     * @param moduleId target module ID
     * @param slots target slots
     * @param outputDirectory output directory location
     * @return the created class symbol
     * @throws IOException if failed to create a class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompiledStage emitEpilogue(
            String moduleId,
            List<CopyDescription> slots,
            Location outputDirectory) throws IOException {
        return emit(moduleId, slots, outputDirectory, false);
    }

    private CompiledStage emit(
            String moduleId,
            List<CopyDescription> slots,
            Location outputDirectory,
            boolean prologue) throws IOException {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(slots, "slots"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputDirectory, "outputDirectory"); //$NON-NLS-1$
        LOG.debug("Generates job for {} in {} phase", //$NON-NLS-1$
                moduleId,
                prologue ? "prologue" : "epilogue"); //$NON-NLS-1$ //$NON-NLS-2$
        Engine engine = new Engine(environment, moduleId, slots, outputDirectory, prologue);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("\"{}\" will use {}", moduleId, name); //$NON-NLS-1$
        return new CompiledStage(name, prologue ? Naming.getPrologueName(moduleId) : Naming.getEpilogueName(moduleId));
    }

    private static class Engine {

        private static final char PATH_SEPARATOR = '/';

        private final FlowCompilingEnvironment environment;

        private final String moduleId;

        private final List<CopyDescription> slots;

        private final Location outputDirectory;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final boolean prologue;

        Engine(
                FlowCompilingEnvironment environment,
                String moduleId,
                List<CopyDescription> slots,
                Location outputDirectory,
                boolean prologue) {
            assert environment != null;
            assert moduleId != null;
            assert slots != null;
            this.environment = environment;
            this.moduleId = moduleId;
            this.slots = slots;
            this.outputDirectory = outputDirectory;
            this.prologue = prologue;
            this.factory = environment.getModelFactory();
            Name packageName = prologue
                ? environment.getProloguePackageName(moduleId)
                : environment.getEpiloguePackageName(moduleId);
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
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(TraceLocation.class), createTraceLocationElements())
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
            if (prologue) {
                results.put("stageId", Models.toLiteral(factory, Naming.getPrologueName(moduleId))); //$NON-NLS-1$
            } else {
                results.put("stageId", Models.toLiteral(factory, Naming.getEpilogueName(moduleId))); //$NON-NLS-1$
            }
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
                    Models.toLiteral(factory, prologue
                            ? Naming.getPrologueName(moduleId)
                            : Naming.getEpilogueName(moduleId))));
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

            for (CopyDescription slot : slots) {
                SourceInfo info = slot.getInput();
                Type mapperType = generateMapper(slot);
                Type formatClass = t(info.getFormat());
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
                                    factory.newClassLiteral(formatClass),
                                    factory.newClassLiteral(mapperType),
                                    attributes)
                            .toExpression())
                        .toStatement());
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
            for (CopyDescription slot : slots) {
                Expression valueType = factory.newClassLiteral(t(slot.getDataModel().getType()));
                statements.add(new ExpressionBuilder(factory, list)
                    .method("add", new TypeBuilder(factory, t(StageOutput.class)) //$NON-NLS-1$
                        .newObject(
                                Models.toLiteral(factory, slot.getName()),
                                factory.newClassLiteral(t(NullWritable.class)),
                                valueType,
                                factory.newClassLiteral(t(slot.getOutputFormatType())))
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

        private Type generateMapper(CopyDescription slot) throws IOException {
            assert slot != null;
            CopierMapperEmitter sub = new CopierMapperEmitter(environment);
            CompiledType type = sub.emit(moduleId, slot, prologue);
            return importer.toType(type.getQualifiedName());
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
