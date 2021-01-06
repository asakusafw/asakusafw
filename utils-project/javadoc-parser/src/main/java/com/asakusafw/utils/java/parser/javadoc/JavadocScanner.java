/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
 * An abstract super interface of scanners which provides a sequence of {@link JavadocToken}.
 */
public interface JavadocScanner {

    /**
     * Returns the tokens.
     * @return the tokens
     */
    List<JavadocToken> getTokens();

    /**
     * Consumes the tokens.
     * @param count the number of tokens to be consumed
     */
    void consume(int count);

    /**
     * Consumes the next token and returns it.
     * @return the next token
     */
    JavadocToken nextToken();

    /**
     * Looks ahead the token after the offset.
     * The next token's offset is {@code 0}.
     * @param offset the token offset
     * @return the look-ahead token
     */
    JavadocToken lookahead(int offset);

    /**
     * Returns {@link #nextToken() the next token} index.
     * @return the next token index
     */
    int getIndex();

    /**
     * Sets the index of the current scanner position.
     * @param position the index which indicates {@link #nextToken() the next token}
     */
    void seek(int position);
}
