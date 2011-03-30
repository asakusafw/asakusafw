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
package com.asakusafw.vocabulary.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * レコードのキーを指定する注釈。
 * <p>
 * 下記のように、グループ化のプロパティ名や順序付けの条件を指定できる。
 * </p>
<pre><code>
// 名前でグループ化
&#64;Key(group = "name")

// 名前と性別でグループ化
&#64;Key(group = { "name", "sex" })

// 名前でグループ化し、年齢の昇順で整列
&#64;Key(group = "name", order = "age ASC")

// 名前でグループ化し、収入の昇順, 年齢の降順で整列
&#64;Key(group = "name", order = { "income ASC", "age DESC" })

// 全てを単一のグループにまとめ、回数の降順で整列
&#64;Key(group = {}, order = "count DESC")
</code></pre>
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Key {

    /**
     * グループ化条件に利用するフィールド一覧。
     * <p>
     * 対象とする値のプロパティ名の一覧を指定すると、それらのプロパティごとにグループを構成する。
     * </p>
     * <p>
     * この要素に明示的に空の配列(<code>group = {}</code>)を指定した場合、全てのレコードで単一のグループを構成する。
     * </p>
     */
    String[] group();

    /**
     * 順序付け条件のフィールド一覧。
     * <p>
     * 対象とする値のプロパティ名の一覧を指定すると、それらのプロパティごとにグループを構成する。
     * 複数のプロパティ名が指定された場合、それらの辞書式順序で整列される。
     * </p>
     * <p>
     * プロパティ名の直後に空白文字を挿入し、さらに文字列{@code ASC}を指定すると、
     * それらのプロパティの昇順に整列される。
     * </p>
     * <p>
     * 同様に、プロパティ名の直後に空白文字を挿入し、さらに文字列{@code DESC}を指定すると、
     * それらのプロパティの昇順に整列される。
     * </p>
     * <p>
     * 厳密には、それぞれの要素には下記のような言語{@code Order}に含まれる文字列を指定できる。
     * また、{@code ID}と以降の区切り文字はJavaにおける任意の空白文字とする。
     * </p>
     * <p><code>
     * Order:<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;ID<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;ID 'ASC'<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;ID 'DESC'<br/>
     * </code></p>
     * <p>
     * この要素を省略した場合や、空の配列を指定した場合には、値の順序は保証されなくなる。
     * </p>
     */
    String[] order() default { };
}
