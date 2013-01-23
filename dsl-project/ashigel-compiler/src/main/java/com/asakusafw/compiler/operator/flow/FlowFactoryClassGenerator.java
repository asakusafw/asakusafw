/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.operator.flow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.compiler.operator.OperatorPortDeclaration;
import com.asakusafw.compiler.operator.util.GeneratorUtil;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
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
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.Inline;
import com.asakusafw.vocabulary.operator.OperatorFactory;

/**
 * フロー部品クラスから演算子ファクトリークラスのJava DOMを構築する。
 */
public class FlowFactoryClassGenerator {

    /**
     * {@link FlowElementResolver}を保持するフィールド名。
     */
    static final String RESOLVER_FIELD_NAME = "$";

    private final ModelFactory factory;

    private final ImportBuilder importer;

    private final FlowPartClass flowClass;

    private final GeneratorUtil util;

    private final OperatorCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param factory DOMを構築するためのファクトリ
     * @param importer インポート宣言を構築するビルダー
     * @param flowClass フロー部品クラスの情報
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public FlowFactoryClassGenerator(
            OperatorCompilingEnvironment environment,
            ModelFactory factory,
            ImportBuilder importer,
            FlowPartClass flowClass) {
        this.environment = environment;
        this.factory = factory;
        this.importer = importer;
        this.flowClass = flowClass;
        this.util = new GeneratorUtil(environment, factory, importer);
    }

    /**
     * このジェネレータの情報を利用して型宣言の情報を生成する。
     * @return 生成したモデル
     */
    public TypeDeclaration generate() {
        // 先に名前空間を退避する
        SimpleName name = getClassName();
        importer.resolvePackageMember(Models.append(
                factory,
                name,
                getObjectClassName()));
        return factory.newClassDeclaration(
                new JavadocBuilder(factory)
                    .code(flowClass.getElement().getSimpleName().toString())
                    .text("に対する演算子ファクトリークラス。")
                    .seeType(new Jsr269(factory).convert(environment.getErasure(flowClass.getElement().asType())))
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .annotation(util.t(Generated.class), util.v("{0}:{1}",
                            FlowOperatorCompiler.class.getSimpleName(),
                            FlowOperatorCompiler.VERSION))
                    .annotation(util.t(OperatorFactory.class),
                            factory.newClassLiteral(util.t(flowClass.getElement())))
                    .Public()
                    .toAttributes(),
                name,
                Collections.<TypeParameterDeclaration>emptyList(),
                null,
                Collections.<Type>emptyList(),
                createMembers());
    }

    private List<TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = Lists.create();

        TypeDeclaration objectClass = createObjectClass();
        results.add(objectClass);

        NamedType objectType = (NamedType) importer.resolvePackageMember(
                Models.append(factory,
                        getClassName(),
                        objectClass.getName()));
        MethodDeclaration factoryMethod = createFactoryMethod(objectType);
        if (factoryMethod != null) {
            results.add(factoryMethod);
        }

        return results;
    }

    private TypeDeclaration createObjectClass() {
        SimpleName name = getObjectClassName();
        NamedType objectType = (NamedType) importer.resolvePackageMember(
                Models.append(factory, getClassName(), name));
        List<TypeBodyDeclaration> members = createObjectMembers(objectType);
        return factory.newClassDeclaration(
                new JavadocBuilder(factory)
                    .inline(flowClass.getDocumentation())
                    .seeType(new Jsr269(factory).convert(environment.getErasure(flowClass.getElement().asType())))
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .Static()
                    .Final()
                    .toAttributes(),
                name,
                util.toTypeParameters(flowClass.getElement()),
                null,
                Collections.singletonList(util.t(Operator.class)),
                members);
    }

    private SimpleName getObjectClassName() {
        return factory.newSimpleName(
                JavaName.of(flowClass.getElement().getSimpleName().toString()).toTypeName());
    }

    private List<TypeBodyDeclaration> createObjectMembers(NamedType objectType) {
        assert objectType != null;
        NameGenerator names = new NameGenerator(factory);
        List<TypeBodyDeclaration> results = Lists.create();
        results.add(createResolverField());
        for (OperatorPortDeclaration var : flowClass.getOutputPorts()) {
            results.add(createObjectOutputField(var, names));
        }
        results.add(createObjectConstructor(objectType, names));
        results.add(createRenamer(objectType, names));
        results.add(createInliner(objectType, names));
        return results;
    }

    private FieldDeclaration createResolverField() {
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

    private MethodDeclaration createRenamer(NamedType objectType, NameGenerator names) {
        assert objectType != null;
        assert names != null;
        SimpleName newName = names.create("newName");
        return factory.newMethodDeclaration(
                new JavadocBuilder(factory)
                    .text("この演算子の名前を設定する。")
                    .param(newName)
                        .text("設定する名前")
                    .returns()
                        .text("この演算子オブジェクト (this)")
                    .exception(util.t(IllegalArgumentException.class))
                        .text("引数に")
                        .code("null")
                        .text("が指定された場合")
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .toAttributes(),
                getType(objectType),
                factory.newSimpleName("as"),
                Collections.singletonList(factory.newFormalParameterDeclaration(
                        util.t(String.class),
                        newName)),
                Arrays.asList(new Statement[] {
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(RESOLVER_FIELD_NAME)
                            .method("setName", newName)
                            .toStatement(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .toReturnStatement(),
                }));
    }

    private MethodDeclaration createInliner(NamedType objectType, NameGenerator names) {
        assert objectType != null;
        assert names != null;
        SimpleName optimize = names.create("optimize");
        return factory.newMethodDeclaration(
                new JavadocBuilder(factory)
                    .text("このフロー部品のインライン化状態を設定する。")
                    .param(optimize)
                        .text("trueならば最適化を行い、falseならばステージ構成を保持する")
                    .returns()
                        .text("この演算子オブジェクト (this)")
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .toAttributes(),
                getType(objectType),
                factory.newSimpleName("inlined"),
                Collections.singletonList(factory.newFormalParameterDeclaration(
                        util.t(boolean.class),
                        optimize)),
                Arrays.asList(new Statement[] {
                        new ExpressionBuilder(factory, factory.newThis())
                            .field(RESOLVER_FIELD_NAME)
                            .method("getElement")
                            .method("override", factory.newConditionalExpression(
                                    optimize,
                                    new TypeBuilder(factory, util.t(Inline.class))
                                        .field(Inline.FORCE_AGGREGATE.name())
                                        .toExpression(),
                                    new TypeBuilder(factory, util.t(Inline.class))
                                        .field(Inline.KEEP_SEGREGATED.name())
                                        .toExpression()))
                            .toStatement(),
                        new ExpressionBuilder(factory, factory.newThis())
                            .toReturnStatement(),
                }));
    }

    private TypeBodyDeclaration createObjectOutputField(
            OperatorPortDeclaration var,
            NameGenerator names) {
        assert var != null;
        assert names != null;
        return factory.newFieldDeclaration(
                new JavadocBuilder(factory)
                    .inline(var.getDocumentation())
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .Final()
                    .toAttributes(),
                util.toSourceType(var.getType().getRepresentation()),
                factory.newSimpleName(names.reserve(var.getName())),
                null);
    }

    private TypeBodyDeclaration createObjectConstructor(
            NamedType objectType,
            NameGenerator names) {
        assert objectType != null;
        List<FormalParameterDeclaration> parameters =
                createParametersForConstructor(names);

        List<Statement> statements = createBodyForConstructor(parameters, names);

        return factory.newConstructorDeclaration(
                null,
                new AttributeBuilder(factory)
                    // Default Package Access
                    .toAttributes(),
                objectType.getName().getLastSegment(),
                parameters,
                statements);
    }

    private List<FormalParameterDeclaration> createParametersForConstructor(
            NameGenerator names) {
        List<FormalParameterDeclaration> parameters = Lists.create();
        for (OperatorPortDeclaration var : flowClass.getInputPorts()) {
            SimpleName name = factory.newSimpleName(names.reserve(var.getName()));
            parameters.add(factory.newFormalParameterDeclaration(
                    util.toSourceType(var.getType().getRepresentation()),
                    name));
        }
        for (OperatorPortDeclaration var : flowClass.getParameters()) {
            SimpleName name = factory.newSimpleName(names.reserve(var.getName()));
            parameters.add(factory.newFormalParameterDeclaration(
                    util.t(var.getType().getRepresentation()),
                    name));
        }
        return parameters;
    }

    private List<Statement> createBodyForConstructor(
            List<FormalParameterDeclaration> parameters,
            NameGenerator names) {
        assert parameters != null;
        List<Statement> statements = Lists.create();
        SimpleName builderName = names.create("builder");
        statements.add(new TypeBuilder(factory, util.t(FlowPartDescription.Builder.class))
            .newObject(factory.newClassLiteral(util.t(flowClass.getElement())))
            .toLocalVariableDeclaration(util.t(FlowPartDescription.Builder.class),
                    builderName));

        Expression[] arguments = new Expression[
                flowClass.getInputPorts().size()
                + flowClass.getOutputPorts().size()
                + flowClass.getParameters().size()
                ];
        for (OperatorPortDeclaration var : flowClass.getInputPorts()) {
            SimpleName name = names.create(var.getName());
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("addInput",
                        util.v(var.getName()),
                        factory.newSimpleName(var.getType().getReference()))
                .toLocalVariableDeclaration(
                        util.toInType(var.getType().getRepresentation()),
                        name));
            arguments[var.getParameterPosition()] = name;
        }
        for (OperatorPortDeclaration var : flowClass.getOutputPorts()) {
            SimpleName name = names.create(var.getName());
            Expression type;
            switch (var.getType().getKind()) {
            case DIRECT:
                type = factory.newClassLiteral(util.t(var.getType().getDirect()));
                break;
            case REFERENCE:
                type = factory.newSimpleName(var.getType().getReference());
                break;
            default:
                throw new AssertionError(var.getType().getKind());
            }
            assert type != null;
            statements.add(new ExpressionBuilder(factory, builderName)
                .method("addOutput", util.v(var.getName()), type)
                .toLocalVariableDeclaration(
                        util.toOutType(var.getType().getRepresentation()),
                        name));
            arguments[var.getParameterPosition()] = name;
        }
        for (OperatorPortDeclaration var : flowClass.getParameters()) {
            SimpleName name = factory.newSimpleName(var.getName());
            arguments[var.getParameterPosition()] = name;
        }
        SimpleName descName = names.create("desc");
        statements.add(new TypeBuilder(factory, getType(util.t(flowClass.getElement())))
            .newObject(arguments)
            .toLocalVariableDeclaration(util.t(FlowDescription.class), descName));

        Expression resolver = new ExpressionBuilder(factory, factory.newThis())
            .field(RESOLVER_FIELD_NAME)
            .toExpression();
        statements.add(new ExpressionBuilder(factory, resolver)
            .assignFrom(new ExpressionBuilder(factory, builderName)
                .method("toResolver", descName)
                .toExpression())
            .toStatement());
        for (OperatorPortDeclaration var : flowClass.getInputPorts()) {
            statements.add(new ExpressionBuilder(factory, resolver)
                .method("resolveInput",
                        util.v(var.getName()),
                        factory.newSimpleName(var.getName()))
                .toStatement());
        }
        for (OperatorPortDeclaration var : flowClass.getOutputPorts()) {
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(var.getName())
                .assignFrom(new ExpressionBuilder(factory, resolver)
                    .method("resolveOutput", util.v(var.getName()))
                    .toExpression())
                .toStatement());
        }
        return statements;
    }

    private MethodDeclaration createFactoryMethod(NamedType objectType) {
        assert objectType != null;
        JavadocBuilder javadoc = new JavadocBuilder(factory);
        javadoc.inline(flowClass.getDocumentation());
        List<FormalParameterDeclaration> parameters = Lists.create();
        List<Expression> arguments = Lists.create();
        for (OperatorPortDeclaration var : flowClass.getInputPorts()) {
            SimpleName name = factory.newSimpleName(var.getName());
            javadoc.param(name).inline(var.getDocumentation());
            parameters.add(factory.newFormalParameterDeclaration(
                    util.toSourceType(var.getType().getRepresentation()),
                    name));
            arguments.add(name);
        }
        for (OperatorPortDeclaration var : flowClass.getParameters()) {
            SimpleName name = factory.newSimpleName(var.getName());
            javadoc.param(name).inline(var.getDocumentation());
            parameters.add(factory.newFormalParameterDeclaration(
                    util.t(var.getType().getRepresentation()),
                    name));
            arguments.add(name);
        }
        Type type = getType(objectType);
        javadoc.returns().text("生成した演算子オブジェクト");
        javadoc.seeType(util.t(flowClass.getElement()));
        return factory.newMethodDeclaration(
                javadoc.toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .toAttributes(),
                util.toTypeParameters(flowClass.getElement()),
                type,
                factory.newSimpleName("create"),
                parameters,
                0,
                Collections.<Type>emptyList(),
                factory.newBlock(
                        new TypeBuilder(factory, type)
                            .newObject(arguments)
                            .toReturnStatement()));
    }

    private Type getType(Type objectType) {
        assert objectType != null;
        assert objectType.getModelKind() != ModelKind.PARAMETERIZED_TYPE;
        Type type;
        if (flowClass.getElement().getTypeParameters().isEmpty()) {
            type = objectType;
        } else {
            type = new TypeBuilder(factory, objectType)
                .parameterize(util.toTypeVariables(flowClass.getElement()))
                .toType();
        }
        return type;
    }

    private SimpleName getClassName() {
        return util.getFactoryName(flowClass.getElement());
    }
}
