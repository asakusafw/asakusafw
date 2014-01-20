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
 * {@code switch}文に含まれる{@code case}ラベルを表現するインターフェース。
 * <p> この要素は、{@code switch}文の本体にのみ含めることができる </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.11] The switch Statement (<i>SwitchLabel</i>)} </li>
 *   </ul> </li>
 * </ul>
 * @see SwitchStatement
 */
public interface SwitchCaseLabel
        extends SwitchLabel {

    // properties

    /**
     * {@code case}ラベルの値を返す。
     * @return
     *     {@code case}ラベルの値
     */
    Expression getExpression();
}
