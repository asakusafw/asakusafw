/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * An interface which represents conditional expression.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.25] Conditional Operator ? :} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ConditionalExpression
        extends Expression {

    /**
     * Returns the condition term.
     * @return the condition term
     */
    Expression getCondition();

    /**
     * Returns the truth term.
     * @return the truth term
     */
    Expression getThenExpression();

    /**
     * Returns the false term.
     * @return the false term
     */
    Expression getElseExpression();
}
