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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.runtime.stage.AbstractCleanupStageClient;
import com.asakusafw.runtime.stage.BaseStageClient;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Creates a subclass of {@link AbstractCleanupStageClient}.
 * @since 0.2.6
 */
public class CleanupStageClientEmitter {

    static final Logger LOG = LoggerFactory.getLogger(CleanupStageClientEmitter.class);

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment current compilation environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CleanupStageClientEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Emits a new cleanup stage client.
     * @return the compiled class info
     * @throws IOException if faled to emit a class
     */
    public CompiledStage emit() throws IOException {
        LOG.debug("Generating cleanup stage client for {}", environment.getFlowId()); //$NON-NLS-1$
        Engine engine = new Engine(environment);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("Cleanup stage client for {} will be {}", environment.getFlowId(), name); //$NON-NLS-1$
        return new CompiledStage(name, Naming.getCleanupStageName());
    }

    private static class Engine {

        private static final char PATH_SEPARATOR = '/';

        private final FlowCompilingEnvironment environment;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        private final QualifiedName fqn;

        Engine(FlowCompilingEnvironment environment) {
            assert environment != null;
            this.environment = environment;
            this.factory = environment.getModelFactory();
            this.fqn = (QualifiedName) Models.toName(factory, AbstractCleanupStageClient.IMPLEMENTATION);
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(fqn.getQualifier()),
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
            importer.resolvePackageMember(fqn.getSimpleName());
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(createIdMethods());
            members.add(createStageOutputPath());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .Final()
                        .toAttributes(),
                    fqn.getSimpleName(),
                    Collections.emptyList(),
                    t(AbstractCleanupStageClient.class),
                    Collections.emptyList(),
                    members);
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
                    Models.toLiteral(factory, Naming.getCleanupStageName())));
            return results;
        }

        private MethodDeclaration createStageOutputPath() {
            Location location = environment.getTargetLocation();
            location = getCleanupTarget(location);
            String path = location.toPath(PATH_SEPARATOR);
            return createValueMethod(
                    AbstractCleanupStageClient.METHOD_CLEANUP_PATH,
                    t(String.class),
                    Models.toLiteral(factory, path));
        }

        private Location getCleanupTarget(Location location) {
            Location candidate = location;
            Location current = location;
            while (current != null) {
                String name = current.getName();
                if (name.indexOf(StageConstants.EXPR_EXECUTION_ID) >= 0) {
                    candidate = current;
                }
                current = current.getParent();
            }
            return candidate;
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text(Messages.getString("CleanupStageClientEmitter.javadocClass")) //$NON-NLS-1$
                .toJavadoc();
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
    }
}
