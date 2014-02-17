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

/**
 * 抽出演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、入力されたデータから任意のデータを抽出し、それぞれ出力に流す。
 * </p>
 * <p>
 * 一つのモデルオブジェクト型の引数と、
 * 複数の{@code 結果オブジェクト型}の引数を取り、
 * 引数から抽出した任意のモデルオブジェクトを結果オブジェクトに出力を行うプログラムを記述する。
 * </p>
 * <p>
 * また、引数には同メソッドで宣言した型変数を利用できるが、
 * 全ての結果オブジェクト型の出力に型変数を含める場合には、
 * 入力に同様の型変数を指定してある必要がある。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、一般的な演算子メソッドの要件の他に、
 * 下記の要件をすべて満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code void}を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li> モデルオブジェクト型の引数 </li>
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
// スレッド安全なので抽出結果のオブジェクトは再利用可能
private A a = new A();
private B b = new B();

/**
 &#42; レコードに含まれるそれぞれのフィールドを抽出し、出力する。
 &#42; &#64;param hoge 抽出対象のデータモデル
 &#42; &#64;param aResult aの抽出結果
 &#42; &#64;param bResult bの抽出結果
 &#42;&#47;
&#64;Extract
public void extractFields(
        Hoge hoge,
        Result&lt;A&gt; aResult,
        Result&lt;B&gt; bResult) {
    a.setValue(hoge.getA());
    aResult.add(a);
    b.setValue(hoge.getB0());
    bResult.add(b);
    b.setValue(hoge.getB1());
    bResult.add(b);
}
</code></pre>
 * @since 0.2.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extract {

    /**
     * 入力ポートの番号。
     */
    int ID_INPUT = 0;
}
