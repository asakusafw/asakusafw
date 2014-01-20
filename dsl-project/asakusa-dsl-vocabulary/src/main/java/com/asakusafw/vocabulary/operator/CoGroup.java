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
 * グループ結合演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、二種類のデータをそれぞれ条件に応じてグループ化してリストを作成し、
 * それらのリストを処理した結果を出力する。
 * </p>
 * <p>
 * このメソッドは、二つのモデルオブジェクトを要素に取る{@link List リスト型}の引数と、
 * 複数の{@code 結果オブジェクト型}の引数を取り、
 * リストの任意の要素について加工を行った後に、
 * 結果オブジェクトに出力を行うプログラムを記述する。
 * また、それぞれのリスト型の引数には{@link Key}注釈を指定し、
 * {@link Key#group() グループ化}のためのプロパティ名を指定する必要がある
 * ({@link Key#order() 整列}のためのプロパティ名および整列方向も指定可能である。
 * 指定した場合、引数に渡されるリストはその条件で整列される)。
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
 &#42; HogeとFooをHogeのIDでグループ化し、重複なしで突合できたもののみを結果として出力する。
 &#42; それ以外の値はエラーとして出力する。
 &#42; &#64;param hogeList Hogeのグループごとのリスト
 &#42; &#64;param fooList Fooのグループごとのリスト
 &#42; &#64;param hogeResult 成功したHoge
 &#42; &#64;param fooResult 成功したFoo
 &#42; &#64;param hogeError 失敗したHoge
 &#42; &#64;param fooError 失敗したFoo
 &#42;&#47;
&#64;CoGroup
public void checkUp(
        &#64;Key(group = "id") List&lt;Hoge&gt; hogeList,
        &#64;Key(group = "hogeId") List&lt;Foo&gt; fooList,
        Result&lt;Hoge&gt; hogeResult,
        Result&lt;Foo&gt; fooResult,
        Result&lt;Hoge&gt; hogeError,
        Result&lt;Foo&gt; fooError) {
    // いずれも存在＋重複なしで突合成功
    if (hogeList.size() == 1 &amp;&amp; fooList.size() == 1) {
        hogeResult.add(hogeList.get(0));
        fooResult.add(fooList.get(0));
    }
    // それ以外はエラー
    else {
        for (Hoge hoge : hogeList) {
            hogeError.add(hoge);
        }
        for (Foo foo : fooList) {
            fooError.add(foo);
        }
    }
}
</code></pre>
 * @see MasterJoin
 * @see GroupSort
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CoGroup {

    /**
     * 演算子の入力バッファの性質を指定する。
     * <p>
     * デフォルトではヒープ上に高速な入力バッファを構築し、
     * 巨大なグループに対しの処理は行えない。
     * </p>
     * @since 0.2.0
     * @see InputBuffer
     */
    InputBuffer inputBuffer() default InputBuffer.EXPAND;
}
