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
package com.asakusafw.utils.java.parser.javadoc;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * Javadocの解析に失敗したことを表す例外。
 */
public class JavadocParseException extends Exception {

    private static final long serialVersionUID = 1L;

    private IrLocation location;

    /**
     * Creates a new instance.
     * @param message メッセージ
     * @param location エラーの位置
     * @param cause この例外の元となった例外
     */
    public JavadocParseException(String message, IrLocation location, Throwable cause) {
        super(message, cause);
        this.location = location;
    }

    /**
     * エラーの位置を返す。
     * @return エラーの位置
     */
    public IrLocation getLocation() {
        return this.location;
    }
}
