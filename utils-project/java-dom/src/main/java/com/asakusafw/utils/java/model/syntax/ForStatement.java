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
 * An interface which represents for statement.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.14.1] The basic for Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ForStatement
        extends Statement {

    /**
     * Returns the loop initialization part.
     * @return the loop initialization part, or {@code null} if it is not specified
     */
    ForInitializer getInitialization();

    /**
     * Returns the loop condition expression.
     * @return the loop condition expression, or if it is not specified
     */
    Expression getCondition();

    /**
     * Returns the loop update part.
     * @return the loop update part, or {@code null} if it is not specified
     */
    StatementExpressionList getUpdate();

    /**
     * Returns the loop body.
     * @return the loop body
     */
    Statement getBody();
}
