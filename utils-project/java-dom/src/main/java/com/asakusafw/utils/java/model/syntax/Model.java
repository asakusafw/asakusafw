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
package com.asakusafw.utils.java.model.syntax;

/**
 * すべてのモデルの基底となるインターフェース。
 */
public interface Model {

    /**
     * このモデルの種類を表す値を返す。
     * @return このモデルの種類を表す値
     */
    ModelKind getModelKind();

    /**
     * 指定のビジタを受け入れる。
     * @param <R> 戻り値の型
     * @param <C> コンテキストオブジェクトの型
     * @param <E> ビジタで発生する例外の型
     * @param visitor 受け入れるビジタ
     * @param context コンテキストオブジェクト(省略可)
     * @return ビジタの実行結果
     * @throws E ビジタでの処理中に例外が発生した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context)
        throws E;

    /**
     * このモデルのハッシュ値を返す。
     * <p>
     * なお、{@link #findModelTrait(Class) モデルトレイト}の有無はハッシュ値に影響しない。
     * </p>
     * @return このモデルのハッシュ値
     */
    @Override
    int hashCode();

    /**
     * このモデルが指定のモデルと同一の構造をとる場合のみ{@code true}を返す。
     * <p>
     * 次のすべてを満たす場合のみこのモデルと比較対象のモデルは同一の構造をとる。
     * </p>
     * <ul>
     *   <li> このモデルと比較対象のモデルが同一の種類を表現する </li>
     *   <li> それぞれのモデルが有するすべてのプロパティについて、下記が成り立つ:
     *     <ul>
     *       <li>
     *         プロパティがモデルを保持する場合、それぞれのプロパティが保持するモデルが
     *         再帰的に同一の構造をとる
     *       </li>
     *       <li>
     *         そうでなく、プロパティがモデルのリストを保持する場合、それぞれのプロパティが保持するリストには
     *         同一の位置に同一の構造をとるモデルが出現する
     *       </li>
     *       <li>
     *         そうでなく、プロパティが値を保持する場合、それぞれのプロパティが保持する値は同値である
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>
     * なお、{@link #findModelTrait(Class) モデルトレイト}の有無は上記の動作に影響しない。
     * </p>
     * @param other 比較対象のモデル
     * @return
     *      このモデルと比較対象のモデルが同一の構造をとる場合に{@code true}、
     *      そうでない場合は{@code false}
     */
    @Override
    boolean equals(Object other);

    /**
     * このモデルオブジェクトに登録された、指定された種類のトレイトオブジェクトを返す。
     * @param <T> トレイトの種類
     * @param traitClass トレイトの種類
     * @return 登録されたトレイト、指定の種類が登録されていない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    <T> T findModelTrait(Class<T> traitClass);

    /**
     * このモデルオブジェクトに指定のトレイトオブジェクトを登録する。
     * @param <T> トレイトの種類
     * @param traitClass トレイトの種類
     * @param traitObject 登録するトレイトオブジェクト、{@code null}を指定した場合は
     *     指定の種類に関するトレイトオブジェクトの登録を解除する
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    <T> void putModelTrait(Class<T> traitClass, T traitObject);
}
