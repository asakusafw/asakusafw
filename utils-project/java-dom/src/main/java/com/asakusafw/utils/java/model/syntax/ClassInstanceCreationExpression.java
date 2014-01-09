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
 * クラスインスタンス生成式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.9] Class Instance Creation Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ClassInstanceCreationExpression
        extends Expression, Invocation {

    // properties

    /**
     * 限定式を返す。
     * <p> 限定式が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     限定式、
     *     ただし限定式が指定されない場合は{@code null}
     */
    Expression getQualifier();

    /**
     * 型引数の一覧を返す。
     * <p> 型引数が一つも指定されない場合は空が返される。 </p>
     * @return
     *     型引数の一覧
     */
    List<? extends Type> getTypeArguments();

    /**
     * インスタンスを生成する型を返す。
     * @return
     *     インスタンスを生成する型
     */
    Type getType();

    /**
     * 実引数の一覧を返す。
     * <p> 実引数が一つも指定されない場合は空が返される。 </p>
     * @return
     *     実引数の一覧
     */
    List<? extends Expression> getArguments();

    /**
     * 匿名クラス本体を返す。
     * <p> 匿名クラス本体が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     匿名クラス本体、
     *     ただし匿名クラス本体が指定されない場合は{@code null}
     */
    ClassBody getBody();
}
