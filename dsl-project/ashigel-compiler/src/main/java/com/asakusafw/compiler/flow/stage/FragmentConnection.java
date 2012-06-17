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
package com.asakusafw.compiler.flow.stage;

import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ResourceFragment;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * {@code *FragmentEmitter}の共通処理。
 */
public class FragmentConnection {

    private Map<FlowResourceDescription, SimpleName> resources = Maps.create();

    private Map<FlowElementOutput, SimpleName> successors = Maps.create();

    private ModelFactory factory;

    private Fragment fragment;

    private ImportBuilder importer;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param fragment 対象のフラグメント
     * @param names 名前を生成するオブジェクト
     * @param importer インポートを管理するオブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FragmentConnection(
            FlowCompilingEnvironment environment,
            Fragment fragment,
            NameGenerator names,
            ImportBuilder importer) {
        assert environment != null;
        assert fragment != null;
        assert names != null;
        assert importer != null;
        this.factory = environment.getModelFactory();
        this.fragment = fragment;
        this.importer = importer;
        for (ResourceFragment resource : fragment.getResources()) {
            SimpleName name = names.create("resource");
            resources.put(resource.getDescription(), name);
        }
        for (FlowElementOutput output : fragment.getOutputPorts()) {
            SimpleName name = names.create(output.getDescription().getName());
            successors.put(output, name);
        }
    }


    /**
     * リソースや出力に関するフィールドの一覧を返す。
     * @return リソースや出力に関するフィールドの一覧
     */
    public List<FieldDeclaration> createFields() {
        List<FieldDeclaration> results = Lists.create();
        for (ResourceFragment resource : fragment.getResources()) {
            results.add(createResourceField(resource));
        }
        for (FlowElementOutput output : fragment.getOutputPorts()) {
            results.add(createOutputField(output));
        }
        return results;
    }

    /**
     * コンストラクタの宣言を返す。
     * @param className クラス名
     * @return コンストラクタの宣言
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ConstructorDeclaration createConstructor(SimpleName className) {
        Precondition.checkMustNotBeNull(className, "className"); //$NON-NLS-1$
        JavadocBuilder javadoc = new JavadocBuilder(factory)
            .text("インスタンスを生成する。");
        List<FormalParameterDeclaration> parameters = Lists.create();
        List<Statement> statements = Lists.create();
        for (ResourceFragment resource : fragment.getResources()) {
            SimpleName param = getResource(resource.getDescription());
            javadoc.param(param)
                .text(resource.getDescription().toString());
            parameters.add(factory.newFormalParameterDeclaration(
                    importer.toType(resource.getCompiled().getQualifiedName()),
                    param));
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(param)
                .assignFrom(param)
                .toStatement());
        }
        for (FlowElementOutput output : fragment.getOutputPorts()) {
            SimpleName chain = successors.get(output);
            assert chain != null;
            javadoc.param(chain)
                .code("{0}#{1}",
                        output.getOwner().getDescription().getName(),
                        output.getDescription().getName())
                .text("への出力");
            parameters.add(factory.newFormalParameterDeclaration(
                    importer.resolve(factory.newParameterizedType(
                            Models.toType(factory, Result.class),
                            Models.toType(factory, output.getDescription().getDataType()))),
                    chain));
            statements.add(new ExpressionBuilder(factory, factory.newThis())
                .field(chain)
                .assignFrom(chain)
                .toStatement());
        }
        return factory.newConstructorDeclaration(
                javadoc.toJavadoc(),
                new AttributeBuilder(factory)
                    .Public()
                    .toAttributes(),
                className,
                parameters,
                statements);
    }

    private FieldDeclaration createResourceField(ResourceFragment resource) {
        assert resource != null;
        return factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .Final()
                    .toAttributes(),
                importer.toType(resource.getCompiled().getQualifiedName()),
                getResource(resource.getDescription()),
                null);
    }

    private FieldDeclaration createOutputField(FlowElementOutput output) {
        assert output != null;
        return factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .Final()
                    .toAttributes(),
                importer.resolve(factory.newParameterizedType(
                        Models.toType(factory, Result.class),
                        Models.toType(factory, output.getDescription().getDataType()))),
                successors.get(output),
                null);
    }

    private SimpleName getResource(FlowResourceDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        SimpleName name = resources.get(description);
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        return name;
    }

    /**
     * リソースに関する定義とそれを参照するための式の表を返す。
     * @return リソースに関する定義とそれを参照するための式の表
     */
    public Map<FlowResourceDescription, Expression> getResources() {
        Map<FlowResourceDescription, Expression> results = Maps.create();
        for (ResourceFragment key : fragment.getResources()) {
            SimpleName name = resources.get(key.getDescription());
            assert name != null;
            results.put(
                    key.getDescription(),
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(name)
                        .toExpression());
        }
        return results;
    }

    /**
     * 出力に関する定義とそれを参照するための式の表を返す。
     * @return 出力に関する定義とそれを参照するための式の表
     */
    public Map<FlowElementPortDescription, Expression> getOutputs() {
        Map<FlowElementPortDescription, Expression> results = Maps.create();
        for (FlowElementOutput key : fragment.getOutputPorts()) {
            SimpleName name = successors.get(key);
            assert name != null;
            results.put(
                    key.getDescription(),
                    new ExpressionBuilder(factory, factory.newThis())
                        .field(name)
                        .toExpression());
        }
        return results;
    }
}
