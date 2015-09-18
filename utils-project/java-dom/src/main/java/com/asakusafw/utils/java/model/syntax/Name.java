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

import java.util.List;

/**
 * An abstract super interface of names.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:6.2] Names and Identifiers} </li>
 *     <li> {@code [JLS3:6.5.6] Meaning of Expression Names} </li>
 *   </ul> </li>
 * </ul>
 * @see FieldAccessExpression
 */
public interface Name
        extends Expression, DocElement {

    /**
     * Returns the simple name of this name.
     * If this already represents a simple name, this returns the name itself.
     * @return the simple name
     */
    SimpleName getLastSegment();

    /**
     * Returns a simple name list of this name.
     * @return a simple name list of this name
     */
    List<SimpleName> toNameList();

    /**
     * Returns the string representation of this name.
     * @return the string representation of this name
     */
    String toNameString();
}
