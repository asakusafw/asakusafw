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
 * An interface which represents method invocation expressions.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.12] Method Invocation Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface MethodInvocationExpression
        extends Expression, Invocation {

    /**
     * Returns the qualifier expression or type.
     * If this represents a type, the expression must be a {@link Name}.
     * @return the qualifier expression or type, or {@code null} if this represents a simple method invocation
     * @see Name
     * @see Super
     */
    Expression getQualifier();

    /**
     * Returns the type arguments.
     * @return the type arguments
     */
    List<? extends Type> getTypeArguments();

    /**
     * Returns the target method name.
     * @return the target method name
     */
    SimpleName getName();

    /**
     * Returns the actual arguments.
     * @return the actual arguments
     */
    List<? extends Expression> getArguments();
}
