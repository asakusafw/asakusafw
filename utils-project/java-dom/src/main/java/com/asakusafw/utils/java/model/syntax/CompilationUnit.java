/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * コンパイル単位を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.3] Compilation Units} </li>
 *   </ul> </li>
 * </ul>
 */
public interface CompilationUnit
        extends Model {

    // properties

    /**
     * パッケージ宣言を返す。
     * <p> 無名パッケージ上に存在するコンパイル単位を表現する場合は{@code null}が返される。 </p>
     * @return
     *     パッケージ宣言、
     *     ただし無名パッケージ上に存在するコンパイル単位を表現する場合は{@code null}
     */
    PackageDeclaration getPackageDeclaration();

    /**
     * このコンパイル単位で宣言されるインポート宣言の一覧を返す。
     * <p> インポート宣言が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     このコンパイル単位で宣言されるインポート宣言の一覧
     */
    List<? extends ImportDeclaration> getImportDeclarations();

    /**
     * このコンパイル単位で宣言される型の一覧を返す。
     * <p> 型が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     このコンパイル単位で宣言される型の一覧
     */
    List<? extends TypeDeclaration> getTypeDeclarations();

    /**
     * このコンパイル単位に記述されたコメントの一覧を返す。
     * <p> コメントが一つも記述されない場合は空が返される。 </p>
     * @return
     *     このコンパイル単位に記述されたコメントの一覧
     */
    List<? extends Comment> getComments();
}
