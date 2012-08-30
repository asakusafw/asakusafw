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
 * {@code try}文を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.20] The try statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface TryStatement
        extends Statement {

    // properties

    /**
     * {@code try}節を返す。
     * @return
     *     {@code try}節
     */
    Block getTryBlock();

    /**
     * {@code catch}節の一覧を返す。
     * <p> {@code catch}節が一つも指定されない場合は空が返される。 </p>
     * @return
     *     {@code catch}節の一覧
     */
    List<? extends CatchClause> getCatchClauses();

    /**
     * {@code finally}節を返す。
     * <p> {@code finally}節が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     {@code finally}節、
     *     ただし{@code finally}節が指定されない場合は{@code null}
     */
    Block getFinallyBlock();
}
