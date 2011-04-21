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

import com.asakusafw.vocabulary.model.Key;


/**
 * 畳み込み演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、単一の入力を特定の項目でグループ化し、グループ内で畳みこんだ結果を出力する。
 * </p>
 * <p>
 * 同じモデル型の二つの引数を取り、メソッドの本体で第一引数の内容を変更するプログラムを記述する。
 * また、第一引数には{@link Key}注釈を指定し、
 * {@link Key#group() グループ化}のためのプロパティ名を指定する必要がある
 * ({@link Key#order() 整列}のためのプロパティ名および整列方向は無視される)。
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
 *   <li>
 *     ここまでの畳み込みの結果を表すモデルオブジェクト型の引数、
 *     さらに{@link Key}注釈でグループ化のための情報を指定する
 *   </li>
 *   <li>
 *     畳み込み対象のモデルオブジェクト型の引数
 *   </li>
 *   </ul>
 * </li>
 * <li> 以下の修飾子を付与する
 *   <ul>
 *   <li> {@code abstract} </li>
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
 &#42; レコードHogeを畳み込む。
 &#42; &#64;param left ここまでの畳み込みの結果
 &#42; &#64;param right 畳み込む対象
 &#42;&#47;
&#64;Fold
public void fold(&#64;Key(group = "name") Hoge left, Hoge right) {
    // &#64;Summarizeを手動で行うイメージで、leftに次々とrightを加える
    left.setValue(left.getValue() + right.getValue());
}
</code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fold {

    /**
     * 入力ポートの番剛。
     */
    int ID_INPUT = 0;

    /**
     * 出力ポートの番号。
     */
    int ID_OUTPUT = 0;

    /**
     * 入力元のポート名。
     */
    String INPUT = "in";

    /**
     * 出力先のポート名。
     */
    String outputPort() default "out";
}
