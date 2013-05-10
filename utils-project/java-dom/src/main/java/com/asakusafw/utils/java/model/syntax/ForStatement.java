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
 * {@code for}文を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:14.14.1] The basic for Statement} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ForStatement
        extends Statement {

    // properties

    /**
     * ループ初期化部を返す。
     * <p> ループ初期化部が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     ループ初期化部、
     *     ただしループ初期化部が指定されない場合は{@code null}
     */
    ForInitializer getInitialization();

    /**
     * ループ条件式を返す。
     * <p> ループ条件が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     ループ条件式、
     *     ただしループ条件が指定されない場合は{@code null}
     */
    Expression getCondition();

    /**
     * ループ更新部を返す。
     * <p> ループ更新部が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     ループ更新部、
     *     ただしループ更新部が指定されない場合は{@code null}
     */
    StatementExpressionList getUpdate();

    /**
     * ループ本体を返す。
     * @return
     *     ループ本体
     */
    Statement getBody();
}
