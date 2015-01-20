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
 * フィールド参照式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.11] Field Access Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface FieldAccessExpression
        extends Expression {

    // properties

    /**
     * 限定式を返す。
     * <p> フィールド参照式が名前のみによって表現可能な場合、名前式を利用しなければならない。つまり、この値は名前式であってはならない。 </p>
     * <p> 親フィールドの参照式を表現する場合、{@code super}キーワードを表現する疑似式を利用する。 </p>
     * @return
     *     限定式
     * @see Name
     * @see Super
     */
    Expression getQualifier();

    /**
     * フィールドの名前を返す。
     * @return
     *     フィールドの名前
     */
    SimpleName getName();
}
