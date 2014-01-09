/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.collector;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link WritableSlot}.
 */
public class WritableSlotTest {

    /**
     * {@link WritableSlot}を経由した{@link Writable}の永続化。
     * @throws Exception テストに失敗した場合
     */
    @SuppressWarnings("deprecation")
    @Test
    public void loadStore() throws Exception {
        WritableSlot slot = new WritableSlot();
        IntOption value = new IntOption(100);
        IntOption copy = new IntOption();

        slot.store(value);
        slot.loadTo(copy);
        assertThat(copy, is(value));

        value.modify(200);
        slot.store(value);
        slot.loadTo(copy);
        assertThat(copy, is(value));
    }

    /**
     * {@link WritableSlot}自体の永続化。
     * @throws Exception テストに失敗した場合
     */
    @SuppressWarnings("deprecation")
    @Test
    public void writable() throws Exception {
        WritableSlot slot = new WritableSlot();
        IntOption value = new IntOption(100);
        IntOption copy = new IntOption(200);

        slot.store(value);
        WritableSlot restored1 = restore(slot);
        restored1.loadTo(copy);
        assertThat(copy, is(value));

        value.modify(200);
        slot.store(value);
        WritableSlot restored2 = restore(slot);
        restored2.loadTo(copy);
        assertThat(copy, is(value));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Writable> T restore(T writable) {
        try {
            return read((T) writable.getClass().newInstance(), write(writable));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static byte[] write(Writable writable) {
        DataOutputBuffer buffer = new DataOutputBuffer();
        buffer.reset();
        try {
            writable.write(buffer);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return Arrays.copyOf(buffer.getData(), buffer.getLength());
    }

    static <T extends Writable> T read(T writable, byte[] bytes) {
        DataInputBuffer buffer = new DataInputBuffer();
        buffer.reset(bytes, bytes.length);
        try {
            writable.readFields(buffer);
            assertThat("Enf of Stream", buffer.read(), is(-1));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return writable;
    }
}
