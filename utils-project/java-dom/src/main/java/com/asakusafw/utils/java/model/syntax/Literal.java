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

/**
 * An interface which represents literals.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.8.1] Lexical Literals} </li>
 *   </ul> </li>
 * </ul>
 */
public interface Literal
        extends Expression {

    /**
     * Returns the literal token.
     * Literals do not represent negative value, except {@link Integer#MIN_VALUE} and {@link Long#MIN_VALUE}.
     * In those exceptional cases, tokens start with a {@code -} character.
     * @return the literal token
     */
    String getToken();

    /**
     * Returns the literal kind.
     * @return the literal kind
     */
    LiteralKind getLiteralKind();
}
