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


/**
 * 名前によって指定される型を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:4.3] Reference Types and Values} </li>
 *     <li> {@code [JLS3:4.4] Type Variables} </li>
 *   </ul> </li>
 * </ul>
 */
public interface NamedType
        extends Type, DocElement {

    // properties

    /**
     * 型の名前を返す。
     * @return
     *     型の名前
     */
    Name getName();
}
