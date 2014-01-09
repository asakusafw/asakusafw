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
package com.asakusafw.compiler.operator.processor;

import java.util.Set;
import java.util.regex.Pattern;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.utils.collections.Sets;

/**
 *　演算子プロセッサに対するユーティリティ群。
 */
public final class OperatorProcessorUtil {

    /**
     * 指定のポート名一覧をチェックして、間違いがあればエラーを報告する。
     * @param analyzer エラーの報告先
     * @param names 名前の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void checkPortName(ExecutableAnalyzer analyzer, String[] names) {
        Precondition.checkMustNotBeNull(analyzer, "analyzer"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(names, "names"); //$NON-NLS-1$
        for (String name : names) {
            checkName(analyzer, name);
        }
        Set<String> saw = Sets.create();
        for (String name : names) {
            if (saw.contains(name)) {
                analyzer.error("ポート名\"{}\"が重複しています", name);
                saw.remove(name);
            } else {
                saw.add(name);
            }
        }
    }

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*");
    private static void checkName(ExecutableAnalyzer analyzer, String name) {
        assert name != null;
        if (VALID_NAME.matcher(name).matches() == false) {
            analyzer.error("ポート名に\"{}\"は利用できません", name);
        }
    }

    private OperatorProcessorUtil() {
        return;
    }
}
