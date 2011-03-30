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
package com.asakusafw.compiler.operator.util;

import java.text.MessageFormat;
import java.util.Collections;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.ashigeru.lang.java.jsr269.bridge.Jsr269;
import com.ashigeru.lang.java.model.syntax.Literal;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * JavaのDOMを構築する際のユーティリティ。
 */
public class GeneratorUtil {

    private final OperatorCompilingEnvironment environment;

    private final ModelFactory factory;

    private final ImportBuilder importer;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param factory DOMを構築するためのファクトリ
     * @param importer インポート宣言を構築するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public GeneratorUtil(
            OperatorCompilingEnvironment environment,
            ModelFactory factory,
            ImportBuilder importer) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        this.environment = environment;
        this.factory = factory;
        this.importer = importer;
    }

    /**
     * 指定の型に対する演算子ファクトリークラスの単純名を返す。
     * @param type 対象の型
     * @return 対応する単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public final SimpleName getFactoryName(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return factory.newSimpleName(MessageFormat.format(
                "{0}{1}",
                type.getSimpleName(),
                "Factory"));
    }

    /**
     * 指定の型に対する演算子実装クラスの単純名を返す。
     * @param type 対象の型
     * @return 対応する単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public final SimpleName getImplementorName(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return factory.newSimpleName(getImplmentorName(
                type.getSimpleName().toString()));
    }

    /**
     * 指定の型に対する演算子実装クラスの名前を返す。
     * @param typeName 対象の型名
     * @return 対応する単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static final String getImplmentorName(String typeName) {
        Precondition.checkMustNotBeNull(typeName, "typeName"); //$NON-NLS-1$
        return MessageFormat.format(
                "{0}{1}",
                typeName,
                "Impl");
    }

    /**
     * 指定の型に対応する型のモデルを返す。
     * @param type 対象の型
     * @return 型のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final Type t(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return importer.resolve(new Jsr269(factory).convert(type));
    }

    /**
     * 指定の型に対応する型のモデルを返す。
     * @param type 対象の型
     * @return 型のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final Type t(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        DeclaredType t = environment.getTypeUtils().getDeclaredType(type);
        return importer.resolve(new Jsr269(factory).convert(t));
    }

    /**
     * 指定の型に対応する型のモデルを返す。
     * @param type 対象の型
     * @return 型のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final Type t(java.lang.reflect.Type type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return importer.toType(type);
    }

    /**
     * 指定の値に対応するリテラルのモデルを返す。
     * @param value 対象の値
     * @return 指定の値に対応するリテラルのモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Literal v(String value) {
        Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
        return Models.toLiteral(factory, value);
    }

    /**
     * 指定の値に対応するリテラルのモデルを返す。
     * @param pattern {@link MessageFormat}のパターン
     * @param arguments {@link MessageFormat}の引数一覧
     * @return 指定の値に対応するリテラルのモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Literal v(String pattern, Object... arguments) {
        Precondition.checkMustNotBeNull(pattern, "pattern"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arguments, "arguments"); //$NON-NLS-1$
        return Models.toLiteral(factory, MessageFormat.format(pattern, arguments));
    }

    /**
     * 指定のデータの種類に対する{@link Source 結果型}を返す。
     * @param type 対象データの種類
     * @return 対応する結果型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Type toSourceType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(Source.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * 指定のデータの種類に対する{@link In 入力型}を返す。
     * @param type 対象データの種類
     * @return 対応する入力型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Type toInType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(In.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * 指定のデータの種類に対する{@link Out 出力型}を返す。
     * @param type 対象データの種類
     * @return 対応する出力型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Type toOutType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(Out.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }
}
