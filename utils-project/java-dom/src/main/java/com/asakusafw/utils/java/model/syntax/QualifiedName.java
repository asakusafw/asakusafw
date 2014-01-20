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


/**
 * 限定名を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:6.2] Names and Identifiers} </li>
 *   </ul> </li>
 * </ul>
 */
public interface QualifiedName
        extends Name {

    // properties

    /**
     * 限定子を返す。
     * @return
     *     限定子
     */
    Name getQualifier();

    /**
     * この限定名の末尾にある単純名を返す。
     * @return
     *     この限定名の末尾にある単純名
     */
    SimpleName getSimpleName();
}
