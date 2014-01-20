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
package com.asakusafw.utils.java.parser.javadoc;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;

/**
 * {@link JavadocToken}のストリーム。
 */
public interface JavadocTokenStream {

    /**
     * 次のトークンを消費し、返す。
     * @return 次のトークン
     */
    JavadocToken nextToken();

    /**
     * 次のトークンを返す。
     * @return 次のトークン
     */
    JavadocToken peek();

    /**
     * 厳密に次のトークンを調べて返す。
     * @param k スキップする数
     * @return 次のトークン
     */
    JavadocToken lookahead(int k);

    /**
     * 現在の位置をにマークをつけ、{@link #rewind()}によってこの位置に戻れるようにする。
     */
    void mark();

    /**
     * 直前に{@link #mark()}を呼び出した位置に戻り、マークを破棄する。
     * @throws IllegalStateException 直前の{@link #mark()}が存在しない場合
     */
    void rewind();

    /**
     * 直前の{@link #mark()}を破棄する。
     * @throws IllegalStateException 直前の{@link #mark()}が存在しない場合
     */
    void discard();
}
