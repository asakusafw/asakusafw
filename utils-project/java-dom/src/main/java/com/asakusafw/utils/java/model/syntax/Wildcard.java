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
 * 型ワイルドカードを表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:4.5.1] Type Arguments and Wildcards} </li>
 *   </ul> </li>
 * </ul>
 */
public interface Wildcard
        extends Type {

    // properties

    /**
     * 型境界の種類を返す。
     * @return
     *     型境界の種類
     */
    WildcardBoundKind getBoundKind();

    /**
     * 境界型を返す。
     * <p> 境界型が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     境界型、
     *     ただし境界型が指定されない場合は{@code null}
     */
    Type getTypeBound();
}
