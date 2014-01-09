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
package com.asakusafw.utils.java.model.syntax;


/**
 * リテラル式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.8.1] Lexical Literals} </li>
 *   </ul> </li>
 * </ul>
 */
public interface Literal
        extends Expression {

    // properties

    /**
     * このリテラルを構成する字句を返す。
     * <p>
     * 整数のリテラルは基本的に負の値を表現しないが、
     * {@link Integer#MIN_VALUE}と{@link Long#MIN_VALUE}だけは例外的に表現する。
     * この場合、文字列の先頭には{@code -}が含まれる。
     * </p>
     * @return
     *     このリテラルを構成する字句
     */
    String getToken();

    /**
     * このリテラルの種類を返す。
     * <p> リテラルの種類が不明である場合は{@code null}が返される。 </p>
     * @return
     *     このリテラルの種類、
     *     ただしリテラルの種類が不明である場合は{@code null}
     */
    LiteralKind getLiteralKind();
}
