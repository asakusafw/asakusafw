/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * An interface which represents enhanced for statement.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.14.2] The enhanced for statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface EnhancedForStatement
        extends Statement {

    /**
     * Returns the loop variable declaration.
     * @return the loop variable declaration
     */
    FormalParameterDeclaration getParameter();

    /**
     * Returns the loop target expression.
     * @return the loop target expression
     */
    Expression getExpression();

    /**
     * Returns the loop body.
     * @return the loop body
     */
    Statement getBody();
}
