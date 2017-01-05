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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.CompiledType;
import com.asakusafw.runtime.stage.collector.SlotDirectMapper;
import com.asakusafw.runtime.stage.collector.SlotDistributor;
import com.asakusafw.runtime.stage.collector.SortableSlot;
import com.asakusafw.runtime.trace.TraceLocation;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Generates Mapper classes for {@link ParallelSortClientEmitter}.
 * @since 0.1.0
 * @version 0.5.1
 */
final class ParallelSortMapperEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ParallelSortMapperEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    ParallelSortMapperEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    public CompiledType emit(String moduleId, ResolvedSlot slot) throws IOException {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(slot, "slot"); //$NON-NLS-1$
        LOG.debug("Generates a mapper for the output \"{}\" in epilogue phase of \"{}\"", //$NON-NLS-1$
                slot.getSource().getOutputName(), moduleId);
        CompilationUnit source;
        if (slot.getSortProperties().isEmpty() && ParallelSortClientEmitter.legacy(environment) == false) {
            DirectEngine engine = new DirectEngine(environment, moduleId, slot);
            source = engine.generate();
        } else {
            DistributeEngine engine = new DistributeEngine(environment, moduleId, slot);
            source = engine.generate();
        }
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("Mapper for output \"{}\" in epilogue phase is {}", //$NON-NLS-1$
                slot.getSource().getOutputName(),
                name);
        return new CompiledType(name);
    }

    private static class DirectEngine {

        private final ResolvedSlot slot;

        private final FlowCompilingEnvironment environment;

        private final String moduleId;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        DirectEngine(FlowCompilingEnvironment envinronment, String moduleId, ResolvedSlot slot) {
            assert envinronment != null;
            assert moduleId != null;
            assert slot != null;
            this.environment = envinronment;
            this.moduleId = moduleId;
            this.slot = slot;
            this.factory = envinronment.getModelFactory();
            Name packageName = Models.append(
                    factory,
                    envinronment.getEpiloguePackageName(moduleId),
                    JavaName.of(slot.getSource().getOutputName()).toMemberName());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    Strategy.TOP_LEVEL);
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getMapClass(0));
            importer.resolvePackageMember(name);
            return factory.newClassDeclaration(
                    new JavadocBuilder(factory)
                        .inline("Mapper for output {0} in epilogue phase.", //$NON-NLS-1$
                                d -> d.code("\"{0}\"", slot.getSource().getOutputName())) //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(TraceLocation.class), createTraceLocationElements())
                        .Public()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    importer.toType(SlotDirectMapper.class),
                    Collections.emptyList(),
                    Collections.singletonList(createOutputName()));
        }

        private Map<String, Expression> createTraceLocationElements() {
            Map<String, Expression> results = new LinkedHashMap<>();
            results.put("batchId", Models.toLiteral(factory, environment.getBatchId())); //$NON-NLS-1$
            results.put("flowId", Models.toLiteral(factory, environment.getFlowId())); //$NON-NLS-1$
            results.put("stageId", Models.toLiteral(factory, Naming.getEpilogueName(moduleId))); //$NON-NLS-1$
            return results;
        }

        private MethodDeclaration createOutputName() {
            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, Models.toLiteral(factory, slot.getSource().getOutputName()))
                .toReturnStatement());
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(Override.class))
                        .Public()
                        .toAttributes(),
                    importer.toType(String.class),
                    factory.newSimpleName(SlotDirectMapper.NAME_GET_OUTPUT_NAME),
                    Collections.emptyList(),
                    statements);
        }
    }

    private static class DistributeEngine {

        private final ResolvedSlot slot;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        DistributeEngine(FlowCompilingEnvironment envinronment, String moduleId, ResolvedSlot slot) {
            assert envinronment != null;
            assert moduleId != null;
            assert slot != null;
            this.slot = slot;
            this.factory = envinronment.getModelFactory();
            Name packageName = Models.append(
                    factory,
                    envinronment.getEpiloguePackageName(moduleId),
                    JavaName.of(slot.getSource().getOutputName()).toMemberName());
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(packageName),
                    Strategy.TOP_LEVEL);
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getMapClass(0));
            importer.resolvePackageMember(name);
            return factory.newClassDeclaration(
                    new JavadocBuilder(factory)
                        .inline("Mapper for output {0} in epilogue phase.", //$NON-NLS-1$
                                d -> d.code("\"{0}\"", slot.getSource().getOutputName())) //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    new TypeBuilder(factory, importer.toType(SlotDistributor.class))
                        .parameterize(importer.toType(slot.getValueClass().getType()))
                        .toType(),
                    Collections.emptyList(),
                    Collections.singletonList(createSlotSpec()));
        }

        private MethodDeclaration createSlotSpec() {
            SimpleName valueName = factory.newSimpleName("value"); //$NON-NLS-1$
            SimpleName slotName = factory.newSimpleName("slot"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, slotName)
                .method(SortableSlot.NAME_BEGIN, Models.toLiteral(factory, slot.getSlotNumber()))
                .toStatement());
            if (slot.getSortProperties().isEmpty()) {
                statements.add(new ExpressionBuilder(factory, slotName)
                    .method(SortableSlot.NAME_ADD_RANDOM)
                    .toStatement());
            }
            for (DataClass.Property property : slot.getSortProperties()) {
                if (property.canNull()) {
                    statements.add(factory.newIfStatement(
                            property.createIsNull(valueName),
                            factory.newBlock(new Statement[] {
                                    new ExpressionBuilder(factory, slotName)
                                        .method(SortableSlot.NAME_ADD_BYTE, Models.toLiteral(factory, 1))
                                        .toStatement(),
                                    new ExpressionBuilder(factory, slotName)
                                        .method(SortableSlot.NAME_ADD_RANDOM)
                                        .toStatement(),
                            }),
                            factory.newBlock(new Statement[] {
                                    new ExpressionBuilder(factory, slotName)
                                        .method(SortableSlot.NAME_ADD_BYTE, Models.toLiteral(factory, 0))
                                        .toStatement(),
                                    new ExpressionBuilder(factory, slotName)
                                        .method(SortableSlot.NAME_ADD, property.createGetter(valueName))
                                        .toStatement(),
                            })));
                } else {
                    statements.add(new ExpressionBuilder(factory, slotName)
                        .method(SortableSlot.NAME_ADD, property.createGetter(valueName))
                        .toStatement());
                }
            }
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(Override.class))
                        .Protected()
                        .toAttributes(),
                    Collections.emptyList(),
                    importer.toType(void.class),
                    factory.newSimpleName(SlotDistributor.NAME_SET_SLOT_SPEC),
                    Arrays.asList(new FormalParameterDeclaration[] {
                            factory.newFormalParameterDeclaration(
                                    importer.toType(slot.getValueClass().getType()),
                                    valueName),
                            factory.newFormalParameterDeclaration(
                                    importer.toType(SortableSlot.class),
                                    slotName),
                    }),
                    0,
                    Collections.singletonList(importer.toType(IOException.class)),
                    factory.newBlock(statements));
        }
    }
}
