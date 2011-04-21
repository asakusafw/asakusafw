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
 * 重複検出演算子を表すメソッドに付与する注釈。
 * <p>
 * この演算子は、単一の入力を特定の項目でグループ化し、
 * 同項目の値に重複があるものとそうでないものに分類した上で出力する。
 * </p>
 * <p>
 * 対象のメソッドは抽象メソッドとして宣言し、重複を検出する対象のモデルオブジェクト型の引数を取る。
 * また、同引数には{@link Key}注釈を指定し、
 * {@link Key#group() グループ化}のためのプロパティ名を指定する。
 * </p>
 * <p>
 * 引数には同メソッドで宣言した型変数を利用できる。
 * </p>
 * <p>
 * この注釈を付与するメソッドは、下記の要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> 返戻型に{@code void}を指定する </li>
 * <li> 以下の引数を宣言する
 *   <ul>
 *   <li> モデルオブジェクト型の引数、さらに{@link Key}注釈でグループ化のプロパティを指定する </li>
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
 &#42; レコードHogeをプロパティfooにおいて重複を検出する。
 &#42; &#64;param hoge 重複を検出する対象
 &#42;&#47;
&#64;Unique
public abstract void unique(&#64;Key(group="foo") Hoge hoge);
</code></pre>
 * @deprecated {@link GroupSort}の整列順序を利用しないことで代用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface Unique {

    // no members
}
