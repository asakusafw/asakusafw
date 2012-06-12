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
 * 明示的コンストラクタ起動文を表現する基底インターフェース。
 * <p> この要素は、コンストラクタ宣言本体の1つ目の文としてのみ出現できる。 </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.8.7.1] Explicit Constructor Invocations} </li>
 *   </ul> </li>
 * </ul>
 */
public interface ConstructorInvocation
        extends Statement, Invocation {

    // properties

    /**
     * 型引数の一覧を返す。
     * <p> 型引数を一つも指定しない場合は空が返される。 </p>
     * @return
     *     型引数の一覧
     */
    List<? extends Type> getTypeArguments();

    /**
     * 実引数の一覧を返す。
     * <p> 実引数を一つも指定しない場合は空が返される。 </p>
     * @return
     *     実引数の一覧
     */
    List<? extends Expression> getArguments();
}
