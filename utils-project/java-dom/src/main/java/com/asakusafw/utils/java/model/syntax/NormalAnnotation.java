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

import java.util.List;

/**
 * 通常の注釈を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:9.7] Annotations (<i>NormalAnnotation</i>)} </li>
 *   </ul> </li>
 * </ul>
 */
public interface NormalAnnotation
        extends Annotation {

    // properties

    /**
     * 注釈要素の一覧を返す。
     * <p> 注釈要素が一つも指定されない場合は空が返される。 </p>
     * @return
     *     注釈要素の一覧
     */
    List<? extends AnnotationElement> getElements();
}
