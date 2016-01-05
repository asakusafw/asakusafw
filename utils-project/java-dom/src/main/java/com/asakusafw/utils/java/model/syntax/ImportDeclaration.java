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
 * An interface which represents import declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.5] Import Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ImportDeclaration
        extends Model {

    /**
     * Returns the import kind.
     * @return the import kind
     */
    ImportKind getImportKind();

    /**
     * Returns the import target type or member name.
     * @return the import target type or member name
     */
    Name getName();
}
