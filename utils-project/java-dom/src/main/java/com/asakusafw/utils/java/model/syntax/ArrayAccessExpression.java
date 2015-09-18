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
 * An interface which represents array access expression.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.13] Array Access Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ArrayAccessExpression
        extends Expression {

    /**
     * Returns the array expression.
     * @return the array expression
     */
    Expression getArray();

    /**
     * Returns the index expression.
     * @return the index expression
     */
    Expression getIndex();
}
