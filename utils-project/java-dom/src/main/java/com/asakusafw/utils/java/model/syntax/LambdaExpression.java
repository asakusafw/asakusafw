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
 * An abstract super interface of lambda expressions.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS8:15.27] Lambda Expressions} </li>
 *   </ul> </li>
 * </ul>
 * @since 0.9.0
 */
public interface LambdaExpression extends Expression {

    /**
     * Returns the lambda parameters.
     * @return the lambda parameters
     */
    List<? extends LambdaParameter> getParameters();

    /**
     * Returns the lambda body.
     * @return the lambda body
     */
    LambdaBody getBody();
}
