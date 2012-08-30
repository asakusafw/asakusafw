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
package com.asakusafw.runtime.io;

import java.io.IOException;

/**
 * レコードの解析に失敗したことを表す例外。
 */
public class RecordFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成する。
     * @param message 例外メッセージ (省略可)
     */
    public RecordFormatException(String message) {
        super(message);
    }

    /**
     * インスタンスを生成する。
     * @param message 例外メッセージ (省略可)
     * @param cause この例外の原因 (省略可)
     */
    public RecordFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
