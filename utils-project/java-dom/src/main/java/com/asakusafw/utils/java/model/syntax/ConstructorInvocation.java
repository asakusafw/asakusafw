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
 * An abstract super interface of constructor invocations.
 * This can appear the top of constructor declaration bodies as a statement.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.8.7.1] Explicit Constructor Invocations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ConstructorInvocation
        extends Statement, Invocation {

    /**
     * Returns the type arguments.
     * @return the type arguments
     */
    List<? extends Type> getTypeArguments();

    /**
     * Returns the actual arguments.
     * @return the actual arguments
     */
    List<? extends Expression> getArguments();
}
