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
package com.asakusafw.compiler.common;

import java.text.MessageFormat;

/**
 * 事前条件に関するクラス。
 */
public final class Precondition {

    /**
     * 指定の値が{@code null}である場合にエラーをスローする。
     * @param value 対象の値
     * @param expression 対象の表現
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void checkMustNotBeNull(Object value, String expression) {
        if (value == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} must not be null", //$NON-NLS-1$
                    expression));
        }
    }

    private Precondition() {
        return;
    }
}
