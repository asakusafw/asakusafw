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
package com.asakusafw.compiler.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.operator.OperatorProcessor.Context;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.OperatorFactory;
import com.asakusafw.vocabulary.operator.OperatorInfo;

/**
 * Generates operator factory classes for operator classes.
 */
public class OperatorFactoryClassGenerator extends OperatorClassGenerator {

    /**
     * The field name for holding {@link FlowElementResolver}.
     */
    static final String RESOLVER_FIELD_NAME = "$"; //$NON-NLS-1$

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param factory the Java DOM factory
     * @param importer the import declaration builder
     * @param operatorClass the target operator class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorFactoryClassGenerator(
            OperatorCompilingEnvironment environment,
            ModelFactory factory,
            ImportBuilder importer,
            OperatorClass operatorClass) {
        super(environment, factory, importer, operatorClass);
    }

    @Override
    public TypeDeclaration generate() {
        // reserves namespaces for the this operator class on ahead
        for (OperatorMethod method : operatorClass.getMethods()) {
            importer.resolvePackageMember(Models.append(
                    factory,
                    getClassName(),
                    getObjectClassName(method.getElement())));
        }
        return super.generate();
    }

    @Override
    protected SimpleName getClassName() {
        return util.getFactoryName(operatorClass.getElement());
    }

    @Override
    protected List<? extends Attribute> getAttributes() {
        return new AttributeBuilder(factory)
            .annotation(util.t(Generated.class), util.v("{0}:{1}", //$NON-NLS-1$
                    getClass().getSimpleName(),
                    OperatorCompiler.VERSION))
            .annotation(util.t(OperatorFactory.class),
                    factory.newClassLiteral(util.t(operatorClass.getElement())))
            .Public()
            .toAttributes();
    }

    @Override
    protected Javadoc createJavadoc() {
        return new JavadocBuilder(factory)
            .text(Messages.getString("OperatorFactoryClassGenerator.javadocClass"), //$NON-NLS-1$
                    operatorClass.getElement().getSimpleName())
            .seeType(util.t(operatorClass.getElement()))
            .toJavadoc();
    }

    @Override
    protected List<TypeBodyDeclaration> createMembers() {
        NameGenerator names = new NameGenerator(factory);
        List<TypeBodyDeclaration> results = new ArrayList<>();
        for (OperatorMethod method : operatorClass.getMethods()) {
            OperatorProcessor.Context context = new OperatorProcessor.Context(
                    environment,
                    method.getAnnotation(),
                    method.getElement(),
                    importer,
                    names);
            OperatorProcessor processor = method.getProcessor();
            OperatorMethodDescriptor descriptor = processor.describe(context);

            if (descriptor == null) {
                continue;
            }

            TypeDeclaration objectClass = createObjectClass(context, descriptor);
            if (objectClass == null) {
                continue;
            }

            Type objectType = importer.resolvePackageMember(
                    Models.append(factory, getClassName(), objectClass.getName()));
            if (context.element.getTypeParameters().isEmpty() == false) {
                objectType = new TypeBuilder(factory, objectType)
                        .parameterize(util.toTypeVariables(context.element))
                        .toType();
            }
            MethodDeclaration factoryMethod = createFactoryMethod(context, descriptor, objectType);
            if (factoryMethod == null) {
                continue;
            }

            results.add(objectClass);
            results.add(factoryMethod);
        }
        return results;
    }

    private TypeDeclaration createObjectClass(
            Context context,
            OperatorMethodDescriptor descriptor) {
        assert context != null;
        assert descriptor != null;
        SimpleName name = getObjectClassName(context.element);
        NamedType objectType = (NamedType) importer.resolvePackageMember(
                Models.append(factory, getClassName(), name));
        List<TypeParameterDeclaration> typeParameters = util.toTypeParameters(context.element);
        List<TypeBodyDeclaration> members = createObjectMembers(
                context,
                descriptor,
                objectType);
        return factory.newClassDeclaration(
                new JavadocBuilder(factory)
                    .inline(descriptor.getDocumentation())
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .Static()
                    .Final()
                    .toAttributes(),
                name,
                typeParameters,
                null,
                Collections.singletonList(util.t(Operator.class)),
                members);
    }

    private SimpleName getObjectClassName(ExecutableElement element) {
        assert element != null;
        return factory.newSimpleName(JavaName.of(element.getSimpleName().toString()).toTypeName());
    }

    private List<TypeBodyDeclaration> createObjectMembers(
            Context context,
            OperatorMethodDescriptor descriptor,
            NamedType objectType) {
        assert context != null;
        assert descriptor != null;
        assert objectType != null;
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createResolverField(context));
        for (OperatorPortDeclaration var : descriptor.getOutputPorts()) {
            results.add(createObjectOutputField(context, var));
        }
        results.add(createObjectConstructor(context, descriptor, objectType));
        results.add(createRenamer(context, objectType));
        return results;
    }

    private MethodDeclaration createRenamer(Context context, NamedType rawObjectType) {
        assert context != null;
        assert rawObjectType != null;
        Type objectType;
        if (context.element.getTypeParameters().isEmpty()) {
            objectType = rawObjectType;
        } else {
            objectType = new TypeBuilder(factory, rawObjectType)
                .parameterize(util.toTypeVariables(context.element))
                .toType();
        }
        SimpleName newName = context.names.create("newName"); //$NON-NLS-1$
        return factory.newMethodDeclaration(
                new JavadocBuilder(factory)
                    .text(Messages.getString(
                            "OperatorFactoryClassGenerator.javadocSetName")) //$NON-NLS-1$
                    .param(newName)
                        .text(Messages.getString(
                                "OperatorFactoryClassGenerator.javadocSetNameParameter")) //$NON-NLS-1$
                    .returns()
                        .text(Messages.getString(
                                "OperatorFactoryClassGenerator.javadocSetNameReturn")) //$NON-NLS-1$
                    .exception(util.t(IllegalArgumentException.class))
                        .text(Messages.getString(
                                "OperatorFactoryClassGenerator.javadocSetNameNullParameter")) //$NON-NLS-1$
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .toAttributes(),
                objectType,
                factory.newSimpleName("as"), //$NON-NLS-1$
                Collections.singletonList(factory.newFormalParameterDeclaration(
                        util.t(String.class),
                        newName)),
                Arrays.asList(new Statement[] {
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(RESOLVER_FIELD_NAME)
                            .method("setName", newName) //$NON-NLS-1$
                            .toStatement(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .toReturnStatement(),
                }));
    }

    private FieldDeclaration createResolverField(Context context) {
        assert context != null;
        return factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .Final()
                    .toAttributes(),
                util.t(FlowElementResolver.class),
                factory.newSimpleName(RESOLVER_FIELD_NAME),
                null);
    }

    private FieldDeclaration createObjectOutputField(
            Context context,
            OperatorPortDeclaration var) {
        assert context != null;
        assert var != null;
        return factory.newFieldDeclaration(
                new JavadocBuilder(factory)
                    .inline(var.getDocumentation())
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .Final()
                    .toAttributes(),
                util.toSourceType(var.getType().getRepresentation()),
                factory.newSimpleName(context.names.reserve(var.getName())),
                null);
    }

    private ConstructorDeclaration createObjectConstructor(
            Context context,
            OperatorMethodDescriptor descriptor,
            NamedType objectType) {
        assert context != null;
        assert descriptor != null;
        assert objectType != null;

        List<FormalParameterDeclaration> parameters = createParametersForConstructor(context, descriptor);
        List<Statement> statements = createBodyForConstructor(context, descriptor, parameters);
        return factory.newConstructorDeclaration(
                null,
                new AttributeBuilder(factory).toAttributes(),
                objectType.getName().getLastSegment(),
                parameters,
                statements);
    }

    private List<Statement> createBodyForConstructor(
            Context context,
            OperatorMethodDescriptor descriptor,
            List<FormalParameterDeclaration> parameters) {
        assert context != null;
        assert descriptor != null;
        assert parameters != null;
        List<Statement> statements = new ArrayList<>();
        SimpleName builderName = context.names.create("builder"); //$NON-NLS-1$
        statements.add(new TypeBuilder(factory, util.t(OperatorDescription.Builder.class))
            .newObject(factory.newClassLiteral(util.t(descriptor.getAnnotationType())))
            .toLocalVariableDeclaration(
                    util.t(OperatorDescription.Builder.class),
                    builderName));
        statements.add(new ExpressionBuilder(factory, builderName)
            .method("declare", //$NON-NLS-1$
                    factory.newClassLiteral(util.t(operatorClass.getElement())),
                    factory.newClassLiteral(factory.newNamedType(
                            util.getImplementorName(operatorClass.getElement()))),
                    util.v(descriptor.getName()))
            .toStatement());
        for (VariableElement parameter : context.element.getParameters()) {
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("declareParameter", //$NON-NLS-1$
                        new TypeBuilder(factory, util.t(environment.getErasure(parameter.asType())))
                            .dotClass()
                            .toExpression())
                .toStatement());
        }

        for (OperatorPortDeclaration var : descriptor.getInputPorts()) {
            ShuffleKey key = var.getShuffleKey();
            List<Expression> arguments = new ArrayList<>();
            arguments.add(util.v(var.getName()));
            arguments.add(factory.newSimpleName(var.getName()));
            if (key != null) {
                arguments.add(toSource(key));
            }
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("addInput", arguments) //$NON-NLS-1$
                .toStatement());
        }
        for (OperatorPortDeclaration var : descriptor.getOutputPorts()) {
            Expression type = toExpression(var);
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("addOutput", util.v(var.getName()), type) //$NON-NLS-1$
                .toStatement());
        }
        for (OperatorPortDeclaration var : descriptor.getParameters()) {
            Expression type = toExpression(var);
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("addParameter", //$NON-NLS-1$
                        util.v(var.getName()),
                        type,
                        factory.newSimpleName(var.getName()))
                .toStatement());
        }
        for (Expression attr : descriptor.getAttributes()) {
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("addAttribute", attr) //$NON-NLS-1$
                .toStatement());
        }
        Expression resolver = new ExpressionBuilder(factory, factory.newThis())
            .field(RESOLVER_FIELD_NAME)
            .toExpression();
        statements.add(new ExpressionBuilder(factory, resolver)
            .assignFrom(new ExpressionBuilder(factory, builderName)
                .method("toResolver") //$NON-NLS-1$
                .toExpression())
            .toStatement());
        for (OperatorPortDeclaration var : descriptor.getInputPorts()) {
            statements.add(new ExpressionBuilder(factory, resolver)
                .method("resolveInput", //$NON-NLS-1$
                        util.v(var.getName()),
                        factory.newSimpleName(var.getName()))
                .toStatement());
        }
        for (OperatorPortDeclaration var : descriptor.getOutputPorts()) {
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(var.getName())
                .assignFrom(new ExpressionBuilder(factory, resolver)
                    .method("resolveOutput", util.v(var.getName())) //$NON-NLS-1$
                    .toExpression())
                .toStatement());
        }
        return statements;
    }

    private Expression toExpression(OperatorPortDeclaration var) throws AssertionError {
        Expression type;
        switch (var.getType().getKind()) {
        case DIRECT:
            type = factory.newClassLiteral(util.t(environment.getErasure(var.getType().getDirect())));
            break;
        case REFERENCE:
            type = factory.newSimpleName(var.getType().getReference());
            break;
        default:
            throw new AssertionError(var.getType().getKind());
        }
        return type;
    }

    private Expression toSource(ShuffleKey key) {
        assert key != null;
        List<Expression> group = new ArrayList<>();
        for (String property : key.getGroupProperties()) {
            group.add(Models.toLiteral(factory, property));
        }
        List<Expression> order = new ArrayList<>();
        for (ShuffleKey.Order o : key.getOrderings()) {
            order.add(new TypeBuilder(factory, util.t(ShuffleKey.Order.class))
                .newObject(
                        Models.toLiteral(factory, o.getProperty()),
                        new TypeBuilder(factory, util.t(ShuffleKey.Direction.class))
                            .field(o.getDirection().name())
                            .toExpression())
                .toExpression());
        }

        return new TypeBuilder(factory, util.t(ShuffleKey.class))
            .newObject(
                    toList(util.t(String.class), group),
                    toList(util.t(ShuffleKey.Order.class), order))
            .toExpression();
    }

    private Expression toList(Type type, List<Expression> expressions) {
        assert type != null;
        assert expressions != null;
        return new TypeBuilder(factory, util.t(Arrays.class))
            .method("asList", factory.newArrayCreationExpression(//$NON-NLS-1$
                    factory.newArrayType(type),
                    Collections.emptyList(),
                    factory.newArrayInitializer(expressions)))
            .toExpression();
    }

    private List<FormalParameterDeclaration> createParametersForConstructor(
            Context context,
            OperatorMethodDescriptor descriptor) {
        assert context != null;
        assert descriptor != null;
        List<FormalParameterDeclaration> parameters = new ArrayList<>();
        for (OperatorPortDeclaration var : descriptor.getInputPorts()) {
            SimpleName name = factory.newSimpleName(context.names.reserve(var.getName()));
            parameters.add(factory.newFormalParameterDeclaration(
                    util.toSourceType(var.getType().getRepresentation()),
                    name));
        }
        for (OperatorPortDeclaration var : descriptor.getParameters()) {
            SimpleName name = factory.newSimpleName(context.names.reserve(var.getName()));
            parameters.add(factory.newFormalParameterDeclaration(
                    util.t(var.getType().getRepresentation()),
                    name));
        }
        return parameters;
    }

    private MethodDeclaration createFactoryMethod(
            Context context,
            OperatorMethodDescriptor descriptor,
            Type objectType) {
        assert context != null;
        assert descriptor != null;
        assert objectType != null;
        JavadocBuilder javadoc = new JavadocBuilder(factory);
        javadoc.inline(descriptor.getDocumentation());
        List<FormalParameterDeclaration> parameters = new ArrayList<>();
        List<Expression> arguments = new ArrayList<>();
        List<Expression> inputMetaData = new ArrayList<>();
        for (OperatorPortDeclaration var : descriptor.getInputPorts()) {
            SimpleName name = factory.newSimpleName(var.getName());
            javadoc.param(name).inline(var.getDocumentation());
            parameters.add(util.toFactoryMethodInput(var, name));
            inputMetaData.add(util.toMetaData(var, arguments.size()));
            arguments.add(name);
        }
        List<Expression> outputMetaData = new ArrayList<>();
        for (OperatorPortDeclaration var : descriptor.getOutputPorts()) {
            outputMetaData.add(util.toMetaData(var, -1));
        }
        List<Expression> parameterMetaData = new ArrayList<>();
        for (OperatorPortDeclaration var : descriptor.getParameters()) {
            SimpleName name = factory.newSimpleName(var.getName());
            javadoc.param(name).inline(var.getDocumentation());
            parameters.add(factory.newFormalParameterDeclaration(
                    util.t(var.getType().getRepresentation()),
                    name));
            parameterMetaData.add(util.toMetaData(var, arguments.size()));
            arguments.add(name);
        }
        javadoc.returns().text(Messages.getString("OperatorFactoryClassGenerator.javadocFactoryReturn")); //$NON-NLS-1$

        List<Type> rawParameterTypes = new ArrayList<>();
        for (VariableElement var : context.element.getParameters()) {
            rawParameterTypes.add(util.t(environment.getErasure(var.asType())));
        }
        javadoc.seeMethod(
                util.t(operatorClass.getElement()),
                descriptor.getName(),
                rawParameterTypes);

        return factory.newMethodDeclaration(
                javadoc.toJavadoc(),
                new AttributeBuilder(factory)
                    .annotation(util.t(OperatorInfo.class),
                            "kind", factory.newClassLiteral(util.t(descriptor.getAnnotationType())), //$NON-NLS-1$
                            "input", factory.newArrayInitializer(inputMetaData), //$NON-NLS-1$
                            "output", factory.newArrayInitializer(outputMetaData), //$NON-NLS-1$
                            "parameter", factory.newArrayInitializer(parameterMetaData)) //$NON-NLS-1$
                    .Public()
                    .toAttributes(),
                util.toTypeParameters(context.element),
                objectType,
                factory.newSimpleName(JavaName.of(descriptor.getName()).toMemberName()),
                parameters,
                0,
                Collections.emptyList(),
                factory.newBlock(new TypeBuilder(factory, objectType)
                    .newObject(arguments)
                    .toReturnStatement()));
    }
}
