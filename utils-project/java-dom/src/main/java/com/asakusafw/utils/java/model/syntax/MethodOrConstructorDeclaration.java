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
 * メソッドまたはコンストラクタの宣言を表現する基底インターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.8] Constructor Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface MethodOrConstructorDeclaration
        extends TypeBodyDeclaration {

    // properties

    /**
     * 型引数宣言の一覧を返す。
     * <p> 型引数が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     型引数宣言の一覧
     */
    List<? extends TypeParameterDeclaration> getTypeParameters();

    /**
     * メソッドまたはコンストラクタの名前を返す。
     * @return
     *     メソッドまたはコンストラクタの名前
     */
    SimpleName getName();

    /**
     * 仮引数宣言の一覧を返す。
     * <p> 仮引数が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     仮引数宣言の一覧
     */
    List<? extends FormalParameterDeclaration> getFormalParameters();

    /**
     * 例外型宣言の一覧を返す。
     * <p> 例外型が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     例外型宣言の一覧
     */
    List<? extends Type> getExceptionTypes();

    /**
     * メソッドまたはコンストラクタ本体を返す。
     * <p> このメソッドが本体を提供されない抽象メソッドやインターフェースメソッドである場合は{@code null}が返される。 </p>
     * @return
     *     メソッドまたはコンストラクタ本体、
     *     ただしこのメソッドが本体を提供されない抽象メソッドやインターフェースメソッドである場合は{@code null}
     */
    Block getBody();
}
