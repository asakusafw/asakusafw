/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.value;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.hadoop.io.InputBuffer;
import org.apache.hadoop.io.Text;

/**
 * Utilities for {@link StringOption}.
 * @since 0.8.0
 */
public final class StringOptionUtil {

    /**
     * The internal text encoding.
     */
    public static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    private StringOptionUtil() {
        return;
    }

    /**
     * Returns a {@link Reader} to read the text contents in the {@link StringOption}.
     * @param option the target {@link StringOption}
     * @return the created reader
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static Reader asReader(StringOption option) {
        Text text = option.get();
        InputBuffer buffer = new InputBuffer();
        buffer.reset(text.getBytes(), 0, text.getLength());
        return new InputStreamReader(buffer, ENCODING);
    }

    /**
     * Trims the leading/trailing classical whitespace characters in the {@link StringOption}.
     * This only removes the following characters:
     * <ul>
     * <li> {@code "\t" (HT:U+0009)} </li>
     * <li> {@code "\n" (LF:U+000a)} </li>
     * <li> {@code "\r" (CR:U+000d)} </li>
     * <li> {@code " " (SP:U+0020)} </li>
     * </ul>
     * This directly modifies the target {@link StringOption}.
     * @param option the target {@link StringOption}
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static void trim(StringOption option) {
        Text text = option.get();
        byte[] bytes = text.getBytes();
        int length = text.getLength();
        int start = 0;
        int last = length - 1;
        for (; start <= last; start++) {
            if (isTrimTarget(bytes[start]) == false) {
                break;
            }
        }
        for (; last >= start; last--) {
            if (isTrimTarget(bytes[last]) == false) {
                break;
            }
        }
        if (start == 0 && last == length - 1) {
            return;
        }
        text.set(bytes, start, last + 1 - start);
    }

    private static boolean isTrimTarget(byte b) {
        switch (b) {
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            return true;
        default:
            return false;
        }
    }

    /**
     * Appends the text in the second {@link StringOption} into the first one.
     * This directly modifies the first {@link StringOption}.
     * @param target the append target
     * @param contents the text contents to be appended
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static void append(StringOption target, StringOption contents) {
        Text text = contents.get();
        append(target, text);
    }

    /**
     * Appends the text in the second {@link StringOption} into the first one.
     * This directly modifies the first {@link StringOption}.
     * @param target the append target
     * @param contents the text contents to be appended
     * @throws NullPointerException if the {@link StringOption} is/represents {@code null}
     */
    public static void append(StringOption target, String contents) {
        Text buffer = StringOption.BUFFER_POOL.get();
        buffer.set(contents);
        append(target, buffer);
    }

    private static void append(StringOption target, Text text) {
        target.get().append(text.getBytes(), 0, text.getLength());
    }
}
