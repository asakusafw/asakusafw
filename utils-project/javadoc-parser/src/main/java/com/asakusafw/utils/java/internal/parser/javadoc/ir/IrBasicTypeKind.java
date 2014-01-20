/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
 * 基本型の種類。
 */
public enum IrBasicTypeKind {

    /**
     * {@code int}。
     */
    INT,

    /**
     * {@code long}。
     */
    LONG,

    /**
     * {@code float}。
     */
    FLOAT,

    /**
     * {@code double}。
     */
    DOUBLE,

    /**
     * {@code byte}。
     */
    BYTE,

    /**
     * {@code short}。
     */
    SHORT,

    /**
     * {@code char}。
     */
    CHAR,

    /**
     * {@code boolean}。
     */
    BOOLEAN,

    /**
     * {@code void}。
     */
    VOID,

    ;
    /**
     * この型を表現するシンボルを返す。
     * @return この型を表現するシンボル
     */
    public String getSymbol() {
        return name().toLowerCase();
    }
}
