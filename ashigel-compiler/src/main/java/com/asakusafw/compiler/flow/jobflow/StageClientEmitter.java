/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Delivery;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Process;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Reduce;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.SideData;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Source;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Stage;
import com.asakusafw.runtime.stage.AbstractStageClient;
import com.asakusafw.runtime.stage.StageInput;
import com.asakusafw.runtime.stage.StageOutput;
import com.asakusafw.runtime.stage.StageResource;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
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
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * ステージクライアントクラスを生成する。
 */
public class StageClientEmitter {

    static final Logger LOG = LoggerFactory.getLogger(StageClientEmitter.class);

    private FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StageClientEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のステージ情報を元にステージクライアントクラスを生成し、生成したステージの情報を返す。
     * @param stage ステージ情報
     * @return ステージクライアントクラス
     * @throws IOException 生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledStage emit(JobflowModel.Stage stage) throws IOException {
        Precondition.checkMustNotBeNull(stage, "stage"); //$NON-NLS-1$
        LOG.debug("{}に対するジョブ実行クライアントを生成します", stage);
        Engine engine = new Engine(environment, stage);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("{}のジョブ実行には{}が利用されます", stage, name);
        return new CompiledStage(name, Naming.getStageName(stage.getNumber()));
    }

    private static class Engine {

        private static final char PATH_SEPARATOR = '/';

        private FlowCompilingEnvironment environment;

        private Stage stage;

        private ModelFactory factory;

        private ImportBuilder importer;

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
            members.add(createStageResourcesMethod());
            if (stage.getReduceOrNull() != null) {
                members.addAll(createShuffleMethods());
            }
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
            SimpleName list = factory.newSimpleName("results");

            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageInput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageInput.class)), list));
            for (Process process : stage.getProcesses()) {
                Expression mapperType = dotClass(process.getMapperTypeName());
                for (Source source : process.getResolvedSources()) {
                    Class<?> inputFormatType = source.getInputFormatType();
                    for (Location location : source.getLocations()) {
                        statements.add(new ExpressionBuilder(factory, list)
                            .method("add", new TypeBuilder(factory, t(StageInput.class))
                                .newObject(
                                        Models.toLiteral(factory, location.toPath('/')),
                                        factory.newClassLiteral(t(inputFormatType)),
                                        mapperType)
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

        private MethodDeclaration createStageOutputsMethod() {
            SimpleName list = factory.newSimpleName("results");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageOutput.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageOutput.class)), list));
            for (Delivery process : stage.getDeliveries()) {
                Expression valueType = factory.newClassLiteral(t(process.getDataType()));
                Class<?> outputFormatType = process.getOutputFormatType();
                for (Location location : process.getLocations()) {
                    statements.add(new ExpressionBuilder(factory, list)
                        .method("add", new TypeBuilder(factory, t(StageOutput.class))
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
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private TypeBodyDeclaration createStageResourcesMethod() {
            SimpleName list = factory.newSimpleName("results");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new TypeBuilder(factory, t(ArrayList.class, t(StageResource.class)))
                .newObject()
                .toLocalVariableDeclaration(t(List.class, t(StageResource.class)), list));
            for (SideData sideData : stage.getSideData()) {
                statements.add(new ExpressionBuilder(factory, list)
                    .method("add", new TypeBuilder(factory, t(StageResource.class))
                        .newObject(
                                Models.toLiteral(factory, sideData.getClusterPath().toPath('/')),
                                Models.toLiteral(factory, sideData.getLocalName()))
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
                    t(List.class, t(StageResource.class)),
                    factory.newSimpleName(AbstractStageClient.METHOD_STAGE_RESOURCES),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private List<MethodDeclaration> createShuffleMethods() {
            Reduce reduce = stage.getReduceOrNull();
            List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
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
                .text("ステージ{0}のジョブを実行するクライアント。", stage.getNumber())
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

        private Expression dotClass(Name name) {
            assert name != null;
            return factory.newClassLiteral(importer.toType(name));
        }
    }
}
