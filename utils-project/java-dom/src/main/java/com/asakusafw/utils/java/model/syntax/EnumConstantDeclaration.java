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
 * 列挙定数の宣言を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.9] Enums (<i>EnumConstant</i>)} </li>
 *   </ul> </li>
 * </ul>
 */
public interface EnumConstantDeclaration
        extends TypeBodyDeclaration, TypedElement, Invocation {

    // properties

    /**
     * 列挙定数の名前を返す。
     * @return
     *     列挙定数の名前
     */
    SimpleName getName();

    /**
     * コンストラクタ引数の一覧を返す。
     * <p> コンストラクタ引数が指定されない場合は空が返される。 </p>
     * @return
     *     コンストラクタ引数の一覧
     */
    List<? extends Expression> getArguments();

    /**
     * クラス本体の宣言を返す。
     * <p> クラスの本体が宣言されない場合は{@code null}が返される。 </p>
     * @return
     *     クラス本体の宣言、
     *     ただしクラスの本体が宣言されない場合は{@code null}
     */
    ClassBody getBody();
}
