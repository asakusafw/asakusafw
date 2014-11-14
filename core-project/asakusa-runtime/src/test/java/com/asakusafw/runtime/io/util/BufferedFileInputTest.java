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

import java.io.EOFException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.junit.Test;

/**
 * Test for {@link BufferedFileInput}.
 */
public class BufferedFileInputTest extends BufferedFileTestRoot {

    /**
     * Test method for {@link BufferedFileInput#readFully(byte[])}.
     * @throws Exception if failed
     */
    @Test
    public void testReadFully_all() throws Exception {
        RandomAccessFile file = file();
        byte[] data = range(0, 256);
        file.write(data);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 1024));
        byte[] b = new byte[data.length];
        buf.readFully(b);
        assertThat(b, is(data));
        assertThat(buf.getPosition(), is((long) data.length));
    }

    /**
     * Test method for {@link BufferedFileInput#readFully(byte[], int, int)}.
     * @throws Exception if failed
     */
    @Test
    public void testReadFully_slice() throws Exception {
        RandomAccessFile file = file();

        byte[] data = range(0, 256);
        file.write(data);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 1024));
        byte[] b = new byte[data.length];
        buf.readFully(b, 1, 100);
        assertThat(Arrays.copyOfRange(b, 1, 101), is(range(0, 99)));
        assertThat(buf.getPosition(), is(100L));
    }

    /**
     * Test method for {@link BufferedFileInput#readFully(byte[], int, int)}.
     * @throws Exception if failed
     */
    @Test(expected = EOFException.class)
    public void testReadFully_over() throws Exception {
        RandomAccessFile file = file();

        byte[] data = range(0, 256);
        file.write(data);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 1024));
        byte[] b = new byte[data.length + 1];
        buf.readFully(b, 0, b.length);
    }

    /**
     * Test method for {@link BufferedFileInput#skipBytes(int)}.
     * @throws Exception if failed
     */
    @Test
    public void testSkipBytes() throws Exception {
        RandomAccessFile file = file();

        byte[] data = range(0, 1023);
        file.write(data);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        buf.readByte();
        buf.seek(0);
        assertThat(buf.skipBytes(10), is(10));
        assertThat(buf.getPosition(), is(10L));

        assertThat(buf.skipBytes(300), is(300));
        assertThat(buf.getPosition(), is(310L));

        assertThat(buf.readByte(), is((byte) 310));
    }

    /**
     * Test method for {@link BufferedFileInput#readBoolean()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadBoolean() throws Exception {
        RandomAccessFile file = file();
        file.writeBoolean(false);
        file.writeBoolean(true);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readBoolean(), is(false));
        assertThat(buf.readBoolean(), is(true));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readByte()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadByte() throws Exception {
        RandomAccessFile file = file();
        byte[] data = range(0, 256);
        file.write(data);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        for (int i = 0; i < data.length; i++) {
            assertThat(buf.readByte(), is((byte) i));
            assertThat(buf.getPosition(), is((long) i + 1));
        }
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readShort()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadShort() throws Exception {
        RandomAccessFile file = file();
        file.writeShort(100);
        file.writeShort(-100);
        file.writeShort(0x1234);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readShort(), is((short) 100));
        assertThat(buf.readShort(), is((short) -100));
        assertThat(buf.readShort(), is((short) 0x1234));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readInt()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadInt() throws Exception {
        RandomAccessFile file = file();
        file.writeInt(100);
        file.writeInt(-100);
        file.writeInt(0x12345678);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readInt(), is(100));
        assertThat(buf.readInt(), is(-100));
        assertThat(buf.readInt(), is(0x12345678));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readLong()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadLong() throws Exception {
        RandomAccessFile file = file();
        file.writeLong(100);
        file.writeLong(-100);
        file.writeLong(0x123456789abcdef0L);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readLong(), is(100L));
        assertThat(buf.readLong(), is(-100L));
        assertThat(buf.readLong(), is(0x123456789abcdef0L));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readFloat()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadFloat() throws Exception {
        RandomAccessFile file = file();
        file.writeFloat(100f);
        file.writeFloat(-100f);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readFloat(), is(100f));
        assertThat(buf.readFloat(), is(-100f));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readDouble()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadDouble() throws Exception {
        RandomAccessFile file = file();
        file.writeDouble(100d);
        file.writeDouble(-100d);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readDouble(), is(100d));
        assertThat(buf.readDouble(), is(-100d));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readUnsignedByte()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadUnsignedByte() throws Exception {
        RandomAccessFile file = file();
        file.writeByte(100);
        file.writeByte(200);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readUnsignedByte(), is(100));
        assertThat(buf.readUnsignedByte(), is(200));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readUnsignedShort()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadUnsignedShort() throws Exception {
        RandomAccessFile file = file();
        file.writeShort(100);
        file.writeShort(40000);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readUnsignedShort(), is(100));
        assertThat(buf.readUnsignedShort(), is(40000));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readChar()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadChar() throws Exception {
        RandomAccessFile file = file();
        file.writeChar('a');
        file.writeChar(0xa000);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readChar(), is('a'));
        assertThat(buf.readChar(), is((char) 0xa000));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readUTF()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadUTF() throws Exception {
        RandomAccessFile file = file();
        file.writeUTF("Hello, world!");
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readUTF(), is("Hello, world!"));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readUTF()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadUTF_codes() throws Exception {
        String code = "\u0000-\u0001:\u0080-\u07ff:\u0800-\uffff";
        RandomAccessFile file = file();
        file.writeUTF(code);
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readUTF(), is(code));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#readUTF()}.
     * @throws Exception if failed
     */
    @Test
    public void testReadUTF_empty() throws Exception {
        RandomAccessFile file = file();
        file.writeUTF("");
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.readUTF(), is(""));
        eof(buf);
    }

    /**
     * Test method for {@link BufferedFileInput#sync()}.
     * @throws Exception if failed
     */
    @Test
    public void testSync() throws Exception {
        RandomAccessFile file = file();
        file.write(bytes(1, 2, 3, 4, 5));
        file.seek(0);
        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));

        assertThat(buf.readByte(), is((byte) 1));
        assertThat(buf.getPosition(), is(1L));

        file.seek(3);
        buf.sync();
        assertThat(buf.getPosition(), is(3L));
        assertThat(buf.readByte(), is((byte) 4));
    }

    /**
     * Test method for {@link BufferedFileInput#seek(long)}.
     * @throws Exception if failed
     */
    @Test
    public void testSeek() throws Exception {
        RandomAccessFile file = file();
        file.write(range(0, 1023));
        file.seek(0);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        buf.seek(100);
        assertThat(buf.getPosition(), is(100L));
        assertThat(buf.readByte(), is((byte) 100));

        // in-buffer
        buf.seek(200);
        assertThat(buf.getPosition(), is(200L));
        assertThat(buf.readByte(), is((byte) 200));

        // out-of-buffer
        buf.seek(300);
        assertThat(buf.getPosition(), is(300L));
        assertThat(buf.readByte(), is((byte) 300));
    }

    /**
     * Test method for {@link BufferedFileInput#getSize()}.
     * @throws Exception if failed
     */
    @Test
    public void testGetSize() throws Exception {
        RandomAccessFile file = file();
        byte[] data = range(0, 256);
        file.write(data);

        BufferedFileInput buf = manage(new BufferedFileInput(file, 256));
        assertThat(buf.getSize(), is(file.length()));
    }
}
