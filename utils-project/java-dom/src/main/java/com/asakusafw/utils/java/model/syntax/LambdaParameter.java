/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * An abstract super interface of lambda parameters.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS8:15.27.1] Lambda Parameters} </li>
 *   </ul> </li>
 * </ul>
 * @since 0.9.0
 */
public interface LambdaParameter
        extends Model {

    /**
     * Returns the parameter name.
     * @return the parameter name
     */
    SimpleName getName();
}
