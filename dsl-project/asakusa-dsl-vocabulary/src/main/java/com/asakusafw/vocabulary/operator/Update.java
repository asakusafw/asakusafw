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
package com.asakusafw.vocabulary.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO i18n
/**
 * 更新演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、単一の入力に流れるデータの項目を変更し、出力に流す。
 * </p>
 * <p>
 * 一つの引数を取り、メソッドの本体で引数の内容を変更するプログラムを記述する。
 * </p>
 * <p>
 * 引数には同メソッドで宣言した型変数を利用できる。
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
 &#42; レコードの値に100を設定する。
 &#42; &#64;param hoge 更新するレコード
 &#42;&#47;
&#64;Update
public void edit(Hoge hoge) {
    hoge.setValue(100);
}
</code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Update {

    /**
     * The input port number.
     */
    int ID_INPUT = 0;

    /**
     * The output port number.
     */
    int ID_OUTPUT = 0;

    /**
     * The default port name of {@link #ID_OUTPUT}.
     */
    String outputPort() default "out";
}
