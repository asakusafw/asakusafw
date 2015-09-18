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

/**
 * An interface which represents catch clause.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.20] The try statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface CatchClause
        extends TypedElement {

    /**
     * Returns the exception parameter.
     * @return the exception parameter
     */
    FormalParameterDeclaration getParameter();

    /**
     * Returns the {@code catch} block body.
     * @return the {@code catch} block body
     */
    Block getBody();
}
