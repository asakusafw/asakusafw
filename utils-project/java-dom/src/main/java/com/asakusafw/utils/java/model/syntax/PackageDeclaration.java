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
 * パッケージ宣言を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.4] Package Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface PackageDeclaration
        extends Model {

    // properties

    /**
     * ドキュメンテーションコメントを返す。
     * <p> ドキュメンテーションコメントが存在しない場合は{@code null}が返される。 </p>
     * <p> なお、パッケージ宣言へのドキュメンテーションコメントは{@code package-info.java}のみに付与できるのが普通である。 </p>
     * @return
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     */
    Javadoc getJavadoc();

    /**
     * 注釈の一覧を返す。
     * <p> 注釈が存在しない場合は空が返される。 </p>
     * <p> なお、パッケージ宣言への注釈は{@code package-info.java}のみに付与できるのが普通である。 </p>
     * @return
     *     注釈の一覧
     */
    List<? extends Annotation> getAnnotations();

    /**
     * 宣言するパッケージの名称を返す。
     * @return
     *     宣言するパッケージの名称
     */
    Name getName();
}
