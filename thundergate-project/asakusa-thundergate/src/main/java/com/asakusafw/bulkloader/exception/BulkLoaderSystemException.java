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

import java.sql.SQLException;


/**
 * Importer/Exporterのシステム例外。
 * @author yuta.shirai
 */
public class BulkLoaderSystemException extends Exception {
    private final Class<?> clazz;
    private final String messageId;
    private final Object[] messageArgs;

    private static final long serialVersionUID = 1L;

    /**
    * コンストラクタ。
    * @param cause 例外
    * @param clazz 発生クラス
    * @param messageId メッセージId
    * @param messageArgs メッセージ引数
    */
    public BulkLoaderSystemException(Throwable cause, Class<?> clazz, String messageId, Object... messageArgs) {
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
    public BulkLoaderSystemException(Class<?> clazz, String messageId, Object... messageArgs) {
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
    /**
     * SQL実行時に発生した例外をIOSystemExceptionにする。
     * @param e SQL例外
     * @param clazz 例外が発生したクラス
     * @param sql 例外が発生したSQL文
     * @param params 例外が発生したSQL文のパラメータ
     * @return IOSystemException
     */
    public static BulkLoaderSystemException createInstanceCauseBySQLException(
            SQLException e,
            Class<?> clazz,
            String sql,
            String... params) {
        String param = null;
        if (params != null && params.length != 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i]);
                if (i != params.length) {
                    sb.append(",");
                }
            }
            param = sb.toString();
        }
        return new BulkLoaderSystemException(e, clazz, "TG-COMMON-00014", sql, param);
    }
    /**
     * SQL実行時に発生した例外をIOSystemExceptionにする。
     * @param e SQL例外
     * @param clazz 例外が発生したクラス
     * @param sql 例外が発生したSQL文
     * @param params 例外が発生したSQL文のパラメータ
     * @return IOSystemException
     */
    public static BulkLoaderSystemException createInstanceCauseBySQLException(
            SQLException e,
            Class<?> clazz,
            String sql,
            Object... params) {
        String param = null;
        if (params != null && params.length != 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i]);
                if (i != params.length) {
                    sb.append(",");
                }
            }
            param = sb.toString();
        }
        return new BulkLoaderSystemException(e, clazz, "TG-COMMON-00014", sql, param);
    }
}
