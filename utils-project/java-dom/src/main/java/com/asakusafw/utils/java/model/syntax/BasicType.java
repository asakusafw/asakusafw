/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
 * 基本型を表現するインターフェース。
 * <p> 疑似型{@code void}もこのクラスで表現する </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:4.2] Primitive Types and Values} </li>
 *   </ul> </li>
 * </ul>
 */
public interface BasicType
        extends Type {

    // properties

    /**
     * 基本型の種類を返す。
     * @return
     *     基本型の種類
     */
    BasicTypeKind getTypeKind();
}
