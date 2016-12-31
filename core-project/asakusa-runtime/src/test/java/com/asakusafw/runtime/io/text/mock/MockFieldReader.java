/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.mock;

import com.asakusafw.runtime.io.text.FieldReader;

/**
 * Mock {@link FieldReader}.
 */
public class MockFieldReader implements FieldReader {

    private final String[][] fields;

    private int row = -1;

    private int col = -1;

    /**
     * Creates a new instance.
     * @param fields fields
     */
    public MockFieldReader(String[][] fields) {
        this.fields = fields.clone();
    }

    @Override
    public boolean nextRecord() {
        int next = row + 1;
        if (next < 0 || next >= fields.length) {
            row = Integer.MIN_VALUE;
            col = -1;
            return false;
        } else {
            row = next;
            col = -1;
            return true;
        }
    }

    @Override
    public boolean nextField() {
        if (row < 0) {
            throw new IllegalStateException();
        }
        int next = col + 1;
        if (next < 0 || next >= fields[row].length) {
            col = Integer.MIN_VALUE;
            return false;
        } else {
            col = next;
            return true;
        }
    }

    @Override
    public void rewindFields() {
        if (row < 0) {
            throw new IllegalStateException();
        }
        col = -1;
    }

    @Override
    public CharSequence getContent() {
        return fields[row][col];
    }

    @Override
    public long getRecordLineNumber() {
        return getRecordIndex();
    }

    @Override
    public long getRecordIndex() {
        return row < 0 ? -1 : row;
    }

    @Override
    public long getFieldIndex() {
        return col < 0 ? -1 : col;
    }

    @Override
    public void close() {
        return;
    }
}
