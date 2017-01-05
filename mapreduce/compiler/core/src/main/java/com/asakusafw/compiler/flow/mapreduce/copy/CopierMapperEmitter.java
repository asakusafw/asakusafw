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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.CompiledType;
import com.asakusafw.runtime.stage.preparator.PreparatorMapper;
import com.asakusafw.runtime.trace.TraceLocation;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
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

/**
 * Generates copier mappers.
 * @since 0.2.5
 * @version 0.5.1
 */
final class CopierMapperEmitter {

    static final Logger LOG = LoggerFactory.getLogger(CopierMapperEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment current environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    CopierMapperEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Emits a mapper for the slot.
     * @param moduleId target module ID
     * @param slot target input
     * @param prologue whether this will run in the prologue or epilogue phase
     * @return the generated class symbol
     * @throws IOException if failed to generate the target class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompiledType emit(String moduleId, CopyDescription slot, boolean prologue) throws IOException {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(slot, "slot"); //$NON-NLS-1$
        LOG.debug("Generates a mapper for \"{}\" in \"{}\"", //$NON-NLS-1$
                slot.getName(), moduleId);
        CompilationUnit source;
        Engine engine = new Engine(environment, moduleId, slot, prologue);
        source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("Mapper for \"{}\" is {}", //$NON-NLS-1$
                slot.getName(),
                name);
        return new CompiledType(name);
    }

    private static class Engine {

        private final FlowCompilingEnvironment environment;

        private final String moduleId;

        private final boolean prologue;

        private final CopyDescription slot;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        Engine(FlowCompilingEnvironment envinronment, String moduleId, CopyDescription slot, boolean prologue) {
            assert envinronment != null;
            assert moduleId != null;
            assert slot != null;
            this.environment = envinronment;
            this.moduleId = moduleId;
            this.prologue = prologue;
            this.slot = slot;
            this.factory = envinronment.getModelFactory();
            Name packageName = Models.append(
                    factory,
                    prologue
                        ? envinronment.getProloguePackageName(moduleId)
                        : envinronment.getEpiloguePackageName(moduleId),
                    JavaName.of(slot.getName()).toMemberName());
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
                        .inline("Mapper for input {0} in prologue phase.",
                                d -> d.code("\"{0}\"", slot.getName())) //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(TraceLocation.class), createTraceLocationElements())
                        .Public()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    factory.newParameterizedType(
                            importer.toType(PreparatorMapper.class),
                            importer.toType(slot.getDataModel().getType())),
                    Collections.emptyList(),
                    Collections.singletonList(createOutputName()));
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

        private MethodDeclaration createOutputName() {
            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, Models.toLiteral(factory, slot.getName()))
                .toReturnStatement());
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(Override.class))
                        .Public()
                        .toAttributes(),
                    importer.toType(String.class),
                    factory.newSimpleName(PreparatorMapper.NAME_GET_OUTPUT_NAME),
                    Collections.emptyList(),
                    statements);
        }
    }
}
