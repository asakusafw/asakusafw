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
package com.asakusafw.runtime.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 変数を含む文字列を解析して変数を展開した文字列を構築する。
 * <p>
 * それぞれのパス文字列には<code>${変数名}</code>の形式で変数を含めることができ、
 * 予め{@link #defineVariable(String, String)}で定義された変数名に対する
 * 文字列に置き換えられる。
 * </p>
 */
public class VariableTable {

    private static final Pattern VARIABLE = Pattern.compile("\\$\\{(.*?)\\}");

    private final RedefineStrategy redefineStrategy;

    private final Map<String, String> variables = new HashMap<String, String>();

    /**
     * 変数の再定義が不可能な空の変数表を生成する。
     */
    public VariableTable() {
        this(RedefineStrategy.ERROR);
    }

    /**
     * 変数の再定義時の動作を指定して、空の変数表を作成する。
     * @param redefineStrategy 再定義時の動作
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VariableTable(RedefineStrategy redefineStrategy) {
        if (redefineStrategy == null) {
            throw new IllegalArgumentException("redefineStrategy must not be null"); //$NON-NLS-1$
        }
        this.redefineStrategy = redefineStrategy;
    }

    /**
     * 指定の変数名を、このパーサーが理解できる変数の表記に変換して返す。
     * @param name 変数名
     * @return 対応する変数の表記
     * @throws IllegalArgumentException 変数として適切でない場合
     */
    public static String toVariable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        String expr = "${" + name + "}";
        if (VARIABLE.matcher(expr).matches() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" is not a valid variable name",
                    name));
        }
        return expr;
    }

    /**
     * この変数表に新しい変数を定義する。
     * @param name 変数の名前
     * @param replacement 変数の値
     * @throws IllegalArgumentException 変数の再定義が不可能で同名の変数が定義済みである場合、
     *     または引数に{@code null}が指定された場合
     */
    public void defineVariable(String name, String replacement) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (replacement == null) {
            throw new IllegalArgumentException("replacement must not be null"); //$NON-NLS-1$
        }
        if (redefineStrategy != RedefineStrategy.OVERWRITE && variables.containsKey(name)) {
            if (redefineStrategy == RedefineStrategy.ERROR) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "variable \"{0}\" is already defined in the variable table: {1}",
                        name,
                        this));
            }
        } else {
            this.variables.put(name, replacement);
        }
    }

    /**
     * この変数表に新しい変数を定義する。
     * @param variableMap 変数の一覧
     * @throws IllegalArgumentException 変数の再定義が不可能で同名の変数が定義済みである場合、
     *     または引数に{@code null}が指定された場合
     * @since 0.2.2
     */
    public void defineVariables(Map<String, String> variableMap) {
        if (variableMap == null) {
            throw new IllegalArgumentException("variableMap must not be null"); //$NON-NLS-1$
        }
        for (Map.Entry<String, String> entry : variableMap.entrySet()) {
            defineVariable(entry.getKey(), entry.getValue());
        }
    }

    /**
     * この変数表の内容を、{@link #defineVariables(String)}で利用可能な文字列に変換して返す。
     * @return 変数表の内容
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public String toSerialString() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            buf.append(escape(entry.getKey()));
            buf.append("=");
            buf.append(escape(entry.getValue()));
            buf.append(",");
        }
        if (buf.length() >= 1) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    private static final Pattern TO_ESCAPED = Pattern.compile("[=,\\\\]");
    private String escape(String string) {
        assert string != null;
        return TO_ESCAPED.matcher(string).replaceAll("\\\\$0");
    }

    private static final Pattern PAIRS = Pattern.compile("(?<!\\\\),");
    private static final Pattern KEY_VALUE = Pattern.compile("(?<!\\\\)=");

    /**
     * 変数表の一覧を定義する文字列を解析し、この変数表に追加する。
     * <p>
     * 変数表の形式は以下の{@code VariableList}をゴール記号とした、構文で表される。
     * ここで定義された{@code Value_key}をキーに、{@code Value_value}を値とするような
     * 変数を定義する。
     * </p>
<pre><code>
VariableList:
    VariableList "," Variable
    Variable
    ","
    (Empty)

Variable:
    Value_key "=" Value_value

Value:
    Character*

Character:
    any character except ",", "=", "\\"
    "\" any character
</code></pre>
     * @param variableList 変数表の一覧を定義する文字列
     * @throws IllegalArgumentException 変数の再定義が不可能で同名の変数が定義済みである場合、
     *     または引数に{@code null}が指定された場合
     */
    public void defineVariables(String variableList) {
        if (variableList == null) {
            throw new IllegalArgumentException("variableList must not be null"); //$NON-NLS-1$
        }
        String[] pairs = PAIRS.split(variableList);
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] kv = KEY_VALUE.split(pair);
            if (kv.length == 0) {
                // "=" returns a empty array
                defineVariable("", "");
            } else if (kv.length == 1 && kv[0].equals(pair) == false) {
                // "key=" returns only its key
                defineVariable(unescape(kv[0]), "");
            } else if (kv.length == 2) {
                defineVariable(unescape(kv[0]), unescape(kv[1]));
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid variable record \"{0}\", in \"{1}\"",
                        pair,
                        variableList));
            }
        }
    }

    private String unescape(String string) {
        assert string != null;
        StringBuilder buf = new StringBuilder();
        int start = 0;
        while (true) {
            int index = string.indexOf('\\', start);
            if (index < 0) {
                break;
            }
            buf.append(string.substring(start, index));
            if (index != string.length() - 1) {
                buf.append(string.charAt(index + 1));
                start = index + 2;
            } else {
                buf.append(string.charAt(index));
                start = index + 1;
            }
        }
        if (start < string.length()) {
            buf.append(string.substring(start));
        }
        return buf.toString();
    }

    /**
     * この変数表に登録された変数の一覧を返す。
     * @return この変数表に登録された変数の一覧
     */
    public Map<String, String> getVariables() {
        return new TreeMap<String, String>(variables);
    }

    /**
     * この変数表を利用して変数を含む文字列を展開する。
     * @param string 展開対象の文字列
     * @return 変数を展開した文字列
     * @throws IllegalArgumentException 定義されていない変数が含まれる場合、
     *     または引数に{@code null}が指定された場合
     * @see #defineVariable(String, String)
     */
    public String parse(String string) {
        return parse(string, true);
    }

    /**
     * この変数表を利用して変数を含む文字列を展開する。
     * @param string 展開対象の文字列
     * @param strict 存在しない変数が含まれる場合にエラーとする場合は{@code true}、無視する場合は{@code false}
     * @return 変数を展開した文字列
     * @throws IllegalArgumentException {@code strict=true}で定義されていない変数が含まれる場合、
     *     または引数に{@code null}が指定された場合
     * @see #defineVariable(String, String)
     */
    public String parse(String string, boolean strict) {
        if (string == null) {
            throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        int start = 0;
        Matcher matcher = VARIABLE.matcher(string);
        while (matcher.find(start)) {
            String name = matcher.group(1);
            String replacement = variables.get(name);
            if (replacement == null) {
                if (strict) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "variable \"{0}\" is not defined in the variable table: {1}",
                            name,
                            this));
                } else {
                    buf.append(string.substring(start, matcher.start()));
                    buf.append(matcher.group(0));
                }
            } else {
                buf.append(string.substring(start, matcher.start()));
                buf.append(replacement);
            }
            start = matcher.end();
        }
        buf.append(string.substring(start));
        return buf.toString();
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}{1}",
                getClass().getSimpleName(),
                variables.toString());
    }

    /**
     * 変数が再定義された際の動作。
     */
    public enum RedefineStrategy {

        /**
         * 最後の定義で上書きする。
         */
        OVERWRITE,

        /**
         * 最後の定義を無視する。
         */
        IGNORE,

        /**
         * エラーとする。
         */
        ERROR,
    }
}
