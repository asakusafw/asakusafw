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
package com.asakusafw.compiler.flow.jobflow;

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
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Delivery;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Process;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Reduce;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.SideData;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Source;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.runtime.stage.BaseStageClient;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.stage.StageResource;
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
 * Generates Hadoop stage client classes.
 */
public class StageClientEmitter {

    static final Logger LOG = LoggerFactory.getLogger(StageClientEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public StageClientEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Emits a Hadoop stage client class for the target stage.
     * @param stage the stage model
     * @return the qualified name of the generated stage client class
     * @throws IOException if error occurred while generating sources
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public CompiledStage emit(JobflowModel.Stage stage) throws IOException {
        Precondition.checkMustNotBeNull(stage, "stage"); //$NON-NLS-1$
        LOG.debug("start generating Hadoop job client: {}", stage); //$NON-NLS-1$
        Engine engine = new Engine(environment, stage);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("finish generating Hadoop job client {} ({})", stage, name); //$NON-NLS-1$
        return new CompiledStage(name, Naming.getStageName(stage.getNumber()));
    }

    private static class Engine {

        private static final char PATH_SEPARATOR = '/';

        private final FlowCompilingEnvironment environment;

        private final Stage stage;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        Engine(FlowCompilingEnvironment environment, Stage stage) {
            assert environment != null;
            assert stage != null;
            this.environment = environment;
            this.stage = stage;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(stage.getNumber());
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
            SimpleName name = factory.newSimpleName(Naming.getClientClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createIdMethods());
            members.add(createStageOutputPath());
            members.add(createStageInputsMethod());
            members.add(createStageOutputsMethod());
            members.add(createStageResourcesMethod());
            if (stage.getReduceOrNull() != null) {
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
            results.put("stageId", Models.toLiteral(factory, Naming.getStageName(stage.getNumber()))); //$NON-NLS-1$
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
                    Models.toLiteral(factory, Naming.getStageName(stage.getNumber()))));
            return results;
        }

        private MethodDeclaration createStageOutputPath() {
            String path = environment
                .getStageLocation(stage.getNumber())
                .toPath(PATH_SEPARATOR);
            return createValueMethod(
                    AbstractStageClient.METHOD_STAGE_OUTPUT_PATH,
                    t(String.class),
                    Models.toLiteral(factory, path));
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

            for (Process process : stage.getProcesses()) {
                Expression mapperType = dotClass(process.getMapperTypeName());
                for (Source source : process.getResolvedSources()) {
                    SourceInfo info = source.getInputInfo();
                    Class<?> inputFormatType = info.getFormat();
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
                    for (Location location : info.getLocations()) {
                        statements.add(new ExpressionBuilder(factory, list)
                            .method("add", new TypeBuilder(factory, t(StageInput.class)) //$NON-NLS-1$
                                .newObject(
                                        Models.toLiteral(factory, location.toPath('/')),
                                        factory.newClassLiteral(t(inputFormatType)),
                                        mapperType,
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
            for (Delivery process : stage.getDeliveries()) {
                Expression valueType = factory.newClassLiteral(t(process.getDataType()));
                Class<?> outputFormatType = process.getOutputFormatType();
                for (Location location : process.getInputInfo().getLocations()) {
                    statements.add(new ExpressionBuilder(factory, list)
                        .method("add", new TypeBuilder(factory, t(StageOutput.class)) //$NON-NLS-1$
                            .newObject(
                                    Models.toLiteral(factory, location.getName()),
                                    factory.newClassLiteral(t(NullWritable.class)),
                                    valueType,
                                    factory.newClassLiteral(t(outputFormatType)))
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
                    t(List.class, t(StageOutput.class)),
                    factory.newSimpleName(AbstractStageClient.METHOD_STAGE_OUTPUTS),
                    Collections.emptyList(),
                    statements);
        }

        private TypeBodyDeclaration createStageResourcesMethod() {
            SimpleName list = factory.newSimpleName("results"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageResource.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageResource.class)), list));
            for (SideData sideData : stage.getSideData()) {
                for (Location location : sideData.getClusterPaths()) {
                    statements.add(new ExpressionBuilder(factory, list)
                        .method("add", new TypeBuilder(factory, t(StageResource.class)) //$NON-NLS-1$
                            .newObject(
                                    Models.toLiteral(factory, location.toPath('/')),
                                    Models.toLiteral(factory, sideData.getLocalName()))
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
                    t(List.class, t(StageResource.class)),
                    factory.newSimpleName(AbstractStageClient.METHOD_STAGE_RESOURCES),
                    Collections.emptyList(),
                    statements);
        }

        private List<MethodDeclaration> createShuffleMethods() {
            Reduce reduce = stage.getReduceOrNull();
            List<MethodDeclaration> results = new ArrayList<>();
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SHUFFLE_KEY_CLASS,
                    importer.toType(reduce.getKeyTypeName())));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SHUFFLE_VALUE_CLASS,
                    importer.toType(reduce.getValueTypeName())));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_PARTITIONER_CLASS,
                    importer.toType(reduce.getPartitionerTypeName())));
            if (reduce.getCombinerTypeNameOrNull() != null) {
                results.add(createClassLiteralMethod(
                        AbstractStageClient.METHOD_COMBINER_CLASS,
                        importer.toType(reduce.getCombinerTypeNameOrNull())));
            }
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_SORT_COMPARATOR_CLASS,
                    importer.toType(reduce.getSortComparatorTypeName())));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_GROUPING_COMPARATOR_CLASS,
                    importer.toType(reduce.getGroupingComparatorTypeName())));
            results.add(createClassLiteralMethod(
                    AbstractStageClient.METHOD_REDUCER_CLASS,
                    importer.toType(reduce.getReducerTypeName())));
            return results;
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text(Messages.getString("StageClientEmitter.javadocClass"), stage.getNumber()) //$NON-NLS-1$
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

        private Expression dotClass(Name name) {
            assert name != null;
            return factory.newClassLiteral(importer.toType(name));
        }
    }
}
