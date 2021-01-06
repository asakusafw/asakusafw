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
 * An interface which represents class instance creation expression.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.9] Class Instance Creation Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ClassInstanceCreationExpression
        extends Expression, Invocation {

    /**
     * Returns the qualifier expression.
     * @return the qualifier expression, or {@code null} if it is not specified
     */
    Expression getQualifier();

    /**
     * Returns the type arguments.
     * @return the type arguments
     */
    List<? extends Type> getTypeArguments();

    /**
     * Returns the target type.
     * @return the target type
     */
    Type getType();

    /**
     * Returns the actual arguments.
     * @return the actual arguments
     */
    List<? extends Expression> getArguments();

    /**
     * Returns the anonymous class body.
     * @return the anonymous class body, or {@code null} if the target class is not anonymous
     */
    ClassBody getBody();
}
