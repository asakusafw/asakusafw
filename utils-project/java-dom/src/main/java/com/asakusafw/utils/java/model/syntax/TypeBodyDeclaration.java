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
 * メンバの宣言を表現する基底インターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.1.6] Class Body and Member Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface TypeBodyDeclaration
        extends Model {

    // properties

    /**
     * ドキュメンテーションコメントを返す。
     * <p> ドキュメンテーションコメントが存在しない場合は{@code null}が返される。 </p>
     * @return
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     */
    Javadoc getJavadoc();

    /**
     * 修飾子および注釈の一覧を返す。
     * <p> 修飾子または注釈が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     修飾子および注釈の一覧
     */
    List<? extends Attribute> getModifiers();
}
