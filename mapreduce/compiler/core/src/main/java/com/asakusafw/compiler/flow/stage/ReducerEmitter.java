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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedReducer;
import com.asakusafw.runtime.flow.SegmentedWritable;
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
import com.asakusafw.vocabulary.flow.graph.FlowElement;

/**
 * An emitter for emitting Reducer classes.
 */
public class ReducerEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ReducerEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ReducerEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new Reducer class, and returns the qualified name of its class.
     * @param model the target stage
     * @return qualified name of the created class
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public CompiledType emit(StageModel model) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        LOG.debug("start generating reducer class: {}", model); //$NON-NLS-1$

        Engine engine = new Engine(environment, model);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();

        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);

        LOG.debug("finish generating reducer class: {} ({})", model, name); //$NON-NLS-1$
        return new CompiledType(name);
    }

    private static class Engine {

        private final FlowCompilingEnvironment environment;

        private final StageModel model;

        private final ShuffleModel shuffle;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final NameGenerator names;

        private final FragmentFlow fragments;

        private final SimpleName context;

        Engine(FlowCompilingEnvironment environment, StageModel model) {
            assert environment != null;
            assert model != null;
            this.environment = environment;
            this.model = model;
            this.shuffle = model.getShuffleModel();
            this.factory = environment.getModelFactory();
            Name packageName = environment.getStagePackageName(
                    model.getStageBlock().getStageNumber());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    ImportBuilder.Strategy.TOP_LEVEL);
            this.names = new NameGenerator(factory);
            this.fragments = new FragmentFlow(
                    environment,
                    importer,
                    names,
                    model,
                    model.getReduceUnits());
            this.context = names.create("context"); //$NON-NLS-1$
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getReduceClass());
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(fragments.createFields());
            members.add(createSetup());
            members.add(createCleanup());
            members.add(createGetRendezvous());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(t(TraceLocation.class), createTraceLocationElements())
                        .annotation(t(SuppressWarnings.class), v("deprecation")) //$NON-NLS-1$
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    importer.resolve(factory.newParameterizedType(
                            Models.toType(factory, SegmentedReducer.class),
                            Arrays.asList(
                                    fragments.getShuffleKeyType(),
                                    fragments.getShuffleValueType(),
                                    t(NullWritable.class),
                                    t(NullWritable.class)))),
                    Collections.emptyList(),
                    members);
        }

        private Map<String, Expression> createTraceLocationElements() {
            Map<String, Expression> results = new LinkedHashMap<>();
            results.put("batchId", Models.toLiteral(factory, environment.getBatchId())); //$NON-NLS-1$
            results.put("flowId", Models.toLiteral(factory, environment.getFlowId())); //$NON-NLS-1$
            results.put("stageId", //$NON-NLS-1$
                    Models.toLiteral(factory, Naming.getStageName(model.getStageBlock().getStageNumber())));
            results.put("stageUnitId", Models.toLiteral(factory, "r")); //$NON-NLS-1$ //$NON-NLS-2$
            return results;
        }

        private MethodDeclaration createSetup() {
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    t(void.class),
                    factory.newSimpleName("setup"), //$NON-NLS-1$
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            factory.newNamedType(factory.newSimpleName("Context")), //$NON-NLS-1$
                            context)),
                    0,
                    Arrays.asList(t(IOException.class), t(InterruptedException.class)),
                    factory.newBlock(fragments.createSetup(context)));
        }

        private MethodDeclaration createCleanup() {
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    t(void.class),
                    factory.newSimpleName("cleanup"), //$NON-NLS-1$
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            factory.newNamedType(factory.newSimpleName("Context")), //$NON-NLS-1$
                            context)),
                    0,
                    Arrays.asList(t(IOException.class), t(InterruptedException.class)),
                    factory.newBlock(fragments.createCleanup(context)));
        }

        private MethodDeclaration createGetRendezvous() {
            List<Statement> cases = new ArrayList<>();
            for (List<ShuffleModel.Segment> group : ShuffleEmiterUtil.groupByElement(shuffle)) {
                for (ShuffleModel.Segment segment : group) {
                    cases.add(factory.newSwitchCaseLabel(v(segment.getPortId())));
                }
                FlowElement element = group.get(0).getPort().getOwner();
                cases.add(new ExpressionBuilder(factory, fragments.getRendezvous(element))
                    .toReturnStatement());
            }
            cases.add(factory.newSwitchDefaultLabel());
            cases.add(new TypeBuilder(factory, t(AssertionError.class))
                .newObject()
                .toThrowStatement());

            SimpleName argument = names.create("nextKey"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(factory.newSwitchStatement(
                    new ExpressionBuilder(factory, argument)
                        .method(SegmentedWritable.ID_GETTER)
                        .toExpression(),
                    cases));

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Protected()
                        .toAttributes(),
                    importer.resolve(factory.newParameterizedType(
                            Models.toType(factory, Rendezvous.class),
                            Arrays.asList(fragments.getShuffleValueType()))),
                    factory.newSimpleName(SegmentedReducer.GET_RENDEZVOUS),
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            fragments.getShuffleKeyType(),
                            argument)),
                    statements);
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("A reducer class for processing stage {0}.", //$NON-NLS-1$
                        d -> d.code(shuffle.getStageBlock().getStageNumber()))
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
