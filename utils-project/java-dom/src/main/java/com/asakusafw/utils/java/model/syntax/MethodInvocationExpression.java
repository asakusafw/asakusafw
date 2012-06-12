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
 * メソッド起動式を表現するインターフェース。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:15.12] Method Invocation Expressions} </li>
 *   </ul> </li>
 * </ul>
 */
public interface MethodInvocationExpression
        extends Expression, Invocation {

    // properties

    /**
     * 限定式、または型限定子を返す。
     * <p> 限定式が指定されない場合(単純メソッド起動)は{@code null}が返される。 </p>
     * <p> 型限定子を指定する場合、名前で対象型を表現する。 </p>
     * <p> 親メソッド呼び出しを表現する場合、{@code super}キーワードを表現する疑似式を利用する。 </p>
     * @return
     *     限定式、または型限定子、
     *     ただし限定式が指定されない場合(単純メソッド起動)は{@code null}
     * @see Name
     * @see Super
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
     * メソッドの名前を返す。
     * @return
     *     メソッドの名前
     */
    SimpleName getName();

    /**
     * 実引数の一覧を返す。
     * <p> 実引数が一つも指定されない場合は空が返される。 </p>
     * @return
     *     実引数の一覧
     */
    List<? extends Expression> getArguments();
}
