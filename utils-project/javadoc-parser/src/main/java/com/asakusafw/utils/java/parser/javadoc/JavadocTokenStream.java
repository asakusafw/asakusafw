/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * Provides a stream of {@link JavadocToken}s.
 * @see JavadocScanner
 */
public interface JavadocTokenStream {

    /**
     * Consumes the next token and returns it.
     * @return the next token
     */
    JavadocToken nextToken();

    /**
     * Returns the next token.
     * @return the next token
     */
    JavadocToken peek();

    /**
     * Looks ahead the token after the offset.
     * The next token's offset is {@code 0}.
     * @param offset the token offset
     * @return the look-ahead token
     */
    JavadocToken lookahead(int offset);

    /**
     * Marks the current stream position.
     * A latter {@link #rewind()} invocation will restore the position.
     */
    void mark();

    /**
     * Restores the stream position to the last {@link #mark() marked},
     * and discards the last marked position.
     * @throws IllegalStateException if the {@link #mark()} has not been invoked, or the mark was already discarded
     */
    void rewind();

    /**
     * Discards the last {@link #mark() marked position}.
     * @throws IllegalStateException if the {@link #mark()} has not been invoked, or the mark was already discarded
     */
    void discard();
}
