/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.util.List;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;

/**
 * {@link JavadocToken}を取り扱うスキャナ。
 */
public interface JavadocScanner {

    /**
     * トークンの一覧を返す。
     * @return トークンの一覧
     */
    List<JavadocToken> getTokens();

    /**
     * 指定の個数だけトークンを読み捨てる。
     * @param count 読み捨てる個数
     */
    void consume(int count);

    /**
     * 次のトークンを返す。
     * @return 次のトークン
     */
    JavadocToken nextToken();

    /**
     * 指定の位置から{@code offset}個先のトークンを返す。
     * @param offset オフセット
     * @return {@code offset}個先のトークン
     */
    JavadocToken lookahead(int offset);

    /**
     * 次に{@link #nextToken()}が返すトークンの位置を返す。
     * @return 次のトークンの位置
     */
    int getIndex();

    /**
     * 次に{@link #nextToken()}が返すトークンの位置を設定する。
     * @param position 次のトークンの位置
     */
    void seek(int position);
}
