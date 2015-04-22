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
 * 親コンストラクタ起動文を表現するインターフェース。
 * <p> この要素は、コンストラクタ宣言本体の1つ目の文としてのみ出現できる。 </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.8.7.1] Explicit Constructor Invocations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface SuperConstructorInvocation
        extends ConstructorInvocation {

    // properties

    /**
     * 限定式を返す。
     * <p> 限定式が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     限定式、
     *     ただし限定式が指定されない場合は{@code null}
     */
    Expression getQualifier();
}
