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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * Represents a kind of {@link IrDocElement}.
 */
public enum IrDocElementKind {

    /**
     * Java documentation comments.
     */
    COMMENT,

    /**
     * Blocks.
     */
    BLOCK,

    /**
     * Simple names.
     */
    SIMPLE_NAME,

    /**
     * Qualified names.
     */
    QUALIFIED_NAME,

    /**
     * Field references.
     */
    FIELD,

    /**
     * Method or constructor references.
     */
    METHOD,

    /**
     * Plain texts.
     */
    TEXT,

    /**
     * Method or constructor parameters.
     */
    METHOD_PARAMETER,

    /**
     * Basic types.
     */
    BASIC_TYPE,

    /**
     * Named types.
     */
    NAMED_TYPE,

    /**
     * Array types.
     */
    ARRAY_TYPE,
}
