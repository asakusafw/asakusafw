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
package com.asakusafw.dmdl.thundergate.model;

/**
 * プロパティ型の種類。
 */
public enum PropertyTypeKind {

    /**
     * 8bit符号付き整数。
     */
    BYTE(false),

    /**
     * 16bit符号付き整数。
     */
    SHORT(false),

    /**
     * 32bit符号付き整数。
     */
    INT(false),

    /**
     * 64bit符号付き整数。
     */
    LONG(false),

    /**
     * 単精度浮動小数点数。
     */
    FLOAT(false),

    /**
     * 倍精度浮動小数点数。
     */
    DOUBLE(false),

    /**
     * 任意の10進数。
     */
    BIG_DECIMAL(true),

    /**
     * 論理値。
     */
    BOOLEAN(false),

    /**
     * 文字列。
     */
    STRING(true),

    /**
     * 日付。
     */
    DATE(false),

    /**
     * 時刻。
     */
    DATETIME(false),
    ;

    /**
     * 多相データかどうか。
     */
    public final boolean variant;

    private PropertyTypeKind(boolean variant) {
        this.variant = variant;
    }
}
