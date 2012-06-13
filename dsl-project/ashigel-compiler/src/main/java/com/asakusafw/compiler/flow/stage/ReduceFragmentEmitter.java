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
package com.asakusafw.compiler.flow.stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowElementProcessor.Kind;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
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
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Shuffleを必要とする要素を処理するフラグメントクラスを生成するエミッタ。
 */
public class ReduceFragmentEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ReduceFragmentEmitter.class);

    private FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ReduceFragmentEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のフラグメントに対するクラスを生成し、生成したクラスの完全限定名を返す。
     * @param fragment 処理対象のフラグメント (Rendezvous)
     * @param shuffle シャッフルフェーズの情報 (コンパイル済み)
     * @param stageBlock 対象のフラグメントが存在するステージ
     * @return コンパイル結果
     * @throws IOException クラスの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledType emit(
            StageModel.Fragment fragment,
            ShuffleModel shuffle,
            StageBlock stageBlock) throws IOException {
        Precondition.checkMustNotBeNull(fragment, "fragment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(shuffle, "shuffle"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        if (fragment.isRendezvous() == false) {
            throw new IllegalArgumentException();
        }
        if (shuffle.isCompiled() == false) {
            throw new IllegalArgumentException();
        }
        assert fragment.getFactors().size() == 1;

        LOG.debug("{}に対するフラグメントクラスを生成します", fragment);

        Engine engine = new Engine(environment, stageBlock, fragment, shuffle);
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

        private static final String PROCESS_PREFIX = "process";

        private FlowCompilingEnvironment environment;

        private Fragment fragment;

        private ShuffleModel shuffle;

        private ModelFactory factory;

        private ImportBuilder importer;

        private NameGenerator names;

        private List<FieldDeclaration> extraFields = new ArrayList<FieldDeclaration>();

        private Type valueType;

        private FragmentConnection connection;

        Engine(
                FlowCompilingEnvironment environment,
                StageBlock stageBlock,
                Fragment fragment,
                ShuffleModel shuffle) {
            assert environment != null;
            assert stageBlock != null;
            assert fragment != null;
            assert shuffle != null;
            this.environment = environment;
            this.fragment = fragment;
            this.shuffle = shuffle;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(stageBlock.getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.names = new NameGenerator(factory);
            this.valueType = importer.resolve(factory.newNamedType(
                    shuffle.getCompiled().getValueTypeName()));
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
                    Naming.getReduceFragmentClass(fragment.getSerialNumber()));
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.addAll(connection.createFields());
            ConstructorDeclaration ctor = connection.createConstructor(name);
            List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
            SimpleName value = names.create("value");
            methods.add(createProcess(value));
            methods.addAll(emit(value));
            members.addAll(extraFields);
            members.add(ctor);
            members.addAll(methods);
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation"))
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    factory.newParameterizedType(
                            Models.toType(factory, Rendezvous.class),
                            Arrays.asList(valueType)),
                    Collections.<Type>emptyList(),
                    members);
        }

        private MethodDeclaration createBegin(List<Statement> statements) {
            assert statements != null;
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName(Rendezvous.BEGIN),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private MethodDeclaration createProcess(SimpleName value) {
            assert value != null;
            List<Statement> cases = new ArrayList<Statement>();
            for (FlowElementInput input : fragment.getInputPorts()) {
                Segment segment = shuffle.findSegment(input);
                cases.add(factory.newSwitchCaseLabel(
                        Models.toLiteral(factory, segment.getPortId())));
                Expression model = new ExpressionBuilder(factory, value)
                    .method(Naming.getShuffleValueGetter(segment.getPortId()))
                    .toExpression();
                cases.add(new ExpressionBuilder(factory, factory.newThis())
                    .method(getMethodName(PROCESS_PREFIX, segment), model)
                    .toStatement());
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject(value)
                .toThrowStatement());

            List<Statement> statements = new ArrayList<Statement>();
            statements.add(factory.newSwitchStatement(
                    new ExpressionBuilder(factory, value)
                        .method(SegmentedWritable.ID_GETTER)
                        .toExpression(),
                    cases));

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName(Rendezvous.PROCESS),
                    Arrays.asList(factory.newFormalParameterDeclaration(valueType, value)),
                    statements);
        }

        private MethodDeclaration createEnd(List<Statement> statements) {
            assert statements != null;
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName(Rendezvous.END),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private List<MethodDeclaration> emit(SimpleName argument) {
            assert argument != null;
            assert fragment.getFactors().size() == 1;
            Factor factor = fragment.getFactors().get(0);

            FlowElementProcessor proc = factor.getProcessor();
            assert proc.getKind() == Kind.RENDEZVOUS;
            RendezvousProcessor processor = (RendezvousProcessor) proc;
            LOG.debug("{}に{}を適用しています", factor, processor);

            RendezvousProcessor.Context context = createConext(factor, argument);
            processor.emitRendezvous(context);
            return mergeContext(context, argument);
        }

        private RendezvousProcessor.Context createConext(
                Factor factor,
                SimpleName argument) {
            assert argument != null;

            FlowElementDescription desc = factor.getElement().getDescription();
            if ((desc instanceof OperatorDescription) == false) {
                throw new IllegalArgumentException(desc.toString());
            }
            OperatorDescription description = (OperatorDescription) desc;
            Map<FlowElementPortDescription, Expression> inputs =
                new HashMap<FlowElementPortDescription, Expression>();
            for (FlowElementInput port : factor.getElement().getInputPorts()) {
                inputs.put(port.getDescription(), argument);
            }
            return new RendezvousProcessor.Context(
                    environment,
                    importer,
                    names,
                    description,
                    inputs,
                    connection.getOutputs(),
                    connection.getResources());
        }

        private List<MethodDeclaration> mergeContext(
                RendezvousProcessor.Context context,
                SimpleName argument) {
            assert context != null;
            assert argument != null;
            extraFields.addAll(context.getGeneratedFields());
            List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
            results.add(createBegin(context.getBeginStatements()));
            results.add(createEnd(context.getEndStatements()));
            for (FlowElementInput input : fragment.getInputPorts()) {
                Segment segment = shuffle.findSegment(input);
                MethodDeclaration port = createPort(
                        PROCESS_PREFIX,
                        segment,
                        argument,
                        context.getProcessStatements(input.getDescription()));
                LOG.debug("セグメント{}のメソッドは{}として作成されます", segment, port.getName());
                results.add(port);
            }
            return results;
        }

        private MethodDeclaration createPort(
                String prefix,
                Segment segment,
                SimpleName argument,
                List<Statement> statements) {
            assert prefix != null;
            assert segment != null;
            assert argument != null;
            assert statements != null;
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .toAttributes(),
                    t(void.class),
                    getMethodName(prefix, segment),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            t(segment.getTarget().getType()),
                            argument)),
                    statements);
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

        private SimpleName getMethodName(String prefix, Segment segment) {
            return factory.newSimpleName(String.format(
                    "%s%04d",
                    prefix,
                    segment.getPortId()));
        }
    }
}
