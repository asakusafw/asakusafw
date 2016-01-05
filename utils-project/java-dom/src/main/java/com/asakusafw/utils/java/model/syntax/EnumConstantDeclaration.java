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

import java.util.List;

/**
 * An interface which represents enum constants.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.9] Enums (EnumConstant)} </li>
 *   </ul> </li>
 * </ul>
 */
public interface EnumConstantDeclaration
        extends TypeBodyDeclaration, Invocation {

    /**
     * Returns the enum constant name.
     * @return the enum constant name
     */
    SimpleName getName();

    /**
     * Returns the constructor arguments.
     * @return the constructor arguments
     */
    List<? extends Expression> getArguments();

    /**
     * Returns the class body.
     * @return the class body, or {@code null} if it is not specified
     */
    ClassBody getBody();
}
