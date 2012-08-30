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
 * 代入式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.26] Assignment Operators} </li>
 *   </ul> </li>
 * </ul>
 */
public interface AssignmentExpression
        extends Expression {

    // properties

    /**
     * 左辺式を返す。
     * @return
     *     左辺式
     */
    Expression getLeftHandSide();

    /**
     * 単純代入演算子、または複合する演算子を返す。
     * @return
     *     単純代入演算子、または複合する演算子
     */
    InfixOperator getOperator();

    /**
     * 右辺式を返す。
     * @return
     *     右辺式
     */
    Expression getRightHandSide();
}
