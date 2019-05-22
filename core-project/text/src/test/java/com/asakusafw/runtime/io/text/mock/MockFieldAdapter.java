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
package com.asakusafw.runtime.io.text.mock;

import java.util.function.Supplier;

import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.io.text.driver.FieldOutput;

/**
 * Mock {@link FieldAdapter}.
 */
public class MockFieldAdapter implements FieldAdapter<String[]> {

    private final int index;

    /**
     * Creates a new instance.
     * @param index the element index
     */
    public MockFieldAdapter(int index) {
        this.index = index;
    }

    /**
     * Creates a new instance.
     * @param index the element index
     * @return the created instance
     */
    public static MockFieldAdapter of(int index) {
        return new MockFieldAdapter(index);
    }

    /**
     * Returns a supplier.
     * @param index the element index
     * @return the created supplier
     */
    public static Supplier<MockFieldAdapter> supplier(int index) {
        return () -> new MockFieldAdapter(index);
    }

    @Override
    public void clear(String[] property) {
        property[index] = null;
    }

    @Override
    public void parse(CharSequence contents, String[] property) {
        property[index] = contents == null ? null : contents.toString();
    }

    @Override
    public void emit(String[] property, FieldOutput output) {
        String s = property[index];
        if (s == null) {
            output.putNull();
        } else {
            output.put(s);
        }
    }
}
