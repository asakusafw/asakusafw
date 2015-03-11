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
package com.asakusafw.utils.java.model.syntax;

import java.util.List;

/**
 * 仮引数の宣言を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.4.1] Formal Parameters} </li>
 *   </ul> </li>
 * </ul>
 */
public interface FormalParameterDeclaration
        extends TypedElement {

    // properties

    /**
     * 修飾子および注釈の一覧を返す。
     * <p> 修飾子または注釈が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     修飾子および注釈の一覧
     */
    List<? extends Attribute> getModifiers();

    /**
     * 宣言する変数の型を返す。
     * @return
     *     宣言する変数の型
     */
    Type getType();

    /**
     * 可変長引数である場合に{@code true}を返す。
     * <p> そうでない場合、この呼び出しは{@code false}を返す。 </p>
     * @return
     *     可変長引数
     */
    boolean isVariableArity();

    /**
     * 仮引数の名前を返す。
     * @return
     *     仮引数の名前
     */
    SimpleName getName();

    /**
     * 追加次元数の宣言を返す。
     * @return
     *     追加次元数の宣言
     */
    int getExtraDimensions();
}
