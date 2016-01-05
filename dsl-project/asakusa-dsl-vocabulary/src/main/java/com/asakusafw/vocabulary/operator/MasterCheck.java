/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

//TODO i18n
/**
 * マスタ確認演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、トランザクションデータに対応するマスタデータを引き当てて確認し、
 * マスタデータを発見できたものと発見できなかったものに分けて出力に流す。
 * </p>
 * <p>
 * 対象のメソッドは抽象メソッドとして宣言し、結合対象の二つのモデルオブジェクト型の引数を取る。
 * このとき最初の引数は、マスタデータなど結合条件に対してユニークであるようなモデルオブジェクトである必要がある。
 * 戻り値型には{@code boolean型}を指定する。
 * また、モデルオブジェクト型の引数にはそれぞれ{@link Key}注釈を指定し、
 * {@link Key#group() グループ化}のためのプロパティ名を指定する必要がある
 * ({@link Key#order() 整列}のためのプロパティ名および整列方向に関する動作は規定されない)。
 * </p>
 * <p>
 * 引数には同メソッドで宣言した型変数を利用できる。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code boolean}型を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li>
 *     結合対象のモデルオブジェクト型の引数 (マスタデータ)、
 *     さらに{@link Key}注釈でグループ化のための情報を指定する
 *   </li>
 *   <li>
 *     結合対象のモデルオブジェクト型の引数、
 *     さらに{@link Key}注釈でグループ化のための情報を指定する
 *   </li>
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
 &#42; レコードHogeTrnに対するHogeMstが存在する場合に{&#64;code true}を返す。
 &#42; &#64;param master マスタデータ
 &#42; &#64;param tx トランザクションデータ
 &#42; &#64;return HogeMstが存在する場合のみ{&#64;code true}
 &#42;&#47;
&#64;MasterCheck
public abstract boolean exists(
        &#64;Key(group = &quot;id&quot;) HogeMst master,
        &#64;Key(group = &quot;masterId&quot;) HogeTrn tx);
</code></pre>
 * @see MasterJoin
 * @see MasterSelection
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterCheck {

    /**
     * The input port number for the <em>master</em> data.
     */
    int ID_INPUT_MASTER = 0;

    /**
     * The input port number for the <em>transaction</em> data.
     */
    int ID_INPUT_TRANSACTION = 1;

    /**
     * The output port number for the successfully checked data.
     */
    int ID_OUTPUT_FOUND = 0;

    /**
     * The output port number for the <em>master</em> missing data.
     */
    int ID_OUTPUT_MISSED = 1;

    /**
     * The default port name of {#ID_OUTPUT_FOUND}.
     */
    String foundPort() default "found";

    /**
     * The default port name of {@link #ID_OUTPUT_MISSED}.
     */
    String missedPort() default "missed";

    /**
     * The selector method name.
     * The target method must be declared in the same class.
     * @see MasterSelection
     */
    String selection() default MasterSelection.NO_SELECTION;
}
