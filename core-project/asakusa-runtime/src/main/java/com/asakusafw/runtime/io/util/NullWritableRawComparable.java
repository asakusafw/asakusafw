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
package com.asakusafw.runtime.io.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * An implementation of {@link WritableRawComparable} represents {@code null}.
 * @since 0.4.0
 */
public final class NullWritableRawComparable implements WritableRawComparable {

    /**
     * A invariant instance.
     */
    public static final NullWritableRawComparable INSTANCE = new NullWritableRawComparable();

    @Override
    public void write(DataOutput out) throws IOException {
        return;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        return;
    }

    @Override
    public int compareTo(WritableRawComparable o) {
        if (o instanceof NullWritableRawComparable) {
            return 0;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getSizeInBytes(byte[] buf, int offset) throws IOException {
        return 0;
    }

    @Override
    public int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        return 0;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(null)";
    }
}
