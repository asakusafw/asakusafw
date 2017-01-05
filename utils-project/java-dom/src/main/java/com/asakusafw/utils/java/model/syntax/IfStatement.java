/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * An interface which represents if statement.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.9] The if Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface IfStatement
        extends Statement {

    /**
     * Returns the condition expression.
     * @return the condition expression
     */
    Expression getCondition();

    /**
     * Returns the truth statement.
     * @return the truth statement
     */
    Statement getThenStatement();

    /**
     * Returns the false statement.
     * @return the false statement, or {@code null} if it is not specified
     */
    Statement getElseStatement();
}
