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
 * クラスの宣言を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.1] Class Declaration} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ClassDeclaration
        extends TypeDeclaration {

    // properties

    /**
     * 仮型引数宣言の一覧を返す。
     * <p> 仮型引数が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     仮型引数宣言の一覧
     */
    List<? extends TypeParameterDeclaration> getTypeParameters();

    /**
     * 親クラスを返す。
     * <p> 親クラスが明示されない場合は{@code null}が返される。 </p>
     * @return
     *     親クラス、
     *     ただし親クラスが明示されない場合は{@code null}
     */
    Type getSuperClass();

    /**
     * 親インターフェースの一覧を返す。
     * <p> 親インターフェースが一つも宣言されない場合は空が返される。 </p>
     * @return
     *     親インターフェースの一覧
     */
    List<? extends Type> getSuperInterfaceTypes();
}
