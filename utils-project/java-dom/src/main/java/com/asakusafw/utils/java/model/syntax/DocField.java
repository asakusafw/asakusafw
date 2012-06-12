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


/**
 * ドキュメンテーションコメント内のフィールドを表現するインターフェース。
 */
public interface DocField
        extends DocElement {

    // properties

    /**
     * フィールドを宣言した型を返す。
     * <p> 宣言型が指定されない場合は{@code null}が返される。 </p>
     * @return
     *     フィールドを宣言した型、
     *     ただし宣言型が指定されない場合は{@code null}
     */
    Type getType();

    /**
     * フィールドの名称を返す。
     * @return
     *     フィールドの名称
     */
    SimpleName getName();
}
