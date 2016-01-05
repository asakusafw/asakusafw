/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Test helpders for writable objects.
 */
public class WritableTestRoot {

    static byte[] ser(Writable writable) throws IOException {
        DataOutputBuffer out = new DataOutputBuffer();
        writable.write(out);
        byte[] results = Arrays.copyOfRange(out.getData(), 0, out.getLength());
        return results;
    }

    static byte[] ser(WritableRawComparable writable) throws IOException {
        DataOutputBuffer out = new DataOutputBuffer();
        writable.write(out);
        assertThat(writable.getSizeInBytes(out.getData(), 0), is(out.getLength()));
        byte[] results = Arrays.copyOfRange(out.getData(), 0, out.getLength());
        return results;
    }

    static <T extends Writable> T des(T writable, byte[] serialized) throws IOException {
        DataInputBuffer buf = new DataInputBuffer();
        buf.reset(serialized, serialized.length);
        writable.readFields(buf);
        return writable;
    }

    static int cmp(WritableRawComparable a, WritableRawComparable b) throws IOException {
        int cmp = a.compareTo(b);
        assertThat(a.equals(b), is(cmp == 0));
        if (cmp == 0) {
            assertThat(a.hashCode(), is(b.hashCode()));
        }
        byte[] serA = ser(a);
        byte[] serB = ser(b);
        int serCmp = a.compareInBytes(serA, 0, serB, 0);
        assertThat(serCmp, is(cmp));
        return cmp;
    }

    static int cmp(WritableRawComparable a, WritableRawComparable b, WritableComparator comp) throws IOException {
        int cmp = comp.compare(a, b);
        byte[] serA = ser(a);
        byte[] serB = ser(b);
        int serCmp = comp.compare(serA, 0, serA.length, serB, 0, serB.length);
        assertThat(serCmp, is(cmp));
        return cmp;
    }
}
