/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
 * 型の宣言を表現する基底インターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.6] Top Level Type Declarations} </li>
 *     <li> {@code [JLS3:8.5] Member Type Declaration} </li>
 *   </ul> </li>
 * </ul>
 */
public interface TypeDeclaration
        extends TypeBodyDeclaration, TypedElement {

    // properties

    /**
     * 型の単純名を返す。
     * @return
     *     型の単純名
     */
    SimpleName getName();

    /**
     * メンバの一覧を返す。
     * <p> メンバが一つも宣言されない場合は空が返される。 </p>
     * <p> この型が列挙型である場合、列挙定数は返されるリストに含まれない </p>
     * @return
     *     メンバの一覧
     */
    List<? extends TypeBodyDeclaration> getBodyDeclarations();
}
