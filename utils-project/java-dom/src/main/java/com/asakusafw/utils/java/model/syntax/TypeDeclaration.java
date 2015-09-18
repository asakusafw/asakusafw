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
 * An abstract super interface of type declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.6] Top Level Type Declarations} </li>
 *     <li> {@code [JLS3:8.5] Member Type Declaration} </li>
 *   </ul> </li>
 * </ul>
 */
public interface TypeDeclaration
        extends TypeBodyDeclaration, TypedElement {

    /**
     * Returns the simple type name.
     * @return the simple type name
     */
    SimpleName getName();

    /**
     * Returns type member declarations.
     * If this represents an enum type declaration,
     * the enum constant declarations will not be in the member declarations.
     * @return type member declarations
     */
    List<? extends TypeBodyDeclaration> getBodyDeclarations();
}
