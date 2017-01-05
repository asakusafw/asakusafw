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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.mapreduce.Partitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Arrangement;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Term;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
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
 * An emitter for emitting shuffle partitioner classes.
 */
public class ShufflePartitionerEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ShufflePartitionerEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ShufflePartitionerEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new shuffle partitioner class, and returns the qualified name of its class.
     * @param model the target shuffle model
     * @param keyTypeName the qualified name of the target shuffle key class
     * @param valueTypeName the qualified name of the target shuffle value class
     * @return qualified name of the created class
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public Name emit(
            ShuffleModel model,
            Name keyTypeName,
            Name valueTypeName) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(keyTypeName, "keyTypeName"); //$NON-NLS-1$
        LOG.debug("start creating shuffle partitioner: {}", model.getStageBlock()); //$NON-NLS-1$
        Engine engine = new Engine(environment, model, keyTypeName, valueTypeName);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("finish creating shuffle partitioner: {} ({})", model.getStageBlock(), name); //$NON-NLS-1$
        return name;
    }

    private static class Engine {

        private static final String HASH_CODE_METHOD_NAME = "getHashCode"; //$NON-NLS-1$

        private final ShuffleModel model;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final Type keyType;

        private final Type valueType;

        Engine(
                FlowCompilingEnvironment environment,
                ShuffleModel model,
                Name keyTypeName,
                Name valueTypeName) {
            assert environment != null;
            assert model != null;
            assert keyTypeName != null;
            assert valueTypeName != null;
            this.model = model;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(model.getStageBlock().getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.keyType = importer.resolve(factory.newNamedType(keyTypeName));
            this.valueType = importer.resolve(factory.newNamedType(valueTypeName));
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getShufflePartitionerClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(createPartition());
            members.add(createHashCode());
            members.add(ShuffleEmiterUtil.createPortToElement(factory, model));
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(SuppressWarnings.class), v("deprecation")) //$NON-NLS-1$
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    importer.resolve(factory.newParameterizedType(
                            t(Partitioner.class),
                            Arrays.asList(keyType, valueType))),
                    Collections.emptyList(),
                    members);
        }

        private MethodDeclaration createPartition() {
            SimpleName key = factory.newSimpleName("key"); //$NON-NLS-1$
            SimpleName value = factory.newSimpleName("value"); //$NON-NLS-1$
            SimpleName partitions = factory.newSimpleName("numPartitions"); //$NON-NLS-1$

            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method(HASH_CODE_METHOD_NAME, key)
                .apply(InfixOperator.AND, new TypeBuilder(factory, t(Integer.class))
                    .field("MAX_VALUE") //$NON-NLS-1$
                    .toExpression())
                .apply(InfixOperator.REMAINDER, partitions)
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName("getPartition"), //$NON-NLS-1$
                    Arrays.asList(new FormalParameterDeclaration[] {
                            factory.newFormalParameterDeclaration(keyType, key),
                            factory.newFormalParameterDeclaration(valueType, value),
                            factory.newFormalParameterDeclaration(t(int.class), partitions),
                    }),
                    statements);
        }

        private MethodDeclaration createHashCode() {
            SimpleName key = factory.newSimpleName("key"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            SimpleName portId = factory.newSimpleName("portId"); //$NON-NLS-1$
            SimpleName result = factory.newSimpleName("result"); //$NON-NLS-1$

            statements.add(new ExpressionBuilder(factory, key)
                .method(SegmentedWritable.ID_GETTER)
                .toLocalVariableDeclaration(t(int.class), portId));
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method(ShuffleEmiterUtil.PORT_TO_ELEMENT, portId)
                .toLocalVariableDeclaration(t(int.class), result));

            List<Statement> cases = new ArrayList<>();
            for (Segment segment : model.getSegments()) {
                cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                for (Term term : segment.getTerms()) {
                    if (term.getArrangement() != Arrangement.GROUPING) {
                        continue;
                    }
                    Expression hash = term.getSource().createHashCode(
                            new ExpressionBuilder(factory, key)
                                .field(ShuffleEmiterUtil.getPropertyName(segment, term))
                                .toExpression());
                    cases.add(new ExpressionBuilder(factory, result)
                        .assignFrom(
                                new ExpressionBuilder(factory, result)
                                    .apply(InfixOperator.TIMES, v(31))
                                    .apply(InfixOperator.PLUS, hash)
                                    .toExpression())
                        .toStatement());
                }
                cases.add(factory.newBreakStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject(portId)
                .toThrowStatement());

            statements.add(factory.newSwitchStatement(portId, cases));
            statements.add(new ExpressionBuilder(factory, result)
                .toReturnStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .toAttributes(),
                    t(int.class),
                    factory.newSimpleName(HASH_CODE_METHOD_NAME),
                    Collections.singletonList(
                            factory.newFormalParameterDeclaration(keyType, key)),
                    statements);
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("The shuffle partitioner class for stage {0}.", //$NON-NLS-1$
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
