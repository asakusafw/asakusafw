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
 * {@code instanceof}式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.20.2] Type Comparison Operator instanceof} </li>
 *   </ul> </li>
 * </ul>
 */
public interface InstanceofExpression
        extends Expression {

    // properties

    /**
     * 被演算項を返す。
     * @return
     *     被演算項
     */
    Expression getExpression();

    /**
     * 比較対象型を返す。
     * @return
     *     比較対象型
     */
    Type getType();
}
