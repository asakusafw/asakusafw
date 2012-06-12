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
package com.asakusafw.utils.java.internal.model.util;

/**
 * Javaの文字エスケープに関する処理。
 */
public class JavaEscape {

    private static final char[] ASCII_SPECIAL_ESCAPE = new char[128];
    static {
        ASCII_SPECIAL_ESCAPE['\b'] = 'b';
        ASCII_SPECIAL_ESCAPE['\t'] = 't';
        ASCII_SPECIAL_ESCAPE['\n'] = 'n';
        ASCII_SPECIAL_ESCAPE['\f'] = 'f';
        ASCII_SPECIAL_ESCAPE['\r'] = 'r';
        ASCII_SPECIAL_ESCAPE['\\'] = '\\';
    }

    /**
     * インスタンス化の禁止。
     */
    private JavaEscape() {
        super();
    }

    /**
     * 文字列の各文字を必要に応じてエスケープし、ASCIIコードの範囲で表示可能にする。
     * ASCIIコードの表示可能文字はそのままの値を保ち、
     * {@code \b, \t, \n, \f, \r}はそれぞれ左記のようにエスケープされる。
     * @param string エスケープする文字列
     * @param charValue {@code true}が指定された場合、&quot;はエスケープせずに'をエスケープする
     * @param unicodeEscape {@code true}が指定された場合、\u007f以降の文字は全てunicode escapeする
     * @return エスケープされた文字列
     */
    public static String escape(String string, boolean charValue, boolean unicodeEscape) {
        StringBuilder buf = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (c <= 0x7f && ASCII_SPECIAL_ESCAPE[c] != 0) {
                buf.append('\\');
                buf.append(ASCII_SPECIAL_ESCAPE[c]);
            }
            else if ((charValue && c == '\'') || (!charValue && c == '"')) {
                buf.append('\\');
                buf.append(c);
            }
            else if (unicodeEscape || Character.isISOControl(c) || !Character.isDefined(c)){
                addCodePoint(buf, c);
            }
            else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * エスケープを含む文字列のエスケープを解除した文字列を返す。
     * @param string エスケープを解除する文字列
     * @return エスケープが解除された文字列
     * @throws IllegalArgumentException 解除できないエスケープが含まれていた場合
     */
    public static String unescape(String string) {
        return EscapeDecoder.scan(string);
    }

    private static void addCodePoint(StringBuilder target, char c) {
        target.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
    }
}
