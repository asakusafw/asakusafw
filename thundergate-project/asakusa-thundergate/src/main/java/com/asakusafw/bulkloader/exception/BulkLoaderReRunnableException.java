/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.bulkloader.exception;

/**
 * Importer/Exporterのリトライ可能例外。
 * @author yuta.shirai
 */
public class BulkLoaderReRunnableException extends Exception {
    private Class<?> clazz;
    private String messageId;
    private Object[] messageArgs;

    private static final long serialVersionUID = 1L;

    /**
     * コンストラクタ。
     * @param cause 例外
     * @param clazz 発生クラス
     * @param messageId メッセージId
     * @param messageArgs メッセージ引数
     */
    public BulkLoaderReRunnableException(
            Throwable cause,
            Class<?> clazz,
            String messageId,
            Object... messageArgs) {
        super(cause);
        this.clazz = clazz;
        this.messageId = messageId;
        this.messageArgs = messageArgs.clone();
    }
    /**
     * コンストラクタ。
     * @param clazz 発生クラス
     * @param messageId メッセージId
     * @param messageArgs メッセージ引数
     */
    public BulkLoaderReRunnableException(Class<?> clazz, String messageId, Object... messageArgs) {
        this.clazz = clazz;
        this.messageId = messageId;
        this.messageArgs = messageArgs.clone();
    }

    /**
     * この例外の発生クラスを返す。
     * @return この例外の発生クラス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * この例外に関するメッセージIDを返す。
     * @return この例外に関するメッセージID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * この例外のメッセージに関する引数の一覧を返す。
     * @return この例外のメッセージに関する引数の一覧
     */
    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}
