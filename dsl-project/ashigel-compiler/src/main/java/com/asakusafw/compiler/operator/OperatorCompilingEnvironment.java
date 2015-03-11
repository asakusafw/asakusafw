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
package com.asakusafw.compiler.operator;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * Operator DSL Compilerの環境。
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorCompilingEnvironment {

    private static final Element[] EMPTY_ELEMENTS = new Element[0];

    private static final String KEY_FORCE_GENERATE = "com.asakusafw.operator.generate.force"; //$NON-NLS-1$

    private static final String DEFAULT_FORCE_GENERATE = "false"; //$NON-NLS-1$

    private final ProcessingEnvironment processingEnvironment;

    private final ModelFactory factory;

    private final OperatorCompilerOptions options;

    private final boolean forceGenerate;

    private final Set<String> generatedResourceKeys = Sets.create();

    /**
     * インスタンスを生成する。
     * @param processingEnvironment 注釈プロセッサの実行環境
     * @param factory Java DOMを構築するためのファクトリ
     * @param options コンパイラオプション
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public OperatorCompilingEnvironment(
            ProcessingEnvironment processingEnvironment,
            ModelFactory factory,
            OperatorCompilerOptions options) {
        Precondition.checkMustNotBeNull(processingEnvironment, "processingEnvironment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(options, "options"); //$NON-NLS-1$
        this.processingEnvironment = processingEnvironment;
        this.factory = factory;
        this.options = options;
        this.forceGenerate = Boolean.parseBoolean(options.getProperty(KEY_FORCE_GENERATE, DEFAULT_FORCE_GENERATE));
    }

    /**
     * 注釈プロセッサの実行環境を返す。
     * @return 注釈プロセッサの実行環境
     */
    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    /**
     * Java DOMを構築するためのファクトリを返す。
     * @return Java DOMを構築するためのファクトリ
     */
    public ModelFactory getFactory() {
        return factory;
    }

    /**
     * サービスをロードするためのクラスローダーを返す。
     * @return サービスをロードするためのクラスローダー
     */
    public ClassLoader getServiceClassLoader() {
        return options.getServiceClassLoader();
    }

    /**
     * コンパイラオプションの一覧を返す。
     * @return コンパイラオプションの一覧
     */
    public OperatorCompilerOptions getOptions() {
        return options;
    }

    /**
     * 環境に診断メッセージを表示するためのオブジェクトを返す。
     * @return 環境に診断メッセージを表示するためのオブジェクト
     */
    public Messager getMessager() {
        return getProcessingEnvironment().getMessager();
    }

    /**
     * 構文要素のユーティリティを返す。
     * @return 構文要素のユーティリティ
     */
    public Elements getElementUtils() {
        return getProcessingEnvironment().getElementUtils();
    }

    /**
     * 型のユーティリティを返す。
     * @return 型のユーティリティ
     */
    public Types getTypeUtils() {
        return getProcessingEnvironment().getTypeUtils();
    }

    /**
     * 指定のコンパイル単位をソースコードとして適切な位置に出力する。
     * @param unit 対象のコンパイル単位
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void emit(CompilationUnit unit) throws IOException {
        emit(unit, EMPTY_ELEMENTS);
    }

    /**
     * 指定のコンパイル単位をソースコードとして適切な位置に出力する。
     * @param unit 対象のコンパイル単位
     * @param originatingElements 生成するソースコードの元になった要素の一覧
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     * @since 0.7.0
     */
    public void emit(CompilationUnit unit, Element... originatingElements) throws IOException {
        Precondition.checkMustNotBeNull(unit, "unit"); //$NON-NLS-1$
        Filer filer = getProcessingEnvironment().getFiler();
        new Jsr269(factory).emit(filer, unit, originatingElements);
    }

    /**
     * Loads a mirror of the data model corresponded to the specified type.
     * @param type the corresponded type
     * @return the loaded data model mirror,
     *     or {@code null} if the type does not represent a valid data model for this repository
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataModelMirror loadDataModel(TypeMirror type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return options.getDataModelRepository().load(this, type);
    }

    /**
     * 指定のクラスに対応する宣言型(型引数無し)を返す。
     * @param type 対象の型
     * @return 対応する宣言型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public DeclaredType getDeclaredType(Class<?> type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        TypeElement elem = getElementUtils().getTypeElement(type.getName());
        if (elem == null) {
            throw new IllegalStateException(type.getName());
        }
        return getTypeUtils().getDeclaredType(elem);
    }

    /**
     * 指定の型の消去型を返す。
     * @param type 対象の型
     * @return 消去型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeMirror getErasure(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        // Eclipse のバグでイレイジャを正しく計算できない
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
            return getTypeUtils().getDeclaredType(element);
        }
        return getTypeUtils().erasure(type);
    }

    /**
     * Sets the target resources is generated.
     * @param key the target resource key
     * @since 0.7.0
     */
    public void setResourceGenerated(String key) {
        Precondition.checkMustNotBeNull(key, "key"); //$NON-NLS-1$
        generatedResourceKeys.add(key);
    }

    /**
     * Returns whether the target resources is generated or not.
     * @param key the target resource key
     * @return {@code true} if it is already generated, otherwise {@code false}
     * @since 0.7.0
     */
    public boolean isResourceGenerated(String key) {
        Precondition.checkMustNotBeNull(key, "key"); //$NON-NLS-1$
        if (forceGenerate) {
            return false;
        }
        return generatedResourceKeys.contains(key);
    }
}
