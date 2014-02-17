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
package com.asakusafw.runtime.core;

/**
 * 演算子の出力を表現するインターフェース。
 * @param <T> データの種類
 */
public interface Result<T> {

    /**
     * この演算子の出力に指定のデータを追加する。
     * <p>
     * この操作により、引数に指定したオブジェクトの内容が変更される場合がある。
     * 同オブジェクトを引き続き利用する場合には、あらかじめオブジェクトの内容を退避しておくこと。
     * </p>
     * @param result 追加するデータ
     * @throws Result.OutputException 追加に失敗した場合
     */
    void add(T result);

    /**
     * 演算子がデータの出力に失敗したことを表す例外。
     */
    class OutputException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * インスタンスを生成する。
         */
        public OutputException() {
            super();
        }

        /**
         * インスタンスを生成する。
         * @param message メッセージ
         */
        public OutputException(String message) {
            super(message);
        }

        /**
         * インスタンスを生成する。
         * @param cause 原因
         */
        public OutputException(Throwable cause) {
            super(cause);
        }

        /**
         * インスタンスを生成する。
         * @param message メッセージ
         * @param cause 原因
         */
        public OutputException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
