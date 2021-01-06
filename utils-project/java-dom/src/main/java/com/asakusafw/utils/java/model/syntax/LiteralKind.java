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
package com.asakusafw.utils.java.model.syntax;

/**
 * Represents a kind of literals.
 */
public enum LiteralKind {

    /**
     * 32bit integer literal.
     */
    INT,

    /**
     * 64bit integer literal.
     */
    LONG,

    /**
     * Single precision floating point number literal.
     */
    FLOAT,

    /**
     * Double precision floating point number literal.
     */
    DOUBLE,

    /**
     * Character literal.
     */
    CHAR,

    /**
     * Boolean literal.
     */
    BOOLEAN,

    /**
     * Character string literal.
     */
    STRING,

    /**
     * {@code null} literal.
     */
    NULL,
    ;

    /**
     * The token image of {@code null} literal.
     */
    public static final String TOKEN_NULL = "null"; //$NON-NLS-1$

    /**
     * The token image of {@code true} literal.
     */
    public static final String TOKEN_TRUE = "true"; //$NON-NLS-1$

    /**
     * The token image of {@code false} literal.
     */
    public static final String TOKEN_FALSE = "false"; //$NON-NLS-1$
}
