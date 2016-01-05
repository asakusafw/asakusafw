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
 * An interface which represents field access expressions.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.11] Field Access Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface FieldAccessExpression
        extends Expression {

    /**
     * Returns the qualifier expression.
     * If a field access expression can be represented in just a qualified name, it must be {@link QualifiedName}.
     * So that, the qualifier must not be a kind of {@link Name}.
     * @return the qualifier expression
     * @see Name
     * @see Super
     */
    Expression getQualifier();

    /**
     * Returns the field name.
     * @return the field name
     */
    SimpleName getName();
}
