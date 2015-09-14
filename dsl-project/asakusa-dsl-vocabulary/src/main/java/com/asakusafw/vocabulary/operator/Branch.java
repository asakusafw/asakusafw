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
 * 分岐演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、入力をデータの内容を元に分類し、それぞれの出力に振り分ける。
 * </p>
 * <p>
 * 一つの引数を取り、分岐先を表現する列挙定数を返す。
 * </p>
 * <p>
 * 引数には同メソッドで宣言した型変数を利用できるが、
 * 戻り値の型に型変数を含めることはできない。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、一般的な演算子メソッドの要件の他に、
 * 下記の要件をすべて満たす必要がある。
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
 * 以下は演算子クラスの例である。
 * </p>
<pre><code>
public abstract class &lt;Operator-Class&gt; {
    ...

    /**
     &#42; レコードの状態ごとに処理を分岐する。
     &#42; &#64;param hoge 対象のレコード
     &#42; &#64;return 分岐先を表すオブジェクト
     &#42;&#47;
    &#64;Branch
    public Status select(Hoge hoge) {
        int price = hoge.getPrice();
        if (price &lt; 0) {
            return Status.ERROR;
        }
        if (price &gt;= 1000000) {
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
 * @since 0.1.0
 * @version 0.5.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Branch {

    /**
     * The input port number.
     */
    int ID_INPUT = 0;
}
