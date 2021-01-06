/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.mapreduce.simple;

/**
 * Byte array slice of key-value pair.
 * @since 0.7.1
 */
public class KeyValueSlice {

    private static final byte[] EMPTY = new byte[0];

    private byte[] bytes = EMPTY;

    private int offset;

    private int keyLength;

    private int valueLength;

    /**
     * Sets slice range.
     * @param newBytes the new byte array
     * @param newOffset the new offset in the specified byte array
     * @param newKeyLength the key length
     * @param newValueLength the value length
     */
    public void set(byte[] newBytes, int newOffset, int newKeyLength, int newValueLength) {
        this.bytes = newBytes;
        set(newOffset, newKeyLength, newValueLength);
    }

    /**
     * Sets slice range.
     * @param newOffset the new offset in current byte array
     * @param newKeyLength the key length
     * @param newValueLength the value length
     */
    public void set(int newOffset, int newKeyLength, int newValueLength) {
        this.offset = newOffset;
        this.keyLength = newKeyLength;
        this.valueLength = newValueLength;
    }

    /**
     * Returns the byte array in this slice.
     * @return the byte array
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Returns the key offset in {@link #getBytes()}.
     * @return the key offset
     */
    public int getKeyOffset() {
        return offset + 0;
    }

    /**
     * Returns the value offset in {@link #getBytes()}.
     * @return the value offset
     */
    public int getValueOffset() {
        return offset + keyLength;
    }

    /**
     * Returns the slice offset in {@code #getBytes()}.
     * @return the slice offset
     * @see #getKeyOffset()
     * @see #getValueOffset()
     */
    public int getSliceOffset() {
        return offset;
    }

    /**
     * Returns the key length.
     * @return the key length
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * Returns the value length.
     * @return the value length
     */
    public int getValueLength() {
        return valueLength;
    }

    /**
     * Returns the slice length.
     * @return the slice length
     */
    public int getSliceLength() {
        return keyLength + valueLength;
    }
}
