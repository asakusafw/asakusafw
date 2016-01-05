/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
 * An interface which represents assignment expression.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.26] Assignment Operators} </li>
 *   </ul> </li>
 * </ul>
 */
public interface AssignmentExpression
        extends Expression {

    /**
     * Returns the left hand side term.
     * @return the left hand side term
     */
    Expression getLeftHandSide();

    /**
     * Returns the simple assignment operator, or an infix operator for compound assignment expression.
     * @return the assignment or infix operator
     */
    InfixOperator getOperator();

    /**
     * Returns the right hand side term.
     * @return the right hand side term
     */
    Expression getRightHandSide();
}
