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
 * ブロックを表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.2] Blocks} </li>
 *   </ul> </li>
 * </ul>
 */
public interface Block
        extends Statement {

    // properties

    /**
     * 文の一覧を返す。
     * <p> 文が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     文の一覧
     */
    List<? extends Statement> getStatements();
}
