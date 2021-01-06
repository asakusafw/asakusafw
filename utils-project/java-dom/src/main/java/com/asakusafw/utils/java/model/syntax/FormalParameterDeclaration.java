/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.util.List;

/**
 * An interface which represents formal parameter declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.4.1] Formal Parameters} </li>
 *   </ul> </li>
 * </ul>
 */
public interface FormalParameterDeclaration
        extends TypedElement, LambdaParameter {

    /**
     * Returns the modifiers and annotations.
     * @return the modifiers and annotations
     */
    List<? extends Attribute> getModifiers();

    /**
     * Returns the variable type.
     * @return the variable type
     */
    Type getType();

    /**
     * Returns whether the parameter is variable arity or not.
     * @return {@code true} if the parameter is variable arity, otherwise {@code false}
     */
    boolean isVariableArity();

    /**
     * Returns the parameter name.
     * @return the parameter name
     */
    @Override
    SimpleName getName();

    /**
     * Returns the extra variable dimensions.
     * @return the extra variable dimensions
     */
    int getExtraDimensions();
}
