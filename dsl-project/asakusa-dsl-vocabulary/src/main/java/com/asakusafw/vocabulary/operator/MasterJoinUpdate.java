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

import com.asakusafw.vocabulary.model.Key;


/**
 * マスタつき更新演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、トランザクションデータに対応するマスタデータを引き当てたのち、
 * マスタデータの情報を利用してトランザクションデータの任意の項目を変更し、出力に流す。
 * </p>
 * <p>
 * 対象のメソッドは結合対象の二つのモデルオブジェクト型の引数を取る。
 * このとき最初の引数は、マスタデータなど結合条件に対してユニークであるようなモデルオブジェクトである必要がある。
 * また、モデルオブジェクト型の引数にはそれぞれ{@link Key}注釈を指定し、
 * {@link Key#group() グループ化}のためのプロパティ名を指定する必要がある
 * ({@link Key#order() 整列}のためのプロパティ名および整列方向に関する動作は規定されない)。
 * メソッドの本体では引数のトランザクションデータの内容を変更するプログラムを記述する
 * (マスタデータの内容を変更した際の動作は規定されない)。
 * </p>
 * <p>
 * また、引数には同メソッドで宣言した型変数を利用できるが、
 * 全ての結果オブジェクト型の出力に型変数を含める場合には、
 * いずれかの入力に同様の型変数を指定してある必要がある。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code void}を指定する </li>
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
 &#42; マスタの価格をトランザクションデータに設定する。
 &#42; &#64;param master マスタデータ
 &#42; &#64;param tx 変更するトランザクションデータ
 &#42;&#47;
&#64;MasterJoinUpdate
public void updateWithMaster(
        &#64;Key(group = "id") ItemMst master,
        &#64;Key(group = "itemId") HogeTrn tx) {
    tx.setPrice(master.getPrice());
}
</code></pre>
 * @see MasterJoin
 * @see Update
 * @see MasterSelection
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterJoinUpdate {

    /**
     * マスタの入力ポート番号。
     */
    int ID_INPUT_MASTER = 0;

    /**
     * トランザクションの入力ポート番号。
     */
    int ID_INPUT_TRANSACTION = 1;

    /**
     * 引き当てが成功した場合の出力先のポート番号。
     */
    int ID_OUTPUT_UPDATED = 0;

    /**
     * 引き当てが失敗した場合の出力先のポート番号。
     */
    int ID_OUTPUT_MISSED = 1;

    /**
     * 引き当ておよび更新が成功した場合の出力先のポート名。
     */
    String updatedPort() default "updated";

    /**
     * 引き当てが失敗した場合の出力先のポート名。
     */
    String missedPort() default "missed";

    /**
     * 利用するマスタ選択演算子のメソッド名。
     * @see MasterSelection
     */
    String selection() default MasterSelection.NO_SELECTION;
}
