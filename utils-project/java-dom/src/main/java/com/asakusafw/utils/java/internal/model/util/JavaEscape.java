/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * Rules about escape characters.
 */
public final class JavaEscape {

    private static final char[] ASCII_SPECIAL_ESCAPE = new char[128];
    static {
        ASCII_SPECIAL_ESCAPE['\b'] = 'b';
        ASCII_SPECIAL_ESCAPE['\t'] = 't';
        ASCII_SPECIAL_ESCAPE['\n'] = 'n';
        ASCII_SPECIAL_ESCAPE['\f'] = 'f';
        ASCII_SPECIAL_ESCAPE['\r'] = 'r';
        ASCII_SPECIAL_ESCAPE['\\'] = '\\';
    }

    private JavaEscape() {
        return;
    }

    /**
     * Escapes characters in the target text.
     * This replaces {@code \} (backslash) and non-printable characters like as Java characters,
     * for creating string or character literals.
     * @param string the target text
     * @param charValue {@code true} to escape <code>&quot;</code>, or {@code false} to escape <code>'</code>
     * @param unicodeEscape {@code true} to escape after U+007f characters, or {@code false} to keep them
     * @return the escaped string
     */
    public static String escape(String string, boolean charValue, boolean unicodeEscape) {
        StringBuilder buf = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (c <= 0x7f && ASCII_SPECIAL_ESCAPE[c] != 0) {
                buf.append('\\');
                buf.append(ASCII_SPECIAL_ESCAPE[c]);
            } else if ((charValue && c == '\'') || (!charValue && c == '"')) {
                buf.append('\\');
                buf.append(c);
            } else if (unicodeEscape || Character.isISOControl(c) || !Character.isDefined(c)) {
                addCodePoint(buf, c);
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * Decodes an escaped string.
     * @param string the escaped string
     * @return the decoded string
     * @throws IllegalArgumentException if the escaped string is malformed
     */
    public static String unescape(String string) {
        return EscapeDecoder.scan(string);
    }

    private static void addCodePoint(StringBuilder target, char c) {
        target.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
    }
}
