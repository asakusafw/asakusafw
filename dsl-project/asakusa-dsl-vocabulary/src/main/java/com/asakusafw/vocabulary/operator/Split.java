/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

//TODO i18n
/**
 * 分割演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、結合済みのデータを入力に取り、結合前のデータに分割してそれぞれ出力する。
 * </p>
 * <p>
 * 一つのモデルオブジェクト型の引数と、二つの{@code 結果オブジェクト}の引数を取り、
 * 抽象メソッドとして宣言する。
 * このメソッドはモデルオブジェクトの結合情報を元に、
 * 結合前のモデルオブジェクトをそれぞれ返すようなプログラムを自動的に生成する。
 * ただし、分割対象のモデルがあらかじめ結合されたものであり、
 * かつ分割先がそれぞれ結合元のモデルである必要がある。
 * </p>
 * <p>
 * なお、この演算子メソッドには型引数を定義できない。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code void}を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li> 結合済みのモデルオブジェクト型の引数 </li>
 *   <li> {@code Result}型の型引数に、一つ目の分割先のモデルオブジェクト型を指定した引数 </li>
 *   <li> {@code Result}型の型引数に、二つ目の分割先のモデルオブジェクト型を指定した引数 </li>
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
 &#42; レコードHogeFooをHogeとFooに分割する。
 &#42; &#64;param joined 分割するレコード
 &#42; &#64;param hoge 分割後のHoge
 &#42; &#64;param foo 分割後のFoo
 &#42;&#47;
&#64;Split
public abstract void split(HogeFoo joined, Result&lt;Hoge&gt; hoge, Result&lt;Foo&gt; foo);
</code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Split {

    /**
     * The input port number.
     */
    int ID_INPUT = 0;

    /**
     * The first (left) output port number.
     */
    int ID_OUTPUT_LEFT = 0;

    /**
     * The second (right) output port number.
     */
    int ID_OUTPUT_RIGHT = 1;
}
