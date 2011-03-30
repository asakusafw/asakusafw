/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;

/**
 * Operator DSLコンパイラのオプション一覧。
 */
public class OperatorCompilerOptions {

    /**
     * 注釈プロセッサのオプションを解析してこのオブジェクトを返す。
     * @param options 注釈プロセッサのオプション一覧
     * @return 解析結果
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static OperatorCompilerOptions parse(Map<String, String> options) {
        Precondition.checkMustNotBeNull(options, "options"); //$NON-NLS-1$
        // 必要に応じて
        return new OperatorCompilerOptions();
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{''}'",
                getClass().getSimpleName());
    }
}
