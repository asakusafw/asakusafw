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
 * 限定型を表現するインターフェース。
 * <p> 限定名のみで表現できる型は名前型で表現することができる。 </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:4.3] Reference Types and Values} </li>
 *   </ul> </li>
 * </ul>
 * @see NamedType
 */
public interface QualifiedType
        extends Type {

    // properties

    /**
     * 型限定子を返す。
     * @return
     *     型限定子
     */
    Type getQualifier();

    /**
     * 型の単純名を返す。
     * @return
     *     型の単純名
     */
    SimpleName getSimpleName();
}
