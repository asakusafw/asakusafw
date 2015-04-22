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
 * メソッドの宣言を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.4] Method Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface MethodDeclaration
        extends MethodOrConstructorDeclaration, TypedElement {

    // properties

    /**
     * 戻り値の型を返す。
     * @return
     *     戻り値の型
     */
    Type getReturnType();

    /**
     * 戻り値の次元数を返す。
     * <p> この要素は古いJavaとの後方互換性のために用意されている。現在使用することは推奨されていない。 </p>
     * @return
     *     戻り値の次元数
     */
    int getExtraDimensions();
}
