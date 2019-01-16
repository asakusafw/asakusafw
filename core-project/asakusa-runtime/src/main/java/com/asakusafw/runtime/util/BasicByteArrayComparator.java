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

import org.apache.hadoop.io.WritableComparator;

/**
 * A basic implementation of {@link ByteArrayComparator}.
 * @since 0.8.0
 */
public class BasicByteArrayComparator implements ByteArrayComparator {

    @Override
    public boolean equals(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        return PureByteArrayComparator.equalsBytes(b1, s1, l1, b2, s2, l2);
    }

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        return WritableComparator.compareBytes(b1, s1, l1, b2, s2, l2);
    }
}
