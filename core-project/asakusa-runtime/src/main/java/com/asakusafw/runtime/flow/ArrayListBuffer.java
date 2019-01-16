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
package com.asakusafw.runtime.flow;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * Implementation of {@link ListBuffer} using an array.
 * @param <E> element type
 */
public class ArrayListBuffer<E> extends AbstractList<E> implements ListBuffer<E>, RandomAccess {

    private static final int BUFFER_SIZE = 64;

    private Object[] buffer;

    private int size;

    private int cursor;

    private int limit;

    /**
     * Creates a new instance with default buffer size.
     */
    public ArrayListBuffer() {
        this(BUFFER_SIZE);
    }

    /**
     * Creates a new instance with default buffer size.
     * If buffer size is too small, the recommended minimum buffer size is used.
     * @param bufferSize initial buffer size (number of objects)
     */
    public ArrayListBuffer(int bufferSize) {
        this.buffer = new Object[Math.max(bufferSize, BUFFER_SIZE / 2)];
        this.size = 0;
        this.cursor = -1;
        this.limit = 0;
    }

    @Override
    public void begin() {
        size = -1;
        cursor = 0;
        modCount++;
    }

    @Override
    public void end() {
        if (cursor >= 0) {
            size = cursor;
            cursor = -1;
            modCount++;
        }
    }

    /**
     * Returns the current cursor position.
     * This method must be invoked between {@link #begin()} and {@link #end()}, and it will return
     * the number of invoking {@link #advance()} since the last {@link #begin()} was invoked.
     * @return the number of invoking {@link #advance()}
     */
    public int getCursorPosition() {
        return cursor;
    }

    @Override
    public boolean isExpandRequired() {
        return limit <= cursor;
    }

    @Override
    public void expand(E value) {
        expandBuffer(buffer.length << 2);
        buffer[limit++] = value;
    }

    @Override
    public E advance() {
        @SuppressWarnings("unchecked")
        E next = (E) buffer[cursor];
        cursor++;
        return next;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return (E) buffer[index];
    }

    @Override
    public void shrink() {
        return;
    }

    private void expandBuffer(int newLength) {
        if (buffer.length <= limit) {
            Object[] newBuffer = new Object[newLength];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
}
