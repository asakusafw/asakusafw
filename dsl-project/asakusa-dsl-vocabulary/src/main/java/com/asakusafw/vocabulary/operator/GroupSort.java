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
package com.asakusafw.vocabulary.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import com.asakusafw.vocabulary.flow.processor.InputBuffer;
import com.asakusafw.vocabulary.model.Key;

/**
 * グループ整列演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、単一の入力をグループ化し、グループ内で整列したリストとして処理した結果を出力する。
 * </p>
 * <p>
 * 一つのモデルオブジェクトを要素に取る{@link List リスト型}の引数と、
 * 複数の{@code 結果オブジェクト型}の引数を取り、
 * リストの任意の要素について加工を行った後に、
 * 結果オブジェクトに出力を行うプログラムを記述する。
 * また、リスト型の引数には{@link Key}注釈を指定し、
 * {@link Key#group() グループ化}のためのプロパティ名と
 * {@link Key#order() 整列}のためのプロパティ名および整列方向を指定する必要がある。
 * </p>
 * <p>
 * また、引数には同メソッドで宣言した型変数を利用できるが、
 * 全ての結果オブジェクト型の出力に型変数を含める場合には、
 * いずれかの入力に同様の型変数を指定してある必要がある。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、一般的な演算子メソッドの要件の他に、
 * 下記の要件をすべて満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code void}を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li>
 *     モデルオブジェクトを要素に取る{@link List リスト型}の引数、
 *     さらに{@link Key}注釈でグループ化と整列のための情報を指定する
 *   </li>
 *   <li> 一つ以上の結果型の引数 </li>
 *   </ul>
 * </li>
 * <li> 以下の修飾子を付与する
 *   <ul>
 *   <li> (特になし) </li>
 *   </ul>
 * </li>
 * <li> 以下の修飾子は付与しない
 *   <ul>
 *   <li> {@code abstract} </li>
 *   </ul>
 * </li>
 * </ul>
 * <p>
 * 例:
 * </p>
<pre><code>
/**
 &#42; レコードHogeを名前ごとに年齢の若い順に並べ、先頭と末尾だけをそれぞれ結果に流す。
 &#42; &#64;param hogeList グループごとのリスト
 &#42; &#64;param first グループごとの先頭要素
 &#42; &#64;param last グループごとの末尾要素
 &#42;&#47;
&#64;GroupSort
public void firstLast(
        &#64;Key(group = "name", order = "age ASC") List&lt;Hoge&gt; hogeList,
        Result&lt;Hoge&gt; first,
        Result&lt;Hoge&gt; last) {
    first.add(hogeList.get(0));
    last.add(hogeList.get(hogeList.size() - 1));
}
</code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GroupSort {

    /**
     * 演算子の入力バッファの性質を指定する。
     * <p>
     * デフォルトではヒープ上に高速な入力バッファを構築し、
     * 巨大なグループに対しての処理は行えない。
     * </p>
     * @since 0.2.0
     * @see InputBuffer
     */
    InputBuffer inputBuffer() default InputBuffer.EXPAND;
}
