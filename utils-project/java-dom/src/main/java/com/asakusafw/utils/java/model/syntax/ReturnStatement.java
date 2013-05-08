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
 * {@code return}文を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.17] The return Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ReturnStatement
        extends Statement, TypedElement {

    // properties

    /**
     * 返戻値を返す。
     * <p> 返戻値が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     返戻値、
     *     ただし返戻値が指定されない場合は{@code null}
     */
    Expression getExpression();
}
