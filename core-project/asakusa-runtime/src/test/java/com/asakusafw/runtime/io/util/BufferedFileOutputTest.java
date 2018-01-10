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
package com.asakusafw.runtime.io.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Test for throws Exception {@link BufferedFileOutput}.
 */
public class BufferedFileOutputTest extends BufferedFileTestRoot {

    /**
     * Test method for throws Exception {@link BufferedFileOutput#write(byte[])}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteByteArray() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.write(range(0, 255));
        assertThat(buf.getPosition(), is(256L));
        buf.flush();
        assertThat(buf.getPosition(), is(256L));

        file.seek(0);
        byte[] b = new byte[256];
        file.readFully(b);
        assertThat(b, is(range(0, 255)));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#write(byte[], int, int)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteByteArray_range() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.write(range(0, 255), 1, 200);
        assertThat(buf.getPosition(), is(200L));
        buf.flush();
        assertThat(buf.getPosition(), is(200L));

        file.seek(0);
        byte[] b = new byte[200];
        file.readFully(b);
        assertThat(b, is(range(1, 200)));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeBoolean(boolean)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteBoolean() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeBoolean(false);
        buf.writeBoolean(true);
        buf.flush();

        file.seek(0);
        assertThat(file.readBoolean(), is(false));
        assertThat(file.readBoolean(), is(true));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#write(int)}.
     * @throws Exception if failed
     */
    @Test
    public void testWrite() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        for (int i = 0; i < 256; i++) {
            buf.write(i);
            assertThat(buf.getPosition(), is(i + 1L));
            assertThat(buf.getSize(), is(i + 1L));
        }
        buf.flush();

        file.seek(0);
        for (int i = 0; i < 256; i++) {
            assertThat(file.readByte(), is((byte) i));
        }

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeByte(int)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteByte() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeByte(100);
        buf.writeByte(-100);
        buf.flush();

        file.seek(0);
        assertThat(file.readByte(), is((byte) 100));
        assertThat(file.readByte(), is((byte) -100));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeShort(int)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteShort() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeShort(100);
        buf.writeShort(-100);
        buf.writeShort(0x1234);
        buf.flush();

        file.seek(0);
        assertThat(file.readShort(), is((short) 100));
        assertThat(file.readShort(), is((short) -100));
        assertThat(file.readShort(), is((short) 0x1234));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeInt(int)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteInt() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeInt(100);
        buf.writeInt(-100);
        buf.writeInt(0x12345678);
        buf.flush();

        file.seek(0);
        assertThat(file.readInt(), is(100));
        assertThat(file.readInt(), is(-100));
        assertThat(file.readInt(), is(0x12345678));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeChar(int)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteChar() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeChar('a');
        buf.writeChar('\ua000');
        buf.flush();

        file.seek(0);
        assertThat(file.readChar(), is('a'));
        assertThat(file.readChar(), is('\ua000'));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeLong(long)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteLong() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeLong(100);
        buf.writeLong(-100);
        buf.writeLong(0x123456789abcdef0L);
        buf.flush();

        file.seek(0);
        assertThat(file.readLong(), is(100L));
        assertThat(file.readLong(), is(-100L));
        assertThat(file.readLong(), is(0x123456789abcdef0L));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeFloat(float)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteFloat() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeFloat(100f);
        buf.writeFloat(-100f);
        buf.flush();

        file.seek(0);
        assertThat(file.readFloat(), is(100f));
        assertThat(file.readFloat(), is(-100f));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeDouble(double)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteDouble() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeDouble(100d);
        buf.writeDouble(-100d);
        buf.flush();

        file.seek(0);
        assertThat(file.readDouble(), is(100d));
        assertThat(file.readDouble(), is(-100d));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeBytes(java.lang.String)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteBytes() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeBytes("Hello, world!");
        buf.flush();

        file.seek(0);
        byte[] expect = "Hello, world!".getBytes(StandardCharsets.US_ASCII);
        byte[] b = new byte[expect.length];
        file.readFully(b);
        assertThat(b, is(expect));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeChars(java.lang.String)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteChars() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeChars("Hello, world!");
        buf.flush();

        file.seek(0);
        String expect = "Hello, world!";
        for (char e : expect.toCharArray()) {
            char c = file.readChar();
            assertThat(c, is(e));
        }

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeUTF(java.lang.String)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteUTF() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeUTF("Hello, world!");
        buf.flush();

        file.seek(0);
        assertThat(file.readUTF(), is("Hello, world!"));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeUTF(java.lang.String)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteUTF_code() throws Exception {
        String code = "\u0000-\u0001:\u0080-\u07ff:\u0800-\uffff";
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeUTF(code);
        buf.flush();

        file.seek(0);
        assertThat(file.readUTF(), is(code));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#writeUTF(java.lang.String)}.
     * @throws Exception if failed
     */
    @Test
    public void testWriteUTF_empty() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));
        buf.writeUTF("");
        buf.flush();

        file.seek(0);
        assertThat(file.readUTF(), is(""));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#sync()}.
     * @throws Exception if failed
     */
    @Test
    public void testSync() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 100));

        buf.sync();
        file.write(1);
        buf.sync();
        assertThat(buf.getPosition(), is(1L));
        buf.write(2);
        buf.flush();

        file.seek(0);
        assertThat(file.readByte(), is((byte) 1));
        assertThat(file.readByte(), is((byte) 2));

        eof(file);
    }

    /**
     * Test method for throws Exception {@link BufferedFileOutput#seek(long)}.
     * @throws Exception if failed
     */
    @Test
    public void testSeek() throws Exception {
        RandomAccessFile file = file();
        BufferedFileOutput buf = manage(new BufferedFileOutput(file, 256));
        buf.write(range(0, 99));
        buf.seek(1);
        assertThat(buf.getPosition(), is(1L));
        buf.write(-1);
        buf.seek(2);
        buf.flush();

        file.seek(0);
        assertThat(file.readByte(), is((byte) 0));
        assertThat(file.readByte(), is((byte) -1));
        for (int i = 2; i < 100; i++) {
            assertThat(file.readByte(), is((byte) i));
        }
    }
}
