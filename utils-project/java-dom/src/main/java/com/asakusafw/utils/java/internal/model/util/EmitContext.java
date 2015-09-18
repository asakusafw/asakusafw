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
package com.asakusafw.utils.java.internal.model.util;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Model;

/**
 * An interface of emitting {@link Model} objects.
 */
public interface EmitContext {

    /**
     * Flushes the previously registered comments.
     * @see #comment(int, String)
     */
    void flushComments();

    /**
     * Flushes the previously registered comments before the specified character position.
     * @param location the character position (exclusive)
     * @see #comment(int, String)
     */
    void flushComments(int location);

    /**
     * Emits a keyword.
     * @param keyword the keyword string
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void keyword(String keyword);

    /**
     * Emits a symbol.
     * @param symbol the symbol string
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void symbol(String symbol);

    /**
     * Emits a normal text.
     * @param immediate the string
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void immediate(String immediate);

    /**
     * Emits an operator symbol.
     * @param symbol the operator symbol
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void operator(String symbol);

    /**
     * Emits a separator symbol.
     * @param symbol the separator symbol
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void separator(String symbol);

    /**
     * Emits a padding.
     */
    void padding();

    /**
     * Registers a comment.
     * The comment will be emitted by {@link #flushComments(int)}.
     * @param location the character position of the comment
     * @param content contents of the comment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void comment(int location, String content);

    /**
     * Emits a begin or end of a class block.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void classBlock(EmitDirection direction);

    /**
     * Emits a begin or end of an array initializer block.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void arrayInitializerBlock(EmitDirection direction);

    /**
     * Emits a begin or end of a statement block.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void statementBlock(EmitDirection direction);

    /**
     * Emits a begin or end of a {@code switch} label block.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void switchLabel(EmitDirection direction);

    /**
     * Emits a begin or end of a statement.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void statement(EmitDirection direction);

    /**
     * Emits a begin or end of a type or member declaration.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void declaration(EmitDirection direction);

    /**
     * Emits a begin or end of a documentation comment.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void docComment(EmitDirection direction);

    /**
     * Emits a begin or end of a block in documentation comments.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void docBlock(EmitDirection direction);

    /**
     * Emits a begin or end of a inline block in documentation comments.
     * @param direction indicates either begin or end
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void docInlineBlock(EmitDirection direction);

    /**
     * Adds an block comment.
     * @param contents contents of the comment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void putBlockComment(List<String> contents);

    /**
     * Puts an line comment.
     * @param content contents of the comment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void putLineComment(String content);

    /**
     * Puts an inline comment.
     * @param content contents of the comment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void putInlineComment(String content);
}
