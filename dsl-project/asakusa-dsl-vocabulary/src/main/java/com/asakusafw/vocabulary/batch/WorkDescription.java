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
package com.asakusafw.vocabulary.batch;

import java.util.regex.Pattern;

/**
 * 任意の処理を実行する。
 */
public abstract class WorkDescription {

    /**
     * この処理の識別子を返す。
     * <p>
     * この識別子は、同一のバッチ内で重複してはならない。
     * また、識別子には、下記の形式の名前 (Javaの変数名のうち、ASCIIコード表に収まるもののみ)
     * を利用可能である。
     * </p>
<pre><code>
Name :
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     * @return この処理の識別子
     */
    public abstract String getName();

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*");

    /**
     * 指定の名前が処理の識別子として正しい場合のみ{@code true}を返す。
     * @param name 名前
     * @return 名前が識別子として正しい場合のみ{@code true}、{@code null}や不正な文字列の場合は{@code false}
     */
    protected static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return VALID_NAME.matcher(name).matches();
    }
}
