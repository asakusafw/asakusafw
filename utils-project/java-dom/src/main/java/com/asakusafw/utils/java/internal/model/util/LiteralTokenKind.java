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

/**
 * Represents a kind of literal.
 */
public enum LiteralTokenKind {

    /**
     * 32bit integer literal.
     */
    INT,

    /**
     * 64bit integer literal.
     */
    LONG,

    /**
     * 32bit floating point number literal.
     */
    FLOAT,

    /**
     * 64bit floating point number literal.
     */
    DOUBLE,

    /**
     * boolean literal.
     */
    BOOLEAN,

    /**
     * character literal.
     */
    CHAR,

    /**
     * character string literal.
     */
    STRING,

    /**
     * {@code null} literal.
     */
    NULL,

    /**
     * Unknown (may be malformed) literal.
     */
    UNKNOWN,
}
