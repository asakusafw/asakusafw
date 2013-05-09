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
package com.asakusafw.compiler.operator;

/**
 * これ以上のコンパイル処理を続けても有用な結果が得られないことを表す例外。
 * <p>
 * 必要なメッセージはこの例外をスローする前に出力しておくこと。
 * </p>
 */
public class OperatorCompilerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成する。
     * @param message メッセージ
     */
    public OperatorCompilerException(String message) {
        super(message);
    }

    /**
     * インスタンスを生成する。
     * @param message メッセージ
     * @param cause この例外の原因となった例外
     */
    public OperatorCompilerException(String message, Throwable cause) {
        super(message, cause);
    }
}
