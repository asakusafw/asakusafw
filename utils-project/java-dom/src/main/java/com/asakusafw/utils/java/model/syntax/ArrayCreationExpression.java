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
 * 配列生成式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.10] Array Creation Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ArrayCreationExpression
        extends Expression {

    // properties

    /**
     * 生成する配列の型を返す。
     * @return
     *     生成する配列の型
     */
    ArrayType getType();

    /**
     * 要素数指定式を返す。
     * <p> 次元ごとの要素数が一つも指定されない場合は空が返される。 </p>
     * @return
     *     要素数指定式
     */
    List<? extends Expression> getDimensionExpressions();

    /**
     * 配列初期化子を返す。
     * <p> 配列初期化子が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     配列初期化子、
     *     ただし配列初期化子が指定されない場合は{@code null}
     */
    ArrayInitializer getArrayInitializer();
}
