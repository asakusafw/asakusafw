/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ResourceFragment;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.ashigeru.lang.java.model.syntax.ConstructorDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * {@code *FragmentEmitter}の共通処理。
 */
public class FragmentConnection {

    private Map<FlowResourceDescription, SimpleName> resources = new HashMap<FlowResourceDescription, SimpleName>();

    private Map<FlowElementOutput, SimpleName> successors = new HashMap<FlowElementOutput, SimpleName>();

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
        List<FieldDeclaration> results = new ArrayList<FieldDeclaration>();
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
        List<FormalParameterDeclaration> parameters = new ArrayList<FormalParameterDeclaration>();
        List<Statement> statements = new ArrayList<Statement>();
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
        Map<FlowResourceDescription, Expression> results =
            new HashMap<FlowResourceDescription, Expression>();
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
        Map<FlowElementPortDescription, Expression> results =
            new HashMap<FlowElementPortDescription, Expression>();
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
