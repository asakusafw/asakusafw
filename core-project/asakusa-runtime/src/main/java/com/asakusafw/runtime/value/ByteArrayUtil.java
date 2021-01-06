/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.IOException;

import org.apache.hadoop.io.WritableComparator;

import com.asakusafw.runtime.util.ByteArrayComparator;
import com.asakusafw.runtime.util.ByteArrayComparators;

final class ByteArrayUtil {

    private static final ByteArrayComparator COMPARATOR = ByteArrayComparators.getInstance();

    static boolean equals(
            byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2) {
        return COMPARATOR.equals(b1, s1, l1, b2, s2, l2);
    }

    static int compare(
            byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2) {
        return COMPARATOR.compare(b1, s1, l1, b2, s2, l2);
    }

    static int compare(int a, int b) {
        return Integer.compare(a, b);
    }

    static int compare(long a, long b) {
        return Long.compare(a, b);
    }

    static short readShort(byte[] bytes, int offset) {
        return (short) WritableComparator.readUnsignedShort(bytes, offset);
    }

    static int readInt(byte[] bytes, int offset) {
        return WritableComparator.readInt(bytes, offset);
    }

    static long readLong(byte[] bytes, int offset) {
        return WritableComparator.readLong(bytes, offset);
    }

    static long readVLong(byte[] bytes, int offset) {
        try {
            return WritableComparator.readVLong(bytes, offset);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private ByteArrayUtil() {
        return;
    }
}
