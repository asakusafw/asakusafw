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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import com.asakusafw.compiler.flow.FlowElementProcessor.ResultMirror;
import com.asakusafw.compiler.flow.LineEndProcessor;
import com.asakusafw.compiler.flow.LinePartProcessor;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.trace.TraceLocation;
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
 * An emitter which emits fragments of a map action.
 */
public class MapFragmentEmitter {

    static final Logger LOG = LoggerFactory.getLogger(MapFragmentEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public MapFragmentEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Creates a new map fragment class, and returns the qualified name of its class.
     * @param fragment the target fragment (must not be a rendezvous)
     * @param stageBlock the target stage
     * @return qualified name of the created class
     * @throws IOException if error was occurred while creating the class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledType emit(StageModel.Fragment fragment, StageBlock stageBlock) throws IOException {
        Precondition.checkMustNotBeNull(fragment, "fragment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        if (fragment.isRendezvous()) {
            throw new IllegalArgumentException();
        }
        LOG.debug("start generating mapper fragment: {}", fragment); //$NON-NLS-1$

        Engine engine = new Engine(environment, stageBlock, fragment);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();

        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);

        LOG.debug("finish generating mapper fragment: {} ({})", fragment, name); //$NON-NLS-1$
        return new CompiledType(name);
    }

    private static class Engine {

        private final FlowCompilingEnvironment environment;

        private final StageBlock stageBlock;

        private final Fragment fragment;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final NameGenerator names;

        private final FragmentConnection connection;

        private final List<FieldDeclaration> extraFields = new ArrayList<>();

        Engine(
                FlowCompilingEnvironment environment,
                StageBlock stageBlock,
                Fragment fragment) {
            assert environment != null;
            assert stageBlock != null;
            assert fragment != null;
            this.environment = environment;
            this.stageBlock = stageBlock;
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
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(
                    Naming.getMapFragmentClass(fragment.getSerialNumber()));
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
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
                        .annotation(t(TraceLocation.class), createTraceLocationElements())
                        .annotation(t(SuppressWarnings.class), v("deprecation")) //$NON-NLS-1$
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    null,
                    Collections.singletonList(importer.resolve(
                            factory.newParameterizedType(
                                    t(Result.class),
                                    Collections.singletonList(inputType)))),
                    members);
        }

        private Map<String, Expression> createTraceLocationElements() {
            Map<String, Expression> results = new LinkedHashMap<>();
            results.put("batchId", //$NON-NLS-1$
                    Models.toLiteral(factory, environment.getBatchId()));
            results.put("flowId", //$NON-NLS-1$
                    Models.toLiteral(factory, environment.getFlowId()));
            results.put("stageId", //$NON-NLS-1$
                    Models.toLiteral(factory, Naming.getStageName(stageBlock.getStageNumber())));
            results.put("fragmentId", //$NON-NLS-1$
                    Models.toLiteral(factory, String.valueOf(fragment.getSerialNumber())));
            return results;
        }

        private MethodDeclaration createBody() {
            SimpleName argument = names.create("result"); //$NON-NLS-1$
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
            List<Statement> results = new ArrayList<>();
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
            LOG.debug("applying {}: {}", processor, factor); //$NON-NLS-1$

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
            LOG.debug("applying {}: {}", processor, factor); //$NON-NLS-1$

            LineEndProcessor.Context context = createEndConext(factor, input);
            processor.emitLineEnd(context);
            mergeEndContext(context, statements);
        }

        private void emitImplicitEnd(List<Statement> statements, Expression input) {
            assert statements != null;
            assert input != null;
            LOG.debug("generating map fragment terminator: {}", fragment); //$NON-NLS-1$

            List<FlowElementOutput> outputs = fragment.getOutputPorts();
            assert outputs.size() == 1 : "number of implicit end output port must be = 1"; //$NON-NLS-1$

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
                    .declare(Void.class, Void.class, "") //$NON-NLS-1$
                    .addInput("input", Object.class) //$NON-NLS-1$
                    .addOutput("output", Object.class) //$NON-NLS-1$
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
                    .declare(Void.class, Void.class, "") //$NON-NLS-1$
                    .addInput("input", Object.class) //$NON-NLS-1$
                    .addOutput("output", Object.class) //$NON-NLS-1$
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
                .inline("A mapper fragment for processing {0}.",
                        d -> d.code(fragment.getInputPorts()))
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
