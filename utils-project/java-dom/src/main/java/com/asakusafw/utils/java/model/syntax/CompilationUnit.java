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
 * An interface which represents compilation units.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.3] Compilation Units} </li>
 *   </ul> </li>
 * </ul>
 */
public interface CompilationUnit
        extends Model {

    /**
     * Returns the package declaration.
     * @return the package declaration, or {@code null} if the compilation unit is on the default package
     */
    PackageDeclaration getPackageDeclaration();

    /**
     * Returns the import declarations.
     * @return the import declarations
     */
    List<? extends ImportDeclaration> getImportDeclarations();

    /**
     * Returns the type declarations.
     * @return the type declarations
     */
    List<? extends TypeDeclaration> getTypeDeclarations();

    /**
     * Returns the comments.
     * @return the comments
     */
    List<? extends Comment> getComments();
}
