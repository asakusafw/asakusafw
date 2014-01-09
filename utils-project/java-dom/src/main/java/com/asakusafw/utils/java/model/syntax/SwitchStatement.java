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

import java.util.List;

/**
 * {@code switch}文を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.11] The switch Statement} </li>
 *   </ul> </li>
 * </ul>
 * @see SwitchLabel
 */
public interface SwitchStatement
        extends Statement, TypedElement {

    // properties

    /**
     * セレクタ式を返す。
     * @return
     *     セレクタ式
     */
    Expression getExpression();

    /**
     * {@code switch}文の本体を返す。
     * <p> 本体に一つもラベルが指定されない場合は空が返される。 </p>
     * <p> {@code switch}文の本体中にのみ、{@code case}, {@code default}ラベルを含めることができる。 </p>
     * @return
     *     {@code switch}文の本体
     */
    List<? extends Statement> getStatements();
}
