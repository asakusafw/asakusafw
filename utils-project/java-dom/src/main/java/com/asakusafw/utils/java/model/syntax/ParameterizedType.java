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

import java.util.List;

/**
 * An interface which represents parameterized types.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:4.5] Parameterized Types} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ParameterizedType
        extends Type {

    /**
     * Returns the non-parameterized type.
     * @return the non-parameterized type
     */
    Type getType();

    /**
     * Returns the type arguments.
     * @return the type arguments
     */
    List<? extends Type> getTypeArguments();
}
