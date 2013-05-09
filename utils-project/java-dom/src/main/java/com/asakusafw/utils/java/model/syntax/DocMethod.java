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

import java.util.List;

/**
 * ドキュメンテーションコメント内のメソッドやコンストラクタを表現するインターフェース。
 */
public interface DocMethod
        extends DocElement {

    // properties

    /**
     * メソッドまたはコンストラクタの宣言型を返す。
     * <p> 宣言型が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     メソッドまたはコンストラクタの宣言型、
     *     ただし宣言型が指定されない場合は{@code null}
     */
    Type getType();

    /**
     * メソッドまたはコンストラクタの名前を返す。
     * @return
     *     メソッドまたはコンストラクタの名前
     */
    SimpleName getName();

    /**
     * メソッドまたはコンストラクタの仮引数宣言の一覧を返す。
     * <p> 仮引数が一つも宣言されない場合は空が返される。 </p>
     * @return
     *     メソッドまたはコンストラクタの仮引数宣言の一覧
     */
    List<? extends DocMethodParameter> getFormalParameters();
}
