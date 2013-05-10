/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowElementProcessor.Kind;
import com.asakusafw.compiler.flow.FlowElementProcessor.ResultMirror;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
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
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.operator.Identity;

/**
 * Shuffleを必要としない1入力の要素を処理するフラグメントクラスを生成するエミッタ。
 */
public class MapFragmentEmitter {

    static final Logger LOG = LoggerFactory.getLogger(MapFragmentEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public MapFragmentEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のフラグメントに対するクラスを生成し、生成したクラスの完全限定名を返す。
     * @param fragment 処理対象のフラグメント (Rendezvousでない)
     * @param stageBlock 対象のフラグメントが存在するステージ
     * @return 生成したクラスの完全限定名
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledType emit(
            StageModel.Fragment fragment,
            StageBlock stageBlock) throws IOException {
        Precondition.checkMustNotBeNull(fragment, "fragment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        if (fragment.isRendezvous()) {
            throw new IllegalArgumentException();
        }
        LOG.debug("{}に対するフラグメントクラスを生成します", fragment);

        Engine engine = new Engine(environment, stageBlock, fragment);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();

        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);

        LOG.debug("{}の処理には{}が利用されます", fragment, name);
        return new CompiledType(name);
    }

    private static class Engine {

        private final FlowCompilingEnvironment environment;

        private final Fragment fragment;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final NameGenerator names;

        private final FragmentConnection connection;

        private final List<FieldDeclaration> extraFields = Lists.create();

        Engine(
                FlowCompilingEnvironment environment,
                StageBlock stageBlock,
                Fragment fragment) {
            assert environment != null;
            assert stageBlock != null;
            assert fragment != null;
            this.environment = environment;
            this.fragment = fragment;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(stageBlock.getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.names = new NameGenerator(factory);
            this.connection = new FragmentConnection(environment, fragment, names, importer);
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
            SimpleName name = factory.newSimpleName(
                    Naming.getMapFragmentClass(fragment.getSerialNumber()));
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = Lists.create();
            members.addAll(connection.createFields());
            ConstructorDeclaration ctor = connection.createConstructor(name);
            MethodDeclaration method = createBody();
            members.addAll(extraFields);
            members.add(ctor);
            members.add(method);
            Type inputType = createInputType();
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation"))
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(importer.resolve(
                            factory.newParameterizedType(
                                    t(Result.class),
                                    Collections.singletonList(inputType)))),
                    members);
        }

        private MethodDeclaration createBody() {
            SimpleName argument = names.create("result");
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
                            createInputType(),
                            argument)),
                    statements);
        }

        private List<Statement> createStatements(SimpleName argument) {
            assert argument != null;
            List<Statement> results = Lists.create();
            boolean end = false;
            Expression input = argument;
            Iterator<Factor> factors = fragment.getFactors().iterator();
            while (factors.hasNext()) {
                Factor factor = factors.next();
                if (factor.isLineEnd()) {
                    assert factors.hasNext() == false;
                    emitEnd(results, factor, input);
                    end = true;
                } else {
                    input = emitPart(results, factor, input);
                }
            }
            if (end == false) {
                emitImplicitEnd(results, input);
            }
            return results;
        }

        private Expression emitPart(List<Statement> results, Factor factor, Expression input) {
            assert results != null;
            assert factor != null;
            assert input != null;
            FlowElementProcessor proc = factor.getProcessor();
            assert proc.getKind() == Kind.LINE_PART;
            LinePartProcessor processor = (LinePartProcessor) proc;
            LOG.debug("{}に{}を適用しています", factor, processor);

            LinePartProcessor.Context context = createPartConext(factor, input);
            processor.emitLinePart(context);
            return mergePartContext(context, results);
        }

        private void emitEnd(
                List<Statement> statements,
                Factor factor,
                Expression input) {
            assert statements != null;
            assert factor != null;
            assert input != null;
            FlowElementProcessor proc = factor.getProcessor();
            assert proc.getKind() == Kind.LINE_END;
            LineEndProcessor processor = (LineEndProcessor) proc;
            LOG.debug("{}に{}を適用しています", factor, processor);

            LineEndProcessor.Context context = createEndConext(factor, input);
            processor.emitLineEnd(context);
            mergeEndContext(context, statements);
        }

        private void emitImplicitEnd(List<Statement> statements, Expression input) {
            assert statements != null;
            assert input != null;
            LOG.debug("{}の末尾のプログラムを生成します", fragment);

            List<FlowElementOutput> outputs = fragment.getOutputPorts();
            // implicit endになりうるelementの出力ポートは
            // 基本的にひとつ。これが崩れたら修正を行うこと
            assert outputs.size() == 1;

            LineEndProcessor.Context context = createEndConext(null, input);
            ResultMirror result = context.getOutput(outputs.get(0).getDescription());
            context.add(result.createAdd(input));
            mergeEndContext(context, statements);
        }

        private LinePartProcessor.Context createPartConext(
                Factor factor,
                Expression input) {
            assert factor != null;
            assert input != null;
            FlowElementDescription description = factor.getElement().getDescription();
            if ((description instanceof OperatorDescription) == false) {
                description = new OperatorDescription.Builder(Identity.class)
                    .declare(Void.class, Void.class, "")
                    .addInput("input", Object.class)
                    .addOutput("output", Object.class)
                    .toDescription();
            }
            return new LinePartProcessor.Context(
                    environment,
                    factor.getElement(),
                    importer,
                    names,
                    (OperatorDescription) description,
                    input,
                    connection.getResources());
        }

        private Expression mergePartContext(
                LinePartProcessor.Context context,
                List<Statement> statements) {
            assert context != null;
            statements.addAll(context.getGeneratedStatements());
            extraFields.addAll(context.getGeneratedFields());
            return context.getOutput();
        }

        private LineEndProcessor.Context createEndConext(
                Factor factorOrNull,
                Expression input) {
            assert input != null;

            OperatorDescription description;
            if (factorOrNull == null) {
                description = new OperatorDescription.Builder(Identity.class)
                    .declare(Void.class, Void.class, "")
                    .addInput("input", Object.class)
                    .addOutput("output", Object.class)
                    .toDescription();
            } else {
                FlowElementDescription desc = factorOrNull.getElement().getDescription();
                if ((desc instanceof OperatorDescription) == false) {
                    throw new IllegalArgumentException(desc.toString());
                }
                description = (OperatorDescription) desc;
            }
            return new LineEndProcessor.Context(
                    environment,
                    factorOrNull == null ? description : factorOrNull.getElement(),
                    importer,
                    names,
                    description,
                    input,
                    connection.getOutputs(),
                    connection.getResources());
        }

        private void mergeEndContext(
                LineEndProcessor.Context context,
                List<Statement> statements) {
            assert context != null;
            assert statements != null;
            statements.addAll(context.getGeneratedStatements());
            extraFields.addAll(context.getGeneratedFields());
        }

        private Type createInputType() {
            List<FlowElementInput> inputs = fragment.getInputPorts();
            assert inputs.size() == 1;
            return t(inputs.get(0).getDescription().getDataType());
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .code("{0}", fragment.getInputPorts())
                .text("の処理を担当するマッププログラムの断片。")
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
