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
package com.asakusafw.compiler.directio.emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.directio.OutputPattern.CompiledResourcePattern;
import com.asakusafw.compiler.directio.OutputPattern.RandomNumber;
import com.asakusafw.compiler.directio.OutputPattern.SourceKind;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.stage.directio.StringTemplate;
import com.asakusafw.runtime.stage.directio.StringTemplate.Format;
import com.asakusafw.runtime.stage.directio.StringTemplate.FormatSpec;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
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

/**
 * Emits {@link StringTemplate} subclasses.
 * @since 0.2.5
 */
@SuppressWarnings("deprecation")
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
        LOG.debug("Start preparing output file name template: " //$NON-NLS-1$
                + "batch={}, flow={}, output={}", new Object[] { //$NON-NLS-1$
                environment.getBatchId(),
                environment.getFlowId(),
                outputName,
        });

        Engine engine = new Engine(environment, moduleId, outputName, index, dataType, namingInfo);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        QualifiedName name = environment
            .getModelFactory()
            .newQualifiedName(packageName, simpleName);
        LOG.debug("Finish preparing output file name template: " //$NON-NLS-1$
                + "batch={}, flow={}, output={}, class={}", new Object[] { //$NON-NLS-1$
                environment.getBatchId(),
                environment.getFlowId(),
                outputName,
                name.toNameString(),
        });
        return name;
    }


    private static final class Engine {

        private static final String FIELD_RANDOM_HOLDER = "randomValue"; //$NON-NLS-1$

        private static final String FIELD_RANDOMIZER = "randomizer"; //$NON-NLS-1$

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
                    Collections.singletonList(type));
        }

        private TypeDeclaration createType() {
            SimpleName name = getClassName();
            importer.resolvePackageMember(name);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            if (requireRandomNumber()) {
                members.add(createRandomHolder());
                members.add(createRandomizer());
            }
            members.add(createConstructor());
            members.add(createSetMethod());
            return factory.newClassDeclaration(
                    createJavadoc(),
                    new AttributeBuilder(factory)
                        .annotation(
                                importer.toType(SuppressWarnings.class),
                                Models.toLiteral(factory, "deprecation")) //$NON-NLS-1$
                        .Public()
                        .Final()
                        .toAttributes(),
                    name,
                    Collections.emptyList(),
                    t(StringTemplate.class),
                    Collections.emptyList(),
                    members);
        }

        private boolean requireRandomNumber() {
            for (CompiledResourcePattern naming : namingInfo) {
                if (naming.getKind() == SourceKind.RANDOM) {
                    return true;
                }
            }
            return false;
        }

        private FieldDeclaration createRandomHolder() {
            new IntOption().modify(index);
            return factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .Final()
                        .toAttributes(),
                    importer.toType(IntOption.class),
                    factory.newSimpleName(FIELD_RANDOM_HOLDER),
                    new TypeBuilder(factory, importer.toType(IntOption.class))
                        .newObject()
                        .toExpression());
        }

        private FieldDeclaration createRandomizer() {
            return factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .Final()
                        .toAttributes(),
                    importer.toType(Random.class),
                    factory.newSimpleName(FIELD_RANDOMIZER),
                    new TypeBuilder(factory, importer.toType(Random.class))
                        .newObject(Models.toLiteral(factory, 12345))
                        .toExpression());
        }

        private ConstructorDeclaration createConstructor() {
            List<Expression> arguments = new ArrayList<>();
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
            List<Statement> statements = new ArrayList<>();
            statements.add(factory.newSuperConstructorInvocation(arguments));
            return factory.newConstructorDeclaration(
                    new JavadocBuilder(factory)
                        .text("Creates a new instance.") //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    getClassName(),
                    Collections.emptyList(),
                    statements);
        }

        private MethodDeclaration createSetMethod() {
            SimpleName raw = factory.newSimpleName("rawObject"); //$NON-NLS-1$
            SimpleName object = factory.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new ExpressionBuilder(factory, raw)
                .castTo(t(dataType.getType()))
                .toLocalVariableDeclaration(t(dataType.getType()), object));
            int position = 0;
            for (CompiledResourcePattern naming : namingInfo) {
                switch (naming.getKind()) {
                case NOTHING:
                    break;
                case PROPERTY: {
                    DataClass.Property property = naming.getTarget();
                    statements.add(new ExpressionBuilder(factory, factory.newThis())
                        .method("setProperty", //$NON-NLS-1$
                                Models.toLiteral(factory, position), property.createGetter(object))
                        .toStatement());
                    break;
                }
                case RANDOM: {
                    RandomNumber rand = naming.getRandomNumber();
                    statements.add(new ExpressionBuilder(factory, factory.newThis())
                        .field(FIELD_RANDOM_HOLDER)
                        .method("modify", new ExpressionBuilder(factory, factory.newThis()) //$NON-NLS-1$
                            .field(FIELD_RANDOMIZER)
                            .method(
                                    "nextInt", //$NON-NLS-1$
                                    Models.toLiteral(factory, rand.getUpperBound() - rand.getLowerBound() + 1))
                            .apply(InfixOperator.PLUS, Models.toLiteral(factory, rand.getLowerBound()))
                            .toExpression())
                        .toStatement());
                    statements.add(new ExpressionBuilder(factory, factory.newThis())
                        .method("setProperty", //$NON-NLS-1$
                                Models.toLiteral(factory, position),
                                new ExpressionBuilder(factory, factory.newThis())
                                    .field(FIELD_RANDOM_HOLDER)
                                    .toExpression())
                        .toStatement());
                    break;
                }
                default:
                    throw new AssertionError();
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
                    factory.newSimpleName("set"), //$NON-NLS-1$
                    Collections.singletonList(factory.newFormalParameterDeclaration(t(Object.class), raw)),
                    statements);
        }

        private SimpleName getClassName() {
            return factory.newSimpleName(String.format("%s%04d", "Naming", index)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .inline("A naming output {1} class for {0}.",
                        d -> d.code("\"{0}\"", moduleId), //$NON-NLS-1$
                        d -> d.code("\"{0}\"", outputName)) //$NON-NLS-1$
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
