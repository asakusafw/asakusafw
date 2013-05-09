/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * {@code synchronized}文を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.19] The synchronized Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface SynchronizedStatement
        extends Statement {

    // properties

    /**
     * 同期オブジェクトを返す。
     * @return
     *     同期オブジェクト
     */
    Expression getExpression();

    /**
     * 本体ブロックを返す。
     * @return
     *     本体ブロック
     */
    Block getBody();
}
