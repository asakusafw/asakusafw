/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

/**
 * A pure JVM implementation of {@link ByteArrayComparator}.
 * @since 0.8.0
 */
public class PureByteArrayComparator implements ByteArrayComparator {

    @Override
    public boolean equals(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        return equalsBytes(b1, s1, l1, b2, s2, l2);
    }

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        return compareBytes(b1, s1, l1, b2, s2, l2);
    }

    static boolean equalsBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        if (l1 != l2) {
            return false;
        }
        for (int i = 0; i < l1; i++) {
            if (b1[s1 + i] != b2[s2 + i]) {
                return false;
            }
        }
        return true;
    }

    static int compareBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        for (int i = 0, n = Math.min(l1, l2); i < n; i++) {
            int cmp = Integer.compare(b1[s1 + i] & 0xff, b2[s2 + i] & 0xff);
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(l1, l2);
    }
}
