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
package com.asakusafw.utils.java.model.syntax;


/**
 * 一つ分の変数宣言を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.3] Field Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface VariableDeclarator
        extends TypedElement {

    // properties

    /**
     * 変数の名前を返す。
     * @return
     *     変数の名前
     */
    SimpleName getName();

    /**
     * 追加次元数の宣言を返す。
     * @return
     *     追加次元数の宣言
     */
    int getExtraDimensions();

    /**
     * 初期化式を返す。
     * <p> 初期化式が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     初期化式、
     *     ただし初期化式が指定されない場合は{@code null}
     */
    Expression getInitializer();
}
