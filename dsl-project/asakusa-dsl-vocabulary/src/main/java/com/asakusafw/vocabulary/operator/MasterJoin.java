/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.vocabulary.model.Joined;

//TODO i18n
/**
 * マスタ結合演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、トランザクションデータに対して対応するマスタデータを結合し、出力に流す。
 * </p>
 * <p>
 * 対象のメソッドは抽象メソッドとして宣言し、結合対象の二つのモデルオブジェクト型の引数を取る。
 * このとき最初の引数は、マスタデータなど結合条件に対してユニークであるようなモデルオブジェクトである必要がある。
 * また、戻り値型には結合結果のモデルオブジェクトの型を指定する。
 * 結合結果のモデルオブジェクトの型は、必ず結合対象の二つのモデルオブジェクトを結合したものを
 * 表現しなければならない。
 * 結合条件や結合方法については戻り値型に{@link Joined 定義されている}ため、
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
 *   <li> 結合対象のモデルオブジェクト型の引数 (マスタデータ) </li>
 *   <li> 結合対象のモデルオブジェクト型の引数 </li>
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
 &#42; レコードHogeMstとHogeTrnを結合し、結合結果のHogeを返す。
 &#42; &#64;param master マスタデータ
 &#42; &#64;param tx トランザクションデータ
 &#42; &#64;return 結合結果
 &#42;&#47;
&#64;MasterJoin
public abstract Hoge join(HogeMst master, HogeTrn tx);
</code></pre>
 * @see Joined
 * @see MasterSelection
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterJoin {

    /**
     * The input port number for the <em>master</em> data.
     */
    int ID_INPUT_MASTER = 0;

    /**
     * The input port number for the <em>transaction</em> data.
     */
    int ID_INPUT_TRANSACTION = 1;

    /**
     * The output port number for the successfully joined data.
     */
    int ID_OUTPUT_JOINED = 0;

    /**
     * The output port number for the <em>master</em> missing data.
     */
    int ID_OUTPUT_MISSED = 1;

    /**
     * The default port name of {@link #ID_OUTPUT_JOINED}.
     */
    String joinedPort() default "joined";

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
