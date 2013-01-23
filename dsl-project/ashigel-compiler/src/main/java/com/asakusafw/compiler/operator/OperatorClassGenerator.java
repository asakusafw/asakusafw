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
package com.asakusafw.compiler.operator;

import java.util.Collections;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.util.GeneratorUtil;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;

/**
 * 演算子クラスの派生生成物の情報を構築するジェネレータ。
 * @since 0.1.0
 * @version 0.5.0
 */
public abstract class OperatorClassGenerator {

    /**
     * 環境オブジェクト。
     */
    protected final OperatorCompilingEnvironment environment;

    /**
     * このジェネレータで利用するモデルファクトリ。
     */
    protected final ModelFactory factory;

    /**
     * このジェネレータで利用するインポート宣言を構築するためのビルダー。
     */
    protected final ImportBuilder importer;

    /**
     * このジェネレータが対象とする演算子クラスの情報。
     */
    protected final OperatorClass operatorClass;

    /**
     * このジェネレータが利用するユーティリティ。
     */
    protected final GeneratorUtil util;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param factory DOMを構築するためのファクトリ
     * @param importer インポート宣言を構築するビルダー
     * @param operatorClass 演算子クラスの情報
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public OperatorClassGenerator(
            OperatorCompilingEnvironment environment,
            ModelFactory factory,
            ImportBuilder importer,
            OperatorClass operatorClass) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(operatorClass, "operatorClass"); //$NON-NLS-1$
        this.environment = environment;
        this.factory = factory;
        this.importer = importer;
        this.operatorClass = operatorClass;
        this.util = new GeneratorUtil(environment, factory, importer);
    }

    /**
     * このジェネレータの情報を利用して型宣言の情報を生成する。
     * @return 生成したモデル
     */
    public TypeDeclaration generate() {
        SimpleName name = getClassName();
        importer.resolvePackageMember(name);
        return factory.newClassDeclaration(
                createJavadoc(),
                getAttribuets(),
                name,
                Collections.<TypeParameterDeclaration>emptyList(),
                getSuperClass(),
                Collections.<Type>emptyList(),
                createMembers());
    }

    /**
     * 生成する型の名前を返す。
     * @return 生成する型の名前
     */
    protected abstract SimpleName getClassName();

    /**
     * 生成する型の属性一覧を返す。
     * @return 生成する型の属性一覧
     * @since 0.5.0
     */
    protected abstract List<? extends Attribute> getAttribuets();

    /**
     * 生成する型のスーパータイプを返す。
     * @return 生成する型のスーパータイプ、明示的に利用しない場合は{@code null}
     */
    protected Type getSuperClass() {
        return null;
    }

    /**
     * 生成する型へのJavadocを返す。
     * @return 生成する型へのJavadoc
     */
    protected abstract Javadoc createJavadoc();

    /**
     * 生成する型のメンバー一覧を返す。
     * @return 生成する型のメンバー一覧
     */
    protected abstract List<TypeBodyDeclaration> createMembers();
}