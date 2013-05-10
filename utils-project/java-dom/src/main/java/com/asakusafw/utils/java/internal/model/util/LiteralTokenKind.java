/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * リテラルの種類。
 */
public enum LiteralTokenKind {

    /**
     * 32bit整数リテラル。
     */
    INT,

    /**
     * 64bit整数リテラル。
     */
    LONG,

    /**
     * 単精度浮動小数点数リテラル。
     */
    FLOAT,

    /**
     * 倍精度浮動小数点数リテラル。
     */
    DOUBLE,

    /**
     * 真偽値リテラル。
     */
    BOOLEAN,

    /**
     * 文字リテラル。
     */
    CHAR,

    /**
     * 文字列リテラル。
     */
    STRING,

    /**
     * {@code null}リテラル。
     */
    NULL,

    /**
     * 不明なリテラル。
     */
    UNKNOWN,
}
