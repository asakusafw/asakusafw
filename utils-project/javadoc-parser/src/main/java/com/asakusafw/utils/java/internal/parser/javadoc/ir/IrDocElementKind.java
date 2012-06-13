/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
 * {@link IrDocElement}の要素種。
 */
public enum IrDocElementKind {

    /**
     * Javadoc全体。
     */
    COMMENT,

    /**
     * ブロック。
     */
    BLOCK,

    /**
     * 単純名。
     */
    SIMPLE_NAME,

    /**
     * 限定名。
     */
    QUALIFIED_NAME,

    /**
     * フィールド。
     */
    FIELD,

    /**
     * メソッド。
     */
    METHOD,

    /**
     * テキスト。
     */
    TEXT,

    /**
     * メソッド仮引数。
     */
    METHOD_PARAMETER,

    /**
     * 基本型。
     */
    BASIC_TYPE,

    /**
     * 名前付き型。
     */
    NAMED_TYPE,

    /**
     * 配列型。
     */
    ARRAY_TYPE,
}
