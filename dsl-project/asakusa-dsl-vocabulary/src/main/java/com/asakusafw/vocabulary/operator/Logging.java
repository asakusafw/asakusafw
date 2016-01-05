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

import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;

//TODO i18n
/**
 * ロギング演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、入力されたデータをそのまま出力するが、その際にロギングを行う。
 * </p>
 * <p>
 * 一つのモデルオブジェクト型の引数を取り、ロガーに出力するログ文字列を返す。
 * なお、引数のオブジェクトの内容を変更してはならない。
 * </p>
 * <p>
 * また、この注釈の要素に{@link Logging.Level}の値を指定することで、
 * 出力するログのレベルを変更できる (標準では「情報レベル」)。
 * この演算子を結線する場合、出力{@code out}は他の演算子等と結線されていなくても
 * エラーにならないという特性を持つ。
 * </p>
 * <p>
 * また、引数には同メソッドで宣言した型変数を利用できる。
 * 例えば上限境界の無い型引数を定義して、引数の型として利用すると、
 * すべてのデータを受け取れるようなロギング演算子を定義できる。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、一般的な演算子メソッドの要件の他に、
 * 下記の要件をすべて満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code String}を指定する </li>
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
 &#42; エラーログを出力する。
 &#42; &#64;param hoge 更新するレコード
 &#42;&#47;
&#64;Logging(Logging.Level.ERROR)
public String error(Hoge hoge) {
    return MessageFormat.format("hoge = {0}", hoge.getValueOption());
}
</code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Logging {

    /**
     * The input port number.
     */
    int ID_INPUT = 0;

    /**
     * The output port number.
     */
    int ID_OUTPUT = 0;

    /**
     * The default output port name.
     */
    String outputPort() default "out";

    /**
     * ログのレベル。
     */
    Level value() default Level.INFO;

    /**
     * ログのレベル。
     */
    enum Level implements FlowElementAttribute {

        /**
         * 障害レベル。
         */
        ERROR,

        /**
         * 警告レベル。
         */
        WARN,

        /**
         * 情報レベル。
         */
        INFO,

        /**
         * デバッグレベル。
         */
        DEBUG,
        ;

        /**
         * 規定のレベルを返す。
         * @return 規定のレベル
         */
        public static Level getDefault() {
            return INFO;
        }
    }
}
