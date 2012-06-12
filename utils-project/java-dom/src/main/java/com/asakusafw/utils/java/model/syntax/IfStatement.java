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
 * {@code if}文を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.9] The if Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface IfStatement
        extends Statement {

    // properties

    /**
     * 条件式を返す。
     * @return
     *     条件式
     */
    Expression getCondition();

    /**
     * 条件成立時に実行される文を返す。
     * @return
     *     条件成立時に実行される文
     */
    Statement getThenStatement();

    /**
     * 条件不成立時に実行される文を返す。
     * <p> この文が{@code if-then}文である場合は{@code null}が返される。 </p>
     * @return
     *     条件不成立時に実行される文、
     *     ただしこの文が{@code if-then}文である場合は{@code null}
     */
    Statement getElseStatement();
}
