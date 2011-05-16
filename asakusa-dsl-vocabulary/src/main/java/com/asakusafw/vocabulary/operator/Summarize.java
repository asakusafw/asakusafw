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
package com.asakusafw.vocabulary.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.model.Summarized;


/**
 * 単純集計演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、単一の入力を特定の項目でグループ化し、グループ内で集計した結果を出力する。
 * </p>
 * <p>
 * 対象のメソッドは抽象メソッドとして宣言し、集計対象のモデルオブジェクト型の引数を取る。
 * また、集計結果を格納するモデルオブジェクトの型を戻り値型に指定する。
 * 集計方法については戻り値型に{@link Summarized 定義されている}ため、
 * ここでは特に指定しない。
 * </p>
 * <p>
 * なお、この演算子メソッドには型引数を定義できない。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に集計結果となるモデル型を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li> 集計対象のモデルオブジェクト型の引数 </li>
 *   </ul>
 * </li>
 * <li> 以下の修飾子を付与する
 *   <ul>
 *   <li> {@code abstract} </li>
 *   </ul>
 * </li>
 * <li> 以下の修飾子は付与しない
 *   <ul>
 *   <li> (特になし) </li>
 *   </ul>
 * </li>
 * </ul>
 * <p>
 * 例:
 * </p>
<pre><code>
/**
 &#42; レコードHogeをHogeTotalに集計する。
 &#42; &#64;param hoge 集計対象
 &#42; &#64;return 集計結果
 &#42;&#47;
&#64;Summarize
public abstract HogeTotal summarize(Hoge hoge);
</code></pre>
 * @see Summarized
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Summarize {

    /**
     * 入力ポートの番号。
     */
    int ID_INPUT = 0;

    /**
     * 出力ポートの番号。
     */
    int ID_OUTPUT = 0;

    /**
     * 出力先のポート名。
     */
    String summarizedPort() default "out";
}
