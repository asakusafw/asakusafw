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
package com.asakusafw.compiler.operator;

import javax.tools.Diagnostic;

/**
 * これ以上のコンパイル処理を続けても有用な結果が得られないことを表す例外。
 * <p>
 * 必要なメッセージはこの例外をスローする前に出力しておくこと。
 * </p>
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorCompilerException extends RuntimeException {

    private static final long serialVersionUID = 2L;

    private static final Diagnostic.Kind DEFAULT_KIND = Diagnostic.Kind.OTHER;

    private final Diagnostic.Kind kind;

    /**
     * インスタンスを生成する。
     * @param message メッセージ
     */
    public OperatorCompilerException(String message) {
        this(DEFAULT_KIND, message);
    }

    /**
     * インスタンスを生成する。
     * @param message メッセージ
     * @param cause この例外の原因となった例外
     */
    public OperatorCompilerException(String message, Throwable cause) {
        this(DEFAULT_KIND, message, cause);
    }

    /**
     * インスタンスを生成する。
     * @param kind 診断情報の種類
     * @param message メッセージ
     * @since 0.7.0
     */
    public OperatorCompilerException(Diagnostic.Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    /**
     * インスタンスを生成する。
     * @param kind 診断情報の種類
     * @param message メッセージ
     * @param cause この例外の原因となった例外
     * @since 0.7.0
     */
    public OperatorCompilerException(Diagnostic.Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    /**
     * 診断情報の種類を返す。
     * @return 診断情報の種類、診断情報を出力しない場合は{@code null}
     * @since 0.7.0
     */
    public Diagnostic.Kind getKind() {
        return kind;
    }
}
