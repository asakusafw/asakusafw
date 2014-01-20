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

import java.util.List;

/**
 * フィールド宣言(の一覧)を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.3] Field Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface FieldDeclaration
        extends TypeBodyDeclaration {

    // properties

    /**
     * フィールドの型を返す。
     * @return
     *     フィールドの型
     */
    Type getType();

    /**
     * 宣言するフィールドの一覧を返す。
     * @return
     *     宣言するフィールドの一覧
     */
    List<? extends VariableDeclarator> getVariableDeclarators();
}
