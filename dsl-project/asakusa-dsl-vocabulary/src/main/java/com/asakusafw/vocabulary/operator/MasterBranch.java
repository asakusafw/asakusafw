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

import com.asakusafw.vocabulary.model.Key;

//TODO i18n
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
 *   <li> その列挙型は、{@code public} として宣言されている </li>
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
     &#42; Returns the record status.
     &#42; &#64;param master the master data, or {&#64;code null} if there is no suitable data for the record
     &#42; &#64;param tx the target record
     &#42; &#64;return the branch target status
     &#42;&#47;
    &#64;MasterBranch
    public Status branchWithJoin(
            &#64;Key(group = &quot;id&quot;) ItemMst master,
            &#64;Key(group = &quot;itemId&quot;) HogeTrn tx) {
        if (master == null) {
            return Status.ERROR;
        }
        int price = master.getPrice();
        if (price &lt; 0) {
            return Status.ERROR;
        } else if (price &lt; 1000000) {
            return Status.CHEAP;
        } else {
            return Status.EXPENSIVE;
        }
    }

    /**
     &#42; Represents price status.
     &#42;&#47;
    public enum Status {

        /**
         &#42; Expensive price.
         &#42;&#47;
        EXPENSIVE,

        /**
         &#42; Cheap price.
         &#42;&#47;
        CHEAP,

        /**
         &#42; Erroneous price.
         &#42;&#47;
        ERROR,
    }
    ...
}
</code></pre>
 * @see MasterJoin
 * @see Branch
 * @see MasterSelection
 * @since 0.1.0
 * @version 0.5.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterBranch {

    /**
     * The input port number for the <em>master</em> data.
     */
    int ID_INPUT_MASTER = 0;

    /**
     * The input port number for the <em>transaction</em> data.
     */
    int ID_INPUT_TRANSACTION = 1;

    /**
     * The selector method name.
     * The target method must be declared in the same class.
     * @see MasterSelection
     */
    String selection() default MasterSelection.NO_SELECTION;
}
