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
package com.asakusafw.runtime.util;

/**
 * An abstract super interface for comparing byte arrays.
 * @since 0.8.0
 */
@FunctionalInterface
public interface ByteArrayComparator {

    /**
     * Returns whether the each byte array region is equivalent or not.
     * @param b1 the first byte array
     * @param s1 the starting offset in the first byte array (in bytes)
     * @param l1 the length in the first byte array (in bytes)
     * @param b2 the second byte array
     * @param s2 the starting offset in the second byte array (in bytes)
     * @param l2 the length in the second byte array (in bytes)
     * @return {@code true} if the each byte array range is equivalent, otherwise {@code false}
     */
    default boolean equals(
            byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2) {
        return compare(b1, s1, l1, b2, s2, l2) == 0;
    }

    /**
     * Compares the two byte array regions (as unsigned bytes), and returns their sign value.
     * @param b1 the first byte array
     * @param s1 the starting offset in the first byte array (in bytes)
     * @param l1 the length in the first byte array (in bytes)
     * @param b2 the second byte array
     * @param s2 the starting offset in the second byte array (in bytes)
     * @param l2 the length in the second byte array (in bytes)
     * @return {@code < 0}, {@code = 0}, or {@code > 0} as the first byte array range is less than, equal to, or
     *      greater than the second range
     */
    int compare(
            byte[] b1, int s1, int l1,
            byte[] b2, int s2, int l2);
}
