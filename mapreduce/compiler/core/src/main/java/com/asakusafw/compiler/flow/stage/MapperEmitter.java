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
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.runtime.flow.MapperWithRuntimeResource;
import com.asakusafw.runtime.trace.TraceLocation;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
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
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;

/**
 * An emitter for emitting Hadoop Mapper classes.
 */
public class MapperEmitter {

    static final Logger LOG = LoggerFactory.getLogger(MapperEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public MapperEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new mapper class, and returns the qualified name of its class.
     * @param model the target stage model
     * @param unit the target map unit
     * @return qualified name of the created class
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledType emit(StageModel model, StageModel.MapUnit unit) throws IOException {
        Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(unit, "unit"); //$NON-NLS-1$
        LOG.debug("start generating mapper class: {}", unit); //$NON-NLS-1$

        Engine engine = new Engine(environment, model, unit);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();

        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);

        LOG.debug("finish generating mapper class: {} ({})", unit, name); //$NON-NLS-1$
        return new CompiledType(name);
    }

    private static class Engine {

        private final FlowCompilingEnvironment environment;

        private final StageModel model;

        private final StageModel.MapUnit unit;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final NameGenerator names;

        private final FragmentFlow fragments;

        private final SimpleName context;

        private final SimpleName cache;

        private DataClass dataClass;

        Engine(
                FlowCompilingEnvironment environment,
                StageModel model,
                StageModel.MapUnit unit) {
            assert environment != null;
            assert model != null;
            assert unit != null;
            this.environment = environment;
            this.model = model;
            this.unit = unit;
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
                    Collections.singletonList(unit));
            this.context = names.create("context"); //$NON-NLS-1$
            this.cache = names.create("cache"); //$NON-NLS-1$
            this.dataClass = environment
                .getDataClasses()
                .load(getInputTypeAsReflect());
            if (dataClass == null) {
                environment.error(Messages.getString("MapperEmitter.errorMissingDataClass"), //$NON-NLS-1$
                        getInputTypeAsReflect());
                dataClass = new DataClass.Unresolved(factory, getInputTypeAsReflect());
            }
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(
                    Naming.getMapClass(unit.getSerialNumber()));
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(createCache());
            members.addAll(fragments.createFields());
            members.add(createSetup());
            members.add(createCleanup());
            members.add(createRun());
            Type inputType = createInputType();
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
                            Models.toType(factory, MapperWithRuntimeResource.class),
                            Arrays.asList(
                                    t(NullWritable.class),
                                    inputType,
                                    fragments.getShuffleKeyType(),
                                    fragments.getShuffleValueType()))),
                    Collections.emptyList(),
                    members);
        }

        private Map<String, Expression> createTraceLocationElements() {
            Map<String, Expression> results = new LinkedHashMap<>();
            results.put("batchId", Models.toLiteral(factory, environment.getBatchId())); //$NON-NLS-1$
            results.put("flowId", Models.toLiteral(factory, environment.getFlowId())); //$NON-NLS-1$
            results.put("stageId", //$NON-NLS-1$
                    Models.toLiteral(factory, Naming.getStageName(model.getStageBlock().getStageNumber())));
            results.put("stageUnitId", //$NON-NLS-1$
                    Models.toLiteral(factory, "m" + unit.getSerialNumber())); //$NON-NLS-1$
            return results;
        }

        private FieldDeclaration createCache() {
            java.lang.reflect.Type type = dataClass.getType();
            return factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .toAttributes(),
                    t(type),
                    cache,
                    dataClass.createNewInstance(t(type)));
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

        private MethodDeclaration createRun() {
            List<Statement> loop = new ArrayList<>();

            for (FlowBlock.Input input : unit.getInputs()) {
                Expression expr = fragments.getLine(input.getElementPort());
                loop.add(dataClass.assign(cache, new ExpressionBuilder(factory, context)
                    .method("getCurrentValue") //$NON-NLS-1$
                    .toExpression()));
                loop.add(new ExpressionBuilder(factory, expr)
                    .method(FlowElementProcessor.RESULT_METHOD_NAME, cache)
                    .toStatement());
            }

            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method("setup", context) //$NON-NLS-1$
                .toStatement());
            statements.add(factory.newWhileStatement(
                    new ExpressionBuilder(factory, context)
                        .method("nextKeyValue") //$NON-NLS-1$
                        .toExpression(),
                    factory.newBlock(loop)));
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .method("cleanup", context) //$NON-NLS-1$
                .toStatement());

            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.emptyList(),
                    t(void.class),
                    factory.newSimpleName("runInternal"), //$NON-NLS-1$
                    Collections.singletonList(factory.newFormalParameterDeclaration(
                            factory.newNamedType(factory.newSimpleName("Context")), //$NON-NLS-1$
                            context)),
                    0,
                    Arrays.asList(t(IOException.class), t(InterruptedException.class)),
                    factory.newBlock(statements));
        }

        private Type createInputType() {
            return t(getInputTypeAsReflect());
        }

        private java.lang.reflect.Type getInputTypeAsReflect() {
            FlowElementInput port = unit.getInputs().get(0).getElementPort();
            FlowElementPortDescription input = port.getDescription();
            return input.getDataType();
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("A mapper class for processing {0}.",
                        d -> d.code(unit.getInputs()))
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
