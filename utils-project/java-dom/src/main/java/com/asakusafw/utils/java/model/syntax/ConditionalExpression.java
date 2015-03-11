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


/**
 * 三項演算式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.25] Conditional Operator ? :} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ConditionalExpression
        extends Expression {

    // properties

    /**
     * 条件式を返す。
     * @return
     *     条件式
     */
    Expression getCondition();

    /**
     * 条件成立時に評価される式を返す。
     * @return
     *     条件成立時に評価される式
     */
    Expression getThenExpression();

    /**
     * 条件不成立時に評価される式を返す。
     * @return
     *     条件不成立時に評価される式
     */
    Expression getElseExpression();
}
