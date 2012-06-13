/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.syntax;


/**
 * ドキュメンテーションコメント内のメソッドやコンストラクタの仮引数宣言を表現するインターフェース。
 */
public interface DocMethodParameter
        extends Model {

    // properties

    /**
     * 仮引数の型を返す。
     * @return
     *     仮引数の型
     */
    Type getType();

    /**
     * 仮引数の名前を返す。
     * <p> 仮引数の名前が省略される場合は{@code null}が返される。 </p>
     * @return
     *     仮引数の名前、
     *     ただし仮引数の名前が省略される場合は{@code null}
     */
    SimpleName getName();

    /**
     * 可変長引数である場合に{@code true}を返す。
     * <p> そうでない場合、この呼び出しは{@code false}を返す。 </p>
     * @return
     *     可変長引数
     */
    boolean isVariableArity();
}
