/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * マスタ分岐演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、トランザクションデータに対応するマスタデータを引き当て、
 * それらの内容に応じてトランザクションデータをそれぞれの出力に振り分ける。
 * </p>
 * <p>
 * 対象のメソッドは結合対象の二つのモデルオブジェクト型の引数を取る。
 * このとき最初の引数は、マスタデータなど結合条件に対してユニークであるようなモデルオブジェクトである必要がある。
 * 戻り値型には、分岐先を表現する列挙定数を返す。
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
 * <li> 返戻型には分岐先を表現する列挙型を指定する
 *   <ul>
 *   <li> その列挙型は、一つ以上の列挙定数を持つ </li>
 *   </ul>
 * </li>
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
public abstract class &lt;Operator-Class&gt; {
    ...

    /**
     &#42; レコードの状態ごとに処理を分岐する。
     &#42; &#64;param master マスタデータ、存在しない場合は{&#64;code null}
     &#42; &#64;param tx トランザクションデータ
     &#42; &#64;return 分岐先を表すオブジェクト
     &#42;&#47;
    &#64;MasterBranch
    public Status branchWithJoin(
            &#64;Key(group = "id") ItemMst master,
            &#64;Key(group = "itemId") HogeTrn tx) {
        if (master == null) {
            return Status.ERROR;
        }
        int price = master.getPrice();
        if (price < 0) {
            return Status.ERROR;
        }
        if (price >= 1000000) {
            return Status.EXPENSIVE;
        }
        return Status.CHEAP;
    }

    /**
     &#42; 値段に関するレコードの状態。
     &#42;&#47;
    public enum Status {
        /**
         &#42; 高い。
         &#42;&#47;
        EXPENSIVE,

        /**
         &#42; 安い。
         &#42;&#47;
        CHEAP,

        /**
         &#42; エラー。
         &#42;&#47;
        ERROR,
    }

    ...
}
</code></pre>
 * @see MasterJoin
 * @see Branch
 * @see MasterSelection
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterBranch {

    /**
     * マスタの入力ポート番号。
     */
    int ID_INPUT_MASTER = 0;

    /**
     * トランザクションの入力ポート番号。
     */
    int ID_INPUT_TRANSACTION = 1;

    /**
     * 利用するマスタ選択演算子のメソッド名。
     * @see MasterSelection
     */
    String selection() default MasterSelection.NO_SELECTION;
}
