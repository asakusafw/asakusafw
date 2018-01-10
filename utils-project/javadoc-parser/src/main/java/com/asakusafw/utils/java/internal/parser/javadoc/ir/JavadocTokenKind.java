/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * Represents a kind of {@link JavadocToken}.
 */
public enum JavadocTokenKind {

    /**
     * A sequence of white-space characters (except line-break characters).
     */
    WHITE_SPACES,

    /**
     * A line-break character.
     */
    LINE_BREAK,

    /**
     * {@code "*"}.
     */
    ASTERISK,

    /**
     * An identifier.
     */
    IDENTIFIER,

    /**
     * <code>&quot;&#64;&quot;</code>.
     */
    AT,

    /**
     * {@code "."}.
     */
    DOT,

    /**
     * {@code ","}.
     */
    COMMA,

    /**
     * {@code "#"}.
     */
    SHARP,

    /**
     * {@code "["}.
     */
    LEFT_BRACKET,

    /**
     * {@code "]"}.
     */
    RIGHT_BRACKET,

    /**
     * <code>&quot;&#123;&quot;</code>.
     */
    LEFT_BRACE,

    /**
     * <code>&quot;&#125;&quot;</code>.
     */
    RIGHT_BRACE,

    /**
     * {@code "("}.
     */
    LEFT_PAREN,

    /**
     * {@code ")"}.
     */
    RIGHT_PAREN,

    /**
     * {@code "<"}.
     */
    LESS,

    /**
     * {@code ">"}.
     */
    GREATER,

    /**
     * {@code "/"}.
     */
    SLASH,

    /**
     * {@code "?"}.
     */
    QUESTION,

    /**
     * Regular text.
     */
    TEXT,

    /**
     * End-Of-File (pseudo-token kind).
     */
    EOF,
}
