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
import java.util.List;

import com.asakusafw.vocabulary.model.Key;

//TODO i18n
/**
 * マスタ選択演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、トランザクションデータに対応する複数のマスタデータを引き当てたのち、
 * 実際に利用するマスタデータを選択する。
 * つまり、等価結合のみしか行えない環境において、重複するマスタデータから
 * 複雑な条件でマスタデータを選択するために利用する。
 * </p>
 * <p>
 * この演算子は単独では利用できず、必ず他の演算子に組み込まれる形で利用する。
 * この演算子を組み込むことができる演算子は、下記のとおりである。
 * </p>
 * <ul>
 * <li> {@link MasterJoin} </li>
 * <li> {@link MasterBranch} </li>
 * <li> {@link MasterCheck} </li>
 * <li> {@link MasterJoinUpdate} </li>
 * </ul>
 * <p>
 * この演算子を参照する他の演算子は、かならず同じクラス内に宣言されていなければならない。
 * また、その演算子は同一種類のマスタおよびトランザクションデータを取り扱うものでなければならない。
 * </p>
 * <p>
 * このメソッドは、マスタデータを表すモデルオブジェクトを要素に取る{@link List リスト型}の引数と、
 * トランザクションデータを表すモデルオブジェクト型の引数を順に取る
 * (いずれも<code>&#64;</code>{@link Key}注釈は不要である)。
 * メソッドの本体ではマスタデータを選択し、一つだけ選んで戻り値として返す。
 * 各引数の内容を変更した際の動作は規定されない。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に結合対象のモデルオブジェクト型(マスタデータ)を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li>
 *     結合対象のモデルオブジェクト型の引数 (マスタデータ)、
 *   </li>
 *   <li>
 *     結合対象のモデルオブジェクト型の引数、
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
 &#42; 有効なマスタを選択する。
 &#42; &#64;param masters 選択対象のマスタデータ一覧
 &#42; &#64;param tx トランザクションデータ
 &#42; &#64;return 実際に利用するマスタデータ、利用可能なものがない場合は{&#64;code null}
 &#42;&#47;
&#64;MasterSelection
public ItemMst selectItemMst(List&lt;ItemMst&gt; masters, HogeTrn tx) {
    for (ItemMst mst : masters) {
        if (mst.getStart() &lt;= tx.getDate() &amp;&amp;
                tx.getDate() &lt;= mst.getEnd()) {
            return mst;
        }
    }
    return null;
}

/**
 &#42; マスタの価格をトランザクションデータに設定する。
 &#42; &#64;param master マスタデータ
 &#42; &#64;param tx 変更するトランザクションデータ
 &#42;&#47;
&#64;MasterJoinUpdate(selection = &quot;selectItemMst&quot;)
public void updateWithMaster(
        &#64;Key(group = &quot;id&quot;) ItemMst master,
        &#64;Key(group = &quot;itemId&quot;) HogeTrn tx) {
    tx.setPrice(master.getPrice());
}
</code></pre>
 * @see MasterJoin
 * @see MasterBranch
 * @see MasterCheck
 * @see MasterJoinUpdate
 */
@OperatorHelper
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterSelection {

    /**
     * The pseudo method name which represents no methods are specified.
     */
    String NO_SELECTION = "-"; //$NON-NLS-1$

    /**
     * The common annotation element name for specifying the selection method name.
     */
    String ELEMENT_NAME = "selection"; //$NON-NLS-1$
}
