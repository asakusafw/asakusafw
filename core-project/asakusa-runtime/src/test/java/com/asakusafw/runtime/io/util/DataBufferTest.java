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
package com.asakusafw.runtime.io.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.DataInput;
import java.util.Arrays;

import org.junit.Test;

/**
 * Test for {@link DataBuffer}.
 */
public class DataBufferTest {

    /**
     * test for primitives.
     * @throws Exception if failed
     */
    @Test
    public void io_simple() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.write(0);
        buffer.write(100);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(2));

        assertThat(buffer.read(), is(0));
        assertThat(buffer.read(), is(100));
        assertThat(buffer.getReadPosition(), is(2));
        assertThat(buffer.getReadRemaining(), is(0));

        assertThat(buffer.read(), is(-1));
        assertThat(buffer.getReadPosition(), is(2));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for booleans.
     * @throws Exception if failed
     */
    @Test
    public void io_boolean() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeBoolean(false);
        buffer.writeBoolean(true);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(2));

        assertThat(buffer.readBoolean(), is(false));
        assertThat(buffer.readBoolean(), is(true));
        assertThat(buffer.getReadPosition(), is(2));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for bytes.
     * @throws Exception if failed
     */
    @Test
    public void io_byte() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeByte(0);
        buffer.writeByte(100);
        buffer.writeByte(-100);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(3));

        assertThat(buffer.readByte(), is((byte) 0));
        assertThat(buffer.readByte(), is((byte) 100));
        assertThat(buffer.readByte(), is((byte) -100));
        assertThat(buffer.getReadPosition(), is(3));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for unsigned bytes.
     * @throws Exception if failed
     */
    @Test
    public void io_ubyte() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeByte(0);
        buffer.writeByte(100);
        buffer.writeByte(-100);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(3));

        assertThat(buffer.readUnsignedByte(), is(0));
        assertThat(buffer.readUnsignedByte(), is(100));
        assertThat(buffer.readUnsignedByte(), is(-100 & 0xff));
        assertThat(buffer.getReadPosition(), is(3));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for shorts.
     * @throws Exception if failed
     */
    @Test
    public void io_short() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeShort(0);
        buffer.writeShort(100);
        buffer.writeShort(-100);
        buffer.writeShort(0x1234);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(8));

        assertThat(buffer.readShort(), is((short) 0));
        assertThat(buffer.readShort(), is((short) 100));
        assertThat(buffer.readShort(), is((short) -100));
        assertThat(buffer.readShort(), is((short) 0x1234));
        assertThat(buffer.getReadPosition(), is(8));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for unsigned shorts.
     * @throws Exception if failed
     */
    @Test
    public void io_uhort() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeShort(0);
        buffer.writeShort(100);
        buffer.writeShort(-100);
        buffer.writeShort(0x1234);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(8));

        assertThat(buffer.readUnsignedShort(), is(0));
        assertThat(buffer.readUnsignedShort(), is(100));
        assertThat(buffer.readUnsignedShort(), is(-100 & 0xffff));
        assertThat(buffer.readUnsignedShort(), is(0x1234));
        assertThat(buffer.getReadPosition(), is(8));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for chars.
     * @throws Exception if failed
     */
    @Test
    public void io_char() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeChar(0);
        buffer.writeChar(100);
        buffer.writeChar(-100);
        buffer.writeChar(0x1234);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(8));

        assertThat(buffer.readChar(), is((char) 0));
        assertThat(buffer.readChar(), is((char) 100));
        assertThat(buffer.readChar(), is((char) -100));
        assertThat(buffer.readChar(), is((char) 0x1234));
        assertThat(buffer.getReadPosition(), is(8));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for integers.
     * @throws Exception if failed
     */
    @Test
    public void io_int() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeInt(0);
        buffer.writeInt(100);
        buffer.writeInt(-100);
        buffer.writeInt(0x12345678);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(16));

        assertThat(buffer.readInt(), is(0));
        assertThat(buffer.readInt(), is(100));
        assertThat(buffer.readInt(), is(-100));
        assertThat(buffer.readInt(), is(0x12345678));
        assertThat(buffer.getReadPosition(), is(16));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for longs.
     * @throws Exception if failed
     */
    @Test
    public void io_long() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeLong(0);
        buffer.writeLong(100);
        buffer.writeLong(-100);
        buffer.writeLong(0x0123456789abcdefL);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(32));

        assertThat(buffer.readLong(), is(0L));
        assertThat(buffer.readLong(), is(100L));
        assertThat(buffer.readLong(), is(-100L));
        assertThat(buffer.readLong(), is(0x0123456789abcdefL));
        assertThat(buffer.getReadPosition(), is(32));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for floats.
     * @throws Exception if failed
     */
    @Test
    public void io_float() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeFloat(0);
        buffer.writeFloat(100);
        buffer.writeFloat(-100);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(12));

        assertThat(buffer.readFloat(), is(0f));
        assertThat(buffer.readFloat(), is(100f));
        assertThat(buffer.readFloat(), is(-100f));
        assertThat(buffer.getReadPosition(), is(12));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for doubles.
     * @throws Exception if failed
     */
    @Test
    public void io_double() throws Exception {
        DataBuffer buffer = new DataBuffer();
        buffer.writeDouble(0);
        buffer.writeDouble(100);
        buffer.writeDouble(-100);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(24));

        assertThat(buffer.readDouble(), is(0d));
        assertThat(buffer.readDouble(), is(100d));
        assertThat(buffer.readDouble(), is(-100d));
        assertThat(buffer.getReadPosition(), is(24));
        assertThat(buffer.getReadRemaining(), is(0));
    }

    /**
     * test for byte arrays.
     * @throws Exception if failed
     */
    @Test
    public void io_byte_array() throws Exception {
        byte[] b1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        DataBuffer buffer = new DataBuffer();
        buffer.write(b1, 1, 8);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(8));

        byte[] b2 = new byte[b1.length];
        buffer.readFully(b2, 1, 8);
        assertThat(buffer.getReadPosition(), is(8));
        assertThat(buffer.getReadRemaining(), is(0));

        assertThat(Arrays.copyOfRange(b2, 1, 9), equalTo(Arrays.copyOfRange(b1, 1, 9)));
    }

    /**
     * test for {@link DataInput}.
     * @throws Exception if failed
     */
    @Test
    public void io_DataInput() throws Exception {
        byte[] b1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        DataBuffer source = new DataBuffer();
        source.reset(b1, 0, b1.length);

        DataBuffer buffer = new DataBuffer();
        buffer.write(source, b1.length);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(10));

        assertThat(Arrays.copyOfRange(buffer.getData(), 0, 10), equalTo(b1));
    }

    /**
     * test for skip.
     * @throws Exception if failed
     */
    @Test
    public void skip() throws Exception {
        DataBuffer buffer = new DataBuffer();
        byte[] b1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        buffer.reset(b1, 0, b1.length);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(10));

        assertThat(buffer.skipBytes(0), is(0));
        assertThat(buffer.getReadPosition(), is(0));

        assertThat(buffer.skipBytes(1), is(1));
        assertThat(buffer.getReadPosition(), is(1));

        assertThat(buffer.skipBytes(2), is(2));
        assertThat(buffer.getReadPosition(), is(3));

        assertThat(buffer.skipBytes(10), is(7));
        assertThat(buffer.getReadPosition(), is(10));
    }

    /**
     * test for reset cursor.
     * @throws Exception if failed
     */
    @Test
    public void reset() throws Exception {
        DataBuffer buffer = new DataBuffer();
        byte[] b1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        buffer.reset(b1, 0, b1.length);
        assertThat(buffer.getReadPosition(), is(0));
        assertThat(buffer.getReadRemaining(), is(10));

        buffer.reset(1, 8);
        assertThat(buffer.getReadPosition(), is(1));
        assertThat(buffer.getReadRemaining(), is(8));
    }

    /**
     * test for expand.
     * @throws Exception if failed
     */
    @Test
    public void expand() throws Exception {
        DataBuffer buffer = new DataBuffer();
        byte[] b1 = { 0, 1, 2, 3 };
        buffer.reset(b1, 1, b1.length - 1);
        assertThat(buffer.getReadPosition(), is(1));
        assertThat(buffer.getReadRemaining(), is(3));

        buffer.ensureCapacity(10);
        assertThat(buffer.getData().length, greaterThanOrEqualTo(10));
        assertThat(buffer.getReadPosition(), is(1));
        assertThat(buffer.getReadRemaining(), is(3));

        assertThat(buffer.read(), is(1));
        assertThat(buffer.read(), is(2));
        assertThat(buffer.read(), is(3));
    }
}
