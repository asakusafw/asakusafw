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
 * 二項演算式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.17] Multiplicative Operators} </li>
 *     <li> {@code [JLS3:15.18] Additive Operators} </li>
 *     <li> {@code [JLS3:15.19] Shift Operators} </li>
 *     <li> {@code [JLS3:15.20.1] Numerical Comparison Operators <, <=, >, and >=} </li>
 *     <li> {@code [JLS3:15.21] Equality Operators} </li>
 *     <li> {@code [JLS3:15.22] Bitwise and Logical Operators} </li>
 *     <li> {@code [JLS3:15.23] Conditional-And Operator &&} </li>
 *     <li> {@code [JLS3:15.24] Conditional-Or Operator ||} </li>
 *   </ul> </li>
 * </ul>
 * @see InstanceofExpression
 */
public interface InfixExpression
        extends Expression {

    // properties

    /**
     * 第一演算項を返す。
     * @return
     *     第一演算項
     */
    Expression getLeftOperand();

    /**
     * 二項演算子を返す。
     * @return
     *     二項演算子
     */
    InfixOperator getOperator();

    /**
     * 第二演算項を返す。
     * @return
     *     第二演算項
     */
    Expression getRightOperand();
}
