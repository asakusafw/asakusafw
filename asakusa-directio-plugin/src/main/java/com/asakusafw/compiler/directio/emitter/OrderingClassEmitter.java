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
import com.asakusafw.compiler.directio.OutputPattern.CompiledOrder;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.io.util.InvertOrder;
import com.asakusafw.runtime.stage.directio.DirectOutputOrder;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ConstructorDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
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
 * Emits a subclass of {@link DirectOutputOrder}.
 * @since 0.2.5
 */
public class OrderingClassEmitter {

    static final Logger LOG = LoggerFactory.getLogger(OrderingClassEmitter.class);

    private final FlowCompilingEnvironment environment;

    private final String moduleId;

    /**
     * Creates a new instance.
     * @param environment current environment
     * @param moduleId target module ID
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OrderingClassEmitter(FlowCompilingEnvironment environment, String moduleId) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        this.environment = environment;
        this.moduleId = moduleId;
    }

    /**
     * Emits an ordering store class.
     * @param outputName the output name
     * @param index the output index
     * @param dataType output data type
     * @param orderingInfo ordering information
     * @return the generated class name
     * @throws IOException if failed to emit class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Name emit(
            String outputName,
            int index,
            DataClass dataType,
            List<CompiledOrder> orderingInfo) throws IOException {
        if (outputName == null) {
            throw new IllegalArgumentException("outputName must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (orderingInfo == null) {
            throw new IllegalArgumentException("orderingInfo must not be null"); //$NON-NLS-1$
        }
        Engine engine = new Engine(environment, moduleId, outputName, index, dataType, orderingInfo);
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

        private final List<CompiledOrder> orderingInfo;

        private final ModelFactory factory;

        private final ImportBuilder importer;

        Engine(
                FlowCompilingEnvironment environment,
                String moduleId,
                String outputName,
                int index,
                DataClass dataType,
                List<CompiledOrder> orderingInfo) {
            assert environment != null;
            assert moduleId != null;
            assert outputName != null;
            assert dataType != null;
            assert orderingInfo != null;
            this.moduleId = moduleId;
            this.outputName = outputName;
            this.index = index;
            this.dataType = dataType;
            this.orderingInfo = orderingInfo;
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
            members.addAll(createFields());
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
                    t(DirectOutputOrder.class),
                    Collections.<Type>emptyList(),
                    members);
        }

        private List<FieldDeclaration> createFields() {
            List<FieldDeclaration> results = new ArrayList<FieldDeclaration>();
            for (CompiledOrder order : orderingInfo) {
                results.add(factory.newFieldDeclaration(
                        null,
                        new AttributeBuilder(factory)
                            .Private()
                            .Final()
                            .toAttributes(),
                        t(order.getTarget().getType()),
                        factory.newSimpleName(order.getTarget().getName()),
                        null));
            }
            return results;
        }

        private ConstructorDeclaration createConstructor() {
            List<Expression> arguments = new ArrayList<Expression>();
            for (CompiledOrder order : orderingInfo) {
                Expression arg = order.getTarget().createNewInstance(t(order.getTarget().getType()));
                if (order.isAscend() == false) {
                    arg = new TypeBuilder(factory, t(InvertOrder.class))
                        .newObject(arg)
                        .toExpression();
                }
                arguments.add(arg);
            }
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(factory.newSuperConstructorInvocation(arguments));
            int position = 0;
            for (CompiledOrder order : orderingInfo) {
                Expression obj = new ExpressionBuilder(factory, factory.newThis())
                    .method("get", Models.toLiteral(factory, position))
                    .toExpression();
                if (order.isAscend() == false) {
                    Expression invert = factory.newParenthesizedExpression(new ExpressionBuilder(factory, obj)
                        .castTo(t(InvertOrder.class))
                        .toExpression());
                    obj = new ExpressionBuilder(factory, invert)
                        .method("getEntity")
                        .toExpression();
                }
                statements.add(new ExpressionBuilder(factory, factory.newThis())
                    .field(order.getTarget().getName())
                    .assignFrom(new ExpressionBuilder(factory, obj)
                        .castTo(t(order.getTarget().getType()))
                        .toExpression())
                    .toStatement());
                position++;
            }
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
            SimpleName raw = getArgumentName("rawObject");
            SimpleName object = getArgumentName("object");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new ExpressionBuilder(factory, raw)
                .castTo(t(dataType.getType()))
                .toLocalVariableDeclaration(t(dataType.getType()), object));
            for (CompiledOrder order : orderingInfo) {
                DataClass.Property property = order.getTarget();
                statements.add(property.createGetter(object, new ExpressionBuilder(factory, factory.newThis())
                    .field(property.getName())
                    .toExpression()));
            }
            AttributeBuilder attributes = new AttributeBuilder(factory);
            if (orderingInfo.isEmpty() == false) {
                attributes = attributes.annotation(
                        importer.toType(SuppressWarnings.class),
                        Models.toLiteral(factory, "deprecation"));
            }
            attributes.annotation(t(Override.class)).Public();

            return factory.newMethodDeclaration(
                    null,
                    attributes.toAttributes(),
                    t(void.class),
                    factory.newSimpleName("set"),
                    Collections.singletonList(factory.newFormalParameterDeclaration(t(Object.class), raw)),
                    statements);
        }

        private SimpleName getArgumentName(String pref) {
            assert pref != null;
            StringBuilder nameBuffer = new StringBuilder(pref);
            while (true) {
                boolean conflict = false;
                for (CompiledOrder order : orderingInfo) {
                    if (order.getTarget().getName().contentEquals(nameBuffer)) {
                        conflict = true;
                        continue;
                    }
                }
                if (conflict == false) {
                    return factory.newSimpleName(nameBuffer.toString());
                }
                nameBuffer.append('_');
            }
        }

        private SimpleName getClassName() {
            return factory.newSimpleName(String.format("%s%04d", "Ordering", index));
        }

        private Javadoc createJavadoc() {
            return new JavadocBuilder(factory)
                .text("An ordering output \"{1}\" class for \"{0}\".", moduleId, outputName)
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
