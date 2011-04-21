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

/**
 * 変換演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、入力されたデータを他の種類のデータに変換し、出力に流す。
 * </p>
 * <p>
 * 一つの引数を取り、変換して別のモデルオブジェクトを返すプログラムを記述する。
 * </p>
 * <p>
 * また、引数には同メソッドで宣言した型変数を利用できるが、
 * 戻り値の型に型変数を含めることはできない。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に変換後のモデルオブジェクト型を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li> モデルオブジェクト型の引数 </li>
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
 * このフレームワークを正しく利用する限り、この注釈を付与するメソッドはスレッド安全となる。
 * ただし、同メソッドが共有データを操作したり、または共有データを操作する別のメソッドを起動したりする
 * 場合についてはスレッド安全でない。
 * </p>
 * <p>
 * 例:
 * </p>
<pre><code>
// スレッド安全なので変換後の結果オブジェクトを再利用可能にする
private Foo foo = new Foo();

/**
 &#42; レコードHogeを等価なFooに変換して返す。
 &#42; &#64;param hoge 変換するレコード
 &#42; &#64;return 変換後のレコード
 &#42;&#47;
&#64;Convert
public Foo toFoo(Hoge hoge) {
    foo.setValue(hoge.getValue());
    return foo;
}
</code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Convert {

    /**
     * 入力ポートの番号。
     */
    int ID_INPUT = 0;

    /**
     * 入力をそのまま出す出力ポートの番号。
     */
    int ID_OUTPUT_ORIGINAL = 0;

    /**
     * 変換後の出力ポートの番号。
     */
    int ID_OUTPUT_CONVERTED = 1;

    /**
     * 入力をそのまま出すポート名。
     */
    String originalPort() default "original";

    /**
     * 変換後の出力のポート名。
     */
    String convertedPort() default "out";
}
