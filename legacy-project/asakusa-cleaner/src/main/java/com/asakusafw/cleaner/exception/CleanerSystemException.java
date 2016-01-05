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
package com.asakusafw.cleaner.exception;

/**
 * Cleanerのシステム例外。
 *
 * @author yuta.shirai
 */
public class CleanerSystemException extends Exception {
    private Class<?> clazz;
    private String messageId;
    private Object[] messageArgs;


     /**
     * デフォルトシリアライズバージョン。
     */
    private static final long serialVersionUID = 1L;

    /**
    * コンストラクタ。
    * @param cause 例外
    * @param clazz 発生クラス
    * @param messageId メッセージId
    * @param messageArgs メッセージ引数
    */
    public CleanerSystemException(Throwable cause, Class<?> clazz, String messageId, Object... messageArgs) {
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
    public CleanerSystemException(Class<?> clazz, String messageId, Object... messageArgs) {
        this.clazz = clazz;
        this.messageId = messageId;
        this.messageArgs = messageArgs.clone();

    }
    /**
     * 例外が発生したクラスを返す。
     * @return 例外が発生したクラス。
     */
    public Class<?> getClazz() {
        return clazz;
    }
    /**
     * ログメッセージIDを返す。
     * @return ログメッセージID
     */
    public String getMessageId() {
        return messageId;
    }
    /**
     * ログメッセージの引数を返す。
     * @return ログメッセージの引数
     */
    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}
