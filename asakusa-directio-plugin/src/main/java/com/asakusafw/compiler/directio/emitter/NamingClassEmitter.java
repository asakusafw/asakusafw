/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.directio.emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.directio.OutputPattern.CompiledResourcePattern;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.stage.directio.StringTemplate;
import com.asakusafw.runtime.stage.directio.StringTemplate.Format;
import com.asakusafw.runtime.stage.directio.StringTemplate.FormatSpec;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ConstructorDeclaration;
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
 * Emits {@link StringTemplate} subclasses.
 * @since 0.2.5
 */
public class NamingClassEmitter {

    static final Logger LOG = LoggerFactory.getLogger(NamingClassEmitter.class);

    private final FlowCompilingEnvironment environment;

    private final String moduleId;

    /**
     * Creates a new instance.
     * @param environment current environment
     * @param moduleId target module ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public NamingClassEmitter(FlowCompilingEnvironment environment, String moduleId) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        this.environment = environment;
        this.moduleId = moduleId;
    }

    /**
     * Emits a name generator class.
     * @param outputName the output name
     * @param index the output index
     * @param dataType output data type
     * @param namingInfo naming information
     * @return the generated class name
     * @throws IOException if failed to emit class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Name emit(
            String outputName,
            int index,
            DataClass dataType,
            List<CompiledResourcePattern> namingInfo) throws IOException {
        if (outputName == null) {
            throw new IllegalArgumentException("outputName must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (namingInfo == null) {
            throw new IllegalArgumentException("namingInfo must not be null"); //$NON-NLS-1$
        }
        Engine engine = new Engine(environment, moduleId, outputName, index, dataType, namingInfo);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("\"{}.{}\" will use {}", new Object[] { moduleId, outputName, name });
        }
        return name;
    }


    private static final class Engine {

        private final String moduleId;

        private final String outputName;

        private final int index;

        private final DataClass dataType;

        private final List<CompiledResourcePattern> namingInfo;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        Engine(
                FlowCompilingEnvironment environment,
                String moduleId,
                String outputName,
                int index,
                DataClass dataType,
                List<CompiledResourcePattern> namingInfo) {
            assert environment != null;
            assert moduleId != null;
            assert outputName != null;
            assert dataType != null;
            assert namingInfo != null;
            this.moduleId = moduleId;
            this.outputName = outputName;
            this.index = index;
            this.dataType = dataType;
            this.namingInfo = namingInfo;
            this.factory = environment.getModelFactory();
            Name packageName = environment.getEpiloguePackageName(moduleId);
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
            SimpleName name = getClassName();
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.add(createConstructor());
            members.add(createSetMethod());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    t(StringTemplate.class),
                    Collections.<Type>emptyList(),
                    members);
        }

        private ConstructorDeclaration createConstructor() {
            List<Expression> arguments = new ArrayList<Expression>();
            for (CompiledResourcePattern naming : namingInfo) {
                arguments.add(new TypeBuilder(factory, t(FormatSpec.class))
                    .newObject(
                            new TypeBuilder(factory, t(Format.class))
                                .field(naming.getFormat().name())
                                .toExpression(),
                            naming.getArgument() == null
                                ? Models.toNullLiteral(factory)
                                : Models.toLiteral(factory, naming.getArgument()))
                    .toExpression());
            }
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(factory.newSuperConstructorInvocation(arguments));
            return factory.newConstructorDeclaration(
                    new JavadocBuilder(factory)
                        .text("Creates a new instance.")
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    getClassName(),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private MethodDeclaration createSetMethod() {
            SimpleName raw = factory.newSimpleName("rawObject");
            SimpleName object = factory.newSimpleName("object");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new ExpressionBuilder(factory, raw)
                .castTo(t(dataType.getType()))
                .toLocalVariableDeclaration(t(dataType.getType()), object));
            int position = 0;
            for (CompiledResourcePattern naming : namingInfo) {
                DataClass.Property property = naming.getTarget();
                if (property != null) {
                    statements.add(new ExpressionBuilder(factory, factory.newThis())
                        .method("setProperty", Models.toLiteral(factory, position), property.createGetter(object))
                        .toStatement());
                }
                position++;
            }
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(t(Override.class))
                        .Public()
                        .toAttributes(),
                    t(void.class),
                    factory.newSimpleName("set"),
                    Collections.singletonList(factory.newFormalParameterDeclaration(t(Object.class), raw)),
                    statements);
        }

        private SimpleName getClassName() {
            return factory.newSimpleName(String.format("%s%04d", "Naming", index));
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text("A naming output \"{1}\" class for \"{0}\".", moduleId, outputName)
                .toJavadoc();
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
