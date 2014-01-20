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
 * ドキュメンテーションコメント内の概要ブロック、タグ付きブロック、インラインブロックを表現するインターフェース。
 */
public interface DocBlock
        extends DocElement {

    // properties

    /**
     * タグ文字列を返す。
     * <p> タグが省略された場合は空が返される。 </p>
     * @return
     *     タグ文字列
     */
    String getTag();

    /**
     * インライン要素の一覧を返す。
     * <p> インライン要素が一つも指定されない場合は空が返される。 </p>
     * @return
     *     インライン要素の一覧
     */
    List<? extends DocElement> getElements();
}
