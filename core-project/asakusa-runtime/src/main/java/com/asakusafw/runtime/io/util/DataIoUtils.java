/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * Utilities for {@link DataInput} and {@link DataOutput}.
 */
public final class DataIoUtils {

    private static final ThreadLocal<StringBuilder> STRING_BUFFER_POOL = ThreadLocal.withInitial(StringBuilder::new);

    private static final int MASK_BYTE = ~(-1 << Byte.SIZE); // 1111_1111

    private static final int MASK_HEAD1 = (-1 << (Byte.SIZE - 1)) & MASK_BYTE; // 1000_0000

    private static final int MASK_HEAD2 = (-1 << (Byte.SIZE - 2)) & MASK_BYTE; // 1100_0000

    private static final int MASK_HEAD3 = (-1 << (Byte.SIZE - 3)) & MASK_BYTE; // 1110_0000

    private static final int MASK_HEAD4 = (-1 << (Byte.SIZE - 4)) & MASK_BYTE; // 1111_0000

    private static final int MASK_BODY4 = ~(-1 << 4); // 0000_1111

    private static final int MASK_BODY5 = ~(-1 << 5); // 0001_1111

    private static final int MASK_BODY6 = ~(-1 << 6); // 0011_1111

    private static final char CHAR_ZERO = '\u0000';

    private static final char CHAR_MAX1 = '\u007f';

    private static final char CHAR_MAX2 = '\u07ff';

    private DataIoUtils() {
        return;
    }

    /**
     * Emulates {@link DataInput#readUTF()} without using it method.
     * @param input the target {@link DataInput}
     * @return the result
     * @throws IOException if failed to read String from the {@link DataInput}
     */
    public static String readUTF(DataInput input) throws IOException {
        int size = input.readUnsignedShort();
        if (size == 0) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder buf = STRING_BUFFER_POOL.get();
        buf.setLength(0);
        while (size > 0) {
            int b0 = read(input);
            if (b0 < MASK_HEAD1) {
                // 1-byte (7-bits)
                assert (b0 & MASK_HEAD1) == 0;
                size -= 1;
                buf.append((char) b0);
            } else if (b0 < MASK_HEAD3) {
                // 2-bytes (11-bits)
                assert (b0 & MASK_HEAD3) == MASK_HEAD2;
                if (size < 2) {
                    throw new UTFDataFormatException();
                }
                size -= 2;
                int b1 = read(input, MASK_HEAD2, MASK_HEAD1);
                buf.append((char) ((b0 & MASK_BODY5) << 6 | (b1 & MASK_BODY6)));
            } else {
                // 3-bytes (16-bits)
                checkHeader(b0, MASK_HEAD4, MASK_HEAD3);
                if (size < 3) {
                    throw new UTFDataFormatException();
                }
                size -= 3;
                int b1 = read(input, MASK_HEAD2, MASK_HEAD1);
                int b2 = read(input, MASK_HEAD2, MASK_HEAD1);
                buf.append((char) ((b0 & MASK_BODY4) << 12 | (b1 & MASK_BODY6) << 6 | (b2 & MASK_BODY6)));
            }
        }
        return buf.toString();
    }

    private static int read(DataInput input) throws IOException {
        return input.readByte() & MASK_BYTE;
    }

    private static int read(DataInput input, int mask, int expected) throws IOException {
        int b = read(input);
        checkHeader(b, mask, expected);
        return b;
    }

    private static void checkHeader(int b, int mask, int expected) throws IOException {
        if ((b & mask) != expected) {
            throw new IOException();
        }
    }

    /**
     * Emulates {@link DataOutput#writeUTF(String)} without using it method.
     * @param output the target {@link DataOutput}
     * @param value the target value
     * @throws IOException if failed to write String into {@link DataOutput}
     */
    public static void writeUTF(DataOutput output, String value) throws IOException {
        int size = computeUtfBodySize(value);
        if (size >>> Short.SIZE != 0) {
            throw new UTFDataFormatException("too long UTF string");
        }
        output.writeShort(size);
        for (int i = 0, n = value.length(); i < n; i++) {
            char c = value.charAt(i);
            if (c != CHAR_ZERO && c <= CHAR_MAX1) {
                output.write(c);
            } else if (c <= CHAR_MAX2) {
                output.write(MASK_HEAD2 | ((c >> 6) & MASK_BODY5));
                output.write(MASK_HEAD1 | (c & MASK_BODY6));
            } else {
                output.write(MASK_HEAD3 | ((c >> 12) & MASK_BODY4));
                output.write(MASK_HEAD1 | ((c >>  6) & MASK_BODY6));
                output.write(MASK_HEAD1 | (c & MASK_BODY6));
            }
        }
    }

    private static int computeUtfBodySize(String value) {
        int result = 0;
        for (int i = 0, n = value.length(); i < n; i++) {
            char c = value.charAt(i);
            if (c != CHAR_ZERO && c <= CHAR_MAX1) {
                result += 1;
            } else if (c <= CHAR_MAX2) {
                result += 2;
            } else {
                result += 3;
            }
        }
        return result;
    }
}
