/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.join;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.flow.join.JoinResource;
import com.asakusafw.runtime.flow.join.LookUpKey;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * {@link JoinResourceDescription}から{@link JoinResource}を生成する。
 */
public final class JoinResourceEmitter {

    static final Logger LOG = LoggerFactory.getLogger(JoinResourceEmitter.class);

    private final FlowCompilingEnvironment environment;

    private final ModelFactory factory;

    private final ImportBuilder importer;

    private final JoinResourceDescription resource;

    private JoinResourceEmitter(FlowCompilingEnvironment environment, JoinResourceDescription resource) {
        assert environment != null;
        assert resource != null;
        this.environment = environment;
        this.factory = environment.getModelFactory();
        Name packageName = environment.getResourcePackage("join");
        this.importer = new ImportBuilder(
                factory,
                factory.newPackageDeclaration(packageName),
                ImportBuilder.Strategy.TOP_LEVEL);
        this.resource = resource;
    }

    /**
     * リソースを表すプログラムを生成する。
     * @param environment 環境オブジェクト
     * @param resource 対象のリソースの情報
     * @return 生成したプログラムのクラス名
     * @throws IOException プログラムの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static Name emit(
            FlowCompilingEnvironment environment,
            JoinResourceDescription resource) throws IOException {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(resource, "resource"); //$NON-NLS-1$
        JoinResourceEmitter emitter = new JoinResourceEmitter(environment, resource);
        return emitter.emit();
    }

    private Name emit() throws IOException {
        LOG.debug("{}のリソースに対するプログラムを生成します", resource);
        CompilationUnit source = generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("{}のリソースには{}が利用されます",
                resource,
                name);
        return name;
    }

    private CompilationUnit generate() {
        ClassDeclaration type = createType();
        return factory.newCompilationUnit(
                importer.getPackageDeclaration(),
                importer.toImportDeclarations(),
                Collections.singletonList(type),
                Collections.<Comment>emptyList());
    }

    private ClassDeclaration createType() {
        SimpleName name = environment.createUniqueName("Join");
        importer.resolvePackageMember(name);
        List<TypeBodyDeclaration> members = createMembers();
        return factory.newClassDeclaration(
                new JavadocBuilder(factory)
                    .linkType(importer.toType(resource.getMasterDataClass().getType()))
                    .text("と")
                    .linkType(importer.toType(resource.getTransactionDataClass().getType()))
                    .text("を結合するためのリソース。")
                    .toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .toAttributes(),
                name,
                importer.resolve(new TypeBuilder(factory, Models.toType(factory, JoinResource.class))
                    .parameterize(
                            resource.getMasterDataClass().getType(),
                            resource.getTransactionDataClass().getType())
                    .toType()),
                Collections.<Type>emptyList(),
                members);
    }

    private List<TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = Lists.create();
        results.add(createGetCacheName());
        results.add(createCreateValueObject());
        results.add(createBuildLeftKey());
        results.add(createBuildRightKey());
        return results;
    }

    private MethodDeclaration createGetCacheName() {
        Expression result = Models.toLiteral(factory, resource.getCacheName());
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Protected()
                    .toAttributes(),
                importer.toType(String.class),
                factory.newSimpleName("getCacheName"),
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(new ExpressionBuilder(factory, result)
                    .toReturnStatement()));
    }

    private MethodDeclaration createCreateValueObject() {
        Expression result = new TypeBuilder(factory, importer.toType(resource.getMasterDataClass().getType()))
            .newObject()
            .toExpression();
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Protected()
                    .toAttributes(),
                importer.toType(resource.getMasterDataClass().getType()),
                factory.newSimpleName("createValueObject"),
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(new ExpressionBuilder(factory, result)
                    .toReturnStatement()));
    }

    private MethodDeclaration createBuildLeftKey() {
        return createBuildKey(
                "buildLeftKey",
                resource.getMasterDataClass(),
                resource.getMasterJoinKeys());
    }

    private MethodDeclaration createBuildRightKey() {
        return createBuildKey(
                "buildRightKey",
                resource.getTransactionDataClass(),
                resource.getTransactionJoinKeys());
    }

    private MethodDeclaration createBuildKey(
            String methodName,
            DataClass dataClass,
            List<Property> joinKeys) {
        assert methodName != null;
        assert dataClass != null;
        assert joinKeys != null;
        SimpleName value = factory.newSimpleName("value");
        SimpleName key = factory.newSimpleName("key");
        List<Statement> statements = Lists.create();
        for (Property join : joinKeys) {
            statements.add(new ExpressionBuilder(factory, key)
                .method("add", join.createGetter(value))
                .toStatement());
        }
        statements.add(new ExpressionBuilder(factory, key)
            .toReturnStatement());
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Protected()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                importer.toType(LookUpKey.class),
                factory.newSimpleName(methodName),
                Arrays.asList(new FormalParameterDeclaration[] {
                        factory.newFormalParameterDeclaration(
                                importer.toType(dataClass.getType()),
                                value),
                        factory.newFormalParameterDeclaration(
                                importer.toType(LookUpKey.class),
                                key),
                }),
                0,
                Collections.singletonList(importer.toType(IOException.class)),
                factory.newBlock(statements));
    }
}
