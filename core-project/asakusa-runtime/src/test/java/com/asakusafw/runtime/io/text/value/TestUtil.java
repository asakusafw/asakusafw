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
package com.asakusafw.runtime.io.text.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.asakusafw.runtime.io.text.driver.BasicFieldOutput;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.io.text.driver.MalformedFieldException;
import com.asakusafw.runtime.value.ValueOption;

final class TestUtil {

    private TestUtil() {
        return;
    }

    static <T extends ValueOption<T>> void equivalent(FieldAdapter<T> adapter, Object value, T buffer) {
        CharSequence cs = toCharSequence(value);
        adapter.parse(cs, buffer);
        checkEmit(adapter, buffer, cs);
    }

    static <T extends ValueOption<T>> void checkParse(FieldAdapter<T> adapter, Object value, T expected) {
        CharSequence cs = toCharSequence(value);
        try {
            @SuppressWarnings("unchecked")
            T sink = (T) expected.getClass().newInstance();
            adapter.parse(cs, sink);
            assertThat(sink, equalTo(expected));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static <T> void checkMalformed(FieldAdapter<T> adapter, Object value, T buffer) {
        CharSequence cs = toCharSequence(value);
        try {
            adapter.parse(cs, buffer);
            fail(String.valueOf(buffer));
        } catch (MalformedFieldException e) {
            // ok.
        }
    }

    private static CharSequence toCharSequence(Object value) {
        return value == null ? null
            : value instanceof CharSequence ? (CharSequence) value
            : String.valueOf(value);
    }

    static <T extends ValueOption<T>> void checkEmit(FieldAdapter<T> adapter, T value, Object expected) {
        BasicFieldOutput output = new BasicFieldOutput();
        adapter.emit(value, output);
        String r = output.get() == null ? null : output.get().toString();
        String e = expected == null ? null : String.valueOf(expected);
        assertThat(r, equalTo(e));
    }
}
