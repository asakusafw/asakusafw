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
 * 分岐する文を表現する基底インターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.15] The break Statement} </li>
 *     <li> {@code [JLS3:14.16] The continue Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface BranchStatement
        extends Statement {

    // properties

    /**
     * 分岐先ラベルを返す。
     * <p> 分岐先ラベルが指定されない場合は{@code null}が返される。 </p>
     * @return
     *     分岐先ラベル、
     *     ただし分岐先ラベルが指定されない場合は{@code null}
     */
    SimpleName getTarget();
}
