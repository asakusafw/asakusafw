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
package com.asakusafw.runtime.stage.temporary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.hadoop.io.IOUtils;

/**
 * Utilities for temporary files.
 * @since 0.7.0
 */
public final class TemporaryFile {

    /**
     * The block size.
     */
    public static final int BLOCK_SIZE = 64 * 1024 * 1024;

    /**
     * The page header size.
     */
    public static final int PAGE_HEADER_SIZE = 3;

    /**
     * The page header value which represents {@code END_OF_BLOCK}.
     */
    public static final int PAGE_HEADER_EOB = 0x000000;

    /**
     * The page header value which represents {@code END_OF_FILE}.
     */
    public static final int PAGE_HEADER_EOF = -1;

    /**
     * The padding byte for empty entries.
     */
    public static final int EMPTY_ENTRY_PADDING = 0;

    private static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    /**
     * The maximum page size.
     */
    public static final int MAX_PAGE_SIZE = 0xffffff;

    private static final byte[] BLOCK_HEADER = { '`', 'A', 'F', '@' };

    private static final int MAJOR_VERSION = 1;

    private static final ThreadLocal<byte[]> HEADER_BUFFER = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[PAGE_HEADER_SIZE];
        }
    };

    private static final ThreadLocal<byte[]> INSTANT_BUFFER = new ThreadLocal<byte[]>();

    static byte[] getInstantBuffer(int minSize) {
        byte[] buffer = INSTANT_BUFFER.get();
        if (buffer == null || buffer.length < minSize) {
            int size = (int) (minSize * 1.2);
            buffer = new byte[size];
            INSTANT_BUFFER.set(buffer);
        }
        return buffer;
    }

    /**
     * Writes a block header.
     * @param output the target output
     * @return the bytes written
     * @throws IOException if failed to write
     */
    public static int writeBlockHeader(OutputStream output) throws IOException {
        output.write(BLOCK_HEADER);
        output.write(MAJOR_VERSION);
        return BLOCK_HEADER.length + 1;
    }

    /**
     * Reads and verifies the block header.
     * @param input the target input
     * @return the bytes read
     * @throws IOException if failed to read
     */
    public static int readBlockHeader(InputStream input) throws IOException {
        byte[] header = new byte[BLOCK_HEADER.length];
        int offset = 0;
        while (offset < header.length) {
            int read = input.read(header, offset, header.length - offset);
            if (read < 0) {
                return PAGE_HEADER_EOF;
            }
            offset += read;
        }
        if (Arrays.equals(header, BLOCK_HEADER) == false) {
            throw new IOException("Unsupported temporary file format (invalid block header)");
        }
        int version = input.read();
        if (version < 0) {
            return PAGE_HEADER_EOF;
        }
        if (version != MAJOR_VERSION) {
            throw new IOException(MessageFormat.format(
                    "Unsupported temporary file format (inconsistent version): file={0}, API={1}",
                    version,
                    MAJOR_VERSION));
        }
        return BLOCK_HEADER.length + 1;
    }

    /**
     * Writes a string.
     * @param output the target output
     * @param string the contents
     * @return the bytes written
     * @throws IOException if failed to write
     */
    public static int writeString(OutputStream output, String string) throws IOException {
        byte[] bytes = string.getBytes(ENCODING);
        int length = bytes.length;
        output.write((length >> 24) & 0xff);
        output.write((length >> 16) & 0xff);
        output.write((length >>  8) & 0xff);
        output.write((length >>  0) & 0xff);
        output.write(bytes);
        return 4 + bytes.length;
    }

    /**
     * Reads a string into appendable.
     * @param input the target input
     * @param appendable the target appendable
     * @return the bytes read
     * @throws IOException if failed to read
     */
    public static int readString(InputStream input, Appendable appendable) throws IOException {
        int length = 0;
        for (int i = 0; i < 4; i++) {
            int c = input.read();
            if (c < 0) {
                return -1;
            }
            length = length << 8 | c;
        }
        byte[] bytes = new byte[length];
        IOUtils.readFully(input, bytes, 0, length);
        appendable.append(new String(bytes, ENCODING));
        return 4 + bytes.length;
    }

    /**
     * Returns whether clients can write a content page into the current block.
     * @param positionInBlock the byte position in the current block
     * @param length the content length in bytes
     * @return {@code true} if clients can write a content page into the current block, otherwise {@code false}
     */
    public static boolean canWritePage(int positionInBlock, int length) {
        int blockRest = BLOCK_SIZE - positionInBlock;
        // page-header + page-contents + next-page-header
        return length + (PAGE_HEADER_SIZE * 2) <= blockRest;
    }

    /**
     * Writes a content page header.
     * @param output the target output stream
     * @param length the content length
     * @throws IOException if failed to write a page header by I/O error
     */
    public static void writeContentPageMark(OutputStream output, int length) throws IOException {
        if (length <= 0 || length > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The content page is too large: {1} (> {0})",
                    MAX_PAGE_SIZE,
                    length));
        }
        output.write(getHeader(length));
    }

    /**
     * Writes an end-of-block header.
     * @param output the target output stream
     * @throws IOException if failed to write a page header by I/O error
     */
    public static void writeEndOfBlockMark(OutputStream output) throws IOException {
        output.write(getHeader(PAGE_HEADER_EOB));
    }

    private static byte[] getHeader(int value) {
        byte[] header = HEADER_BUFFER.get();
        header[0] = (byte) ((value >> 16) & 0xff);
        header[1] = (byte) ((value >>  8) & 0xff);
        header[2] = (byte) ((value >>  0) & 0xff);
        return header;
    }

    /**
     * Reads a page header from the head of the stream.
     * @param input the target input stream
     * @return the page header
     * @throws IOException if failed to read a page header
     * @see #MAX_PAGE_SIZE
     * @see #PAGE_HEADER_EOB
     * @see #PAGE_HEADER_EOF
     */
    public static int readPageHeader(InputStream input) throws IOException {
        byte[] header = HEADER_BUFFER.get();
        int offset = 0;
        while (offset < header.length) {
            int read = input.read(header, offset, header.length - offset);
            if (read < 0) {
                return PAGE_HEADER_EOF;
            }
            offset += read;
        }
        int value = 0;
        value |= (header[0] & 0xff) << 16;
        value |= (header[1] & 0xff) <<  8;
        value |= (header[2] & 0xff) <<  0;
        return value;
    }

    private TemporaryFile() {
        return;
    }
}
