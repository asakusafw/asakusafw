/**
 * Copyright 2012 Asakusa Framework Team.
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

import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * {@link WritableComparable} which is comparable in is bytes.
 * Clients must override {@link #hashCode()} and {@link #equals(Object)}.
 * @since 0.2.5
 */
public interface WritableRawComparable extends WritableComparable<WritableRawComparable> {

    /**
     * Computes and returns size in bytes.
     * @param buf bytes array
     * @param offset bytes offset
     * @return size in bytes
     * @throws IOException if failed to compute size
     */
    int getSizeInBytes(byte[] buf, int offset) throws IOException;

    /**
     * Compares two objects in bytes.
     * @param b1 bytes representation of the first object
     * @param o1 offset of the first object
     * @param b2 bytes representation of the second object
     * @param o2 offset of the second object
     * @return the comparison result
     * @throws IOException if failed to comparison
     */
    int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException;
}
