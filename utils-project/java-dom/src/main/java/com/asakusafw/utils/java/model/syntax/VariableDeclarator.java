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
 * An interface which represents variable declarators.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.3] Field Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface VariableDeclarator
        extends TypedElement {

    /**
     * Returns the variable name.
     * @return the variable name
     */
    SimpleName getName();

    /**
     * Returns the extra variable dimensions.
     * @return the extra variable dimensions
     */
    int getExtraDimensions();

    /**
     * Returns the variable initializer expression.
     * @return the variable initializer expression, or {@code null} if there is not specified
     */
    Expression getInitializer();
}
