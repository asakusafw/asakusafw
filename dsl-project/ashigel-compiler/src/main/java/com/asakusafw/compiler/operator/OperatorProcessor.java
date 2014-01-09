/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;

/**
 * 演算子DSLコンパイラのサブプロセッサとして動作する、個々の演算子注釈を処理するためのインターフェース。
 */
public interface OperatorProcessor {

    /**
     * このプロセッサの初期化が行われる際に起動される。
     * <p>
     * このメソッドはインスタンス生成後に、このオブジェクトへの他のあらゆる操作の前に一度だけ起動される。
     * </p>
     * @param env 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void initialize(OperatorCompilingEnvironment env);

    /**
     * このプロセッサが対象とする注釈の型を返す。
     * @return このプロセッサが対象とする注釈の型
     */
    Class<? extends Annotation> getTargetAnnotationType();

    /**
     * 指定の要素に付与された、この演算子の注釈を返す。
     * @param element 対象の要素
     * @return この演算子の注釈、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    AnnotationMirror getOperatorAnnotation(ExecutableElement element);

    /**
     * 演算子メソッドを解析して、出力に必要な情報を返す。
     * @param context 解析に利用する文脈
     * @return 演算子メソッドの情報、解釈できない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    OperatorMethodDescriptor describe(Context context);

    /**
     * 演算子メソッドを解析して、その実装となるメソッドを返す。
     * @param context 解析に利用する文脈
     * @return 演算子メソッドの実装、解釈できない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    List<? extends TypeBodyDeclaration> implement(Context context);

    /**
     * 演算子プロセッサの処理コンテキスト。
     */
    class Context {

        /**
         * 環境。
         */
        public final OperatorCompilingEnvironment environment;

        /**
         * 演算子注釈。
         */
        public final AnnotationMirror annotation;

        /**
         * 処理対象の要素。
         */
        public final ExecutableElement element;

        /**
         * 利用中のインポーター。
         */
        public final ImportBuilder importer;

        /**
         * 衝突しない名前の生成器。
         */
        public final NameGenerator names;

        /**
         * インスタンスを生成する。
         * @param environment 処理環境
         * @param annotation 処理対象の演算子注釈
         * @param element 処理対象の要素
         * @param importer 利用中のインポーター
         * @param names 衝突しない名前の生成器
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        public Context(
                OperatorCompilingEnvironment environment,
                AnnotationMirror annotation,
                ExecutableElement element,
                ImportBuilder importer,
                NameGenerator names) {
            Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(annotation, "annotation"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(names, "names"); //$NON-NLS-1$
            this.environment = environment;
            this.annotation = annotation;
            this.element = element;
            this.importer = importer;
            this.names = names;
        }
    }
}
