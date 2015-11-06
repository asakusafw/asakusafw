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

import java.util.List;

/**
 * An abstract super interface of method or constructor declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.8] Constructor Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface MethodOrConstructorDeclaration
        extends TypeBodyDeclaration {

    /**
     * Returns the type parameter declarations.
     * @return the type parameter declarations
     */
    List<? extends TypeParameterDeclaration> getTypeParameters();

    /**
     * Returns the target method or constructor name.
     * @return the target method or constructor name
     */
    SimpleName getName();

    /**
     * Returns the formal parameter declarations.
     * @return the formal parameter declarations
     */
    List<? extends FormalParameterDeclaration> getFormalParameters();

    /**
     * Returns the exception types.
     * @return the exception types
     */
    List<? extends Type> getExceptionTypes();

    /**
     * Returns the method or constructor body.
     * @return the method or constructor body, or {@code null} if it is not specified
     */
    Block getBody();
}
