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
package com.asakusafw.runtime.io.text.driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A basic implementation of {@link FieldOutput}.
 * @since 0.9.1
 */
public class BasicFieldOutput implements FieldOutput {

    private final StringBuilder buffer = new StringBuilder();

    private final List<Option> options = new ArrayList<>();

    private boolean isNull;

    /**
     * Clears the last output.
     * @return this
     */
    public BasicFieldOutput reset() {
        isNull = false;
        buffer.setLength(0);
        options.clear();
        return this;
    }

    /**
     * Clears the last output and set the contents.
     * @param contents the contents (nullable)
     * @return this
     */
    public BasicFieldOutput set(CharSequence contents) {
        reset();
        if (contents == null) {
            putNull();
        } else {
            put(contents);
        }
        return this;
    }

    @Override
    public CharSequence get() {
        return isNull ? null : buffer;
    }

    @Override
    public List<Option> getOptions() {
        return options;
    }

    @Override
    public void putNull() {
        if (buffer.length() != 0) {
            throw new IllegalStateException();
        }
        isNull = true;
    }

    @Override
    public FieldOutput put(CharSequence contents, int start, int end) {
        checkNonNull();
        buffer.append(contents, start, end);
        return this;
    }

    @Override
    public FieldOutput put(CharSequence contents) {
        checkNonNull();
        buffer.append(contents);
        return this;
    }

    @Override
    public FieldOutput addOption(Option option) {
        options.add(option);
        return this;
    }

    @Override
    public FieldOutput addOptions(Option... opts) {
        Collections.addAll(options, opts);
        return this;
    }

    @Override
    public StringBuilder acquireBuffer() {
        if (isNull || buffer.length() != 0) {
            throw new IllegalStateException();
        }
        return buffer;
    }

    @Override
    public void releaseBuffer(StringBuilder acquired) {
        if (acquired != buffer) {
            throw new IllegalArgumentException();
        }
    }

    private void checkNonNull() {
        if (isNull) {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return isNull ? "(null)" : buffer.toString(); //$NON-NLS-1$
    }
}
