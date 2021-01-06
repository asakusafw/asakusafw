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
package com.asakusafw.info.value;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.asakusafw.info.InfoSerDe;

/**
 * Test for {@link ValueInfo}.
 */
public class ValueInfoTest {

    /**
     * Test information collector.
     */
    @Rule
    public final TestWatcher collector = new TestWatcher() {
        @Override
        protected void starting(org.junit.runner.Description d) {
            ValueInfoTest.this.description = d;
        }
    };

    Description description;

    /**
     * test for {@code null} values.
     */
    @Test
    public void null_json() {
        checkRestore(NullInfo.get());
    }

    /**
     * test for {@code unknown} values.
     */
    @Test
    public void unknown_json() {
        ValueInfo restored = InfoSerDe.deserialize(ValueInfo.class,
                InfoSerDe.serialize(ValueInfo.class, UnknownInfo.of(new Object())));
        assertThat(restored, is(instanceOf(UnknownInfo.class)));
        assertThat(((UnknownInfo) restored).getDeclaringClass(), is(ClassInfo.of(Object.class)));
    }

    /**
     * test for {@code boolean} values.
     */
    @Test
    public void boolean_json() {
        checkRestore(BooleanInfo.of(false));
        checkRestore(BooleanInfo.of(true));
    }

    /**
     * test for {@code byte} values.
     */
    @Test
    public void byte_json() {
        checkRestore(ByteInfo.of((byte) 100));
    }

    /**
     * test for {@code short} values.
     */
    @Test
    public void short_json() {
        checkRestore(ShortInfo.of((short) 100));
    }

    /**
     * test for {@code int} values.
     */
    @Test
    public void int_json() {
        checkRestore(IntInfo.of(100));
    }

    /**
     * test for {@code long} values.
     */
    @Test
    public void long_json() {
        checkRestore(LongInfo.of(100));
    }

    /**
     * test for {@code float} values.
     */
    @Test
    public void float_json() {
        checkRestore(FloatInfo.of(100));
    }

    /**
     * test for {@code double} values.
     */
    @Test
    public void double_json() {
        checkRestore(DoubleInfo.of(100));
    }

    /**
     * test for {@code char} values.
     */
    @Test
    public void chare_json() {
        checkRestore(CharInfo.of('A'));
    }

    /**
     * test for {@code String} values.
     */
    @Test
    public void string_json() {
        checkRestore(StringInfo.of("Hello, world!"));
    }

    /**
     * test for {@code Class} values.
     */
    @Test
    public void class_json() {
        checkRestore(ClassInfo.of(Thread.class));
    }

    /**
     * test for {@code List} values.
     */
    @Test
    public void list_json() {
        checkRestore(ListInfo.of(Arrays.asList(
                IntInfo.of(1),
                LongInfo.of(2),
                StringInfo.of("3"))));
    }

    /**
     * test for {@code Enum} values.
     */
    @Test
    public void enum_json() {
        checkRestore(EnumInfo.of(ValueInfo.Kind.UNKNOWN));
    }

    /**
     * test for {@code Annotation} values.
     */
    @Test
    public void annotation_json() {
        Map<String, ValueInfo> elements = new LinkedHashMap<>();
        elements.put("expected", ClassInfo.of(IllegalAccessException.class));
        elements.put("timeout", LongInfo.of(0));
        checkRestore(AnnotationInfo.of(ClassInfo.of(Test.class), elements));
    }

    /**
     * test for {@code null} values.
     */
    @Test
    public void null_convert() {
        assertThat(ValueInfo.of(null), is(NullInfo.get()));
    }

    /**
     * test for {@code unknown} values.
     */
    @Test
    public void unknown_convert() {
        ValueInfo info = ValueInfo.of(new Object());
        assertThat(info, is(instanceOf(UnknownInfo.class)));
        assertThat(((UnknownInfo) info).getDeclaringClass(), is(ClassInfo.of(Object.class)));
    }

    /**
     * test for {@code boolean} values.
     */
    @Test
    public void boolean_convert() {
        assertThat(ValueInfo.of(true), is(BooleanInfo.of(true)));
    }

    /**
     * test for {@code byte} values.
     */
    @Test
    public void byte_convert() {
        assertThat(ValueInfo.of((byte) 100), is(ByteInfo.of((byte) 100)));
    }

    /**
     * test for {@code short} values.
     */
    @Test
    public void short_convert() {
        assertThat(ValueInfo.of((short) 100), is(ShortInfo.of((short) 100)));
    }

    /**
     * test for {@code int} values.
     */
    @Test
    public void int_convert() {
        assertThat(ValueInfo.of(100), is(IntInfo.of(100)));
    }

    /**
     * test for {@code long} values.
     */
    @Test
    public void long_convert() {
        assertThat(ValueInfo.of((long) 100), is(LongInfo.of(100)));
    }

    /**
     * test for {@code float} values.
     */
    @Test
    public void float_convert() {
        assertThat(ValueInfo.of((float) 100), is(FloatInfo.of(100)));
    }

    /**
     * test for {@code double} values.
     */
    @Test
    public void double_convert() {
        assertThat(ValueInfo.of((double) 100), is(DoubleInfo.of(100)));
    }

    /**
     * test for {@code char} values.
     */
    @Test
    public void char_convert() {
        assertThat(ValueInfo.of('A'), is(CharInfo.of('A')));
    }

    /**
     * test for {@code String} values.
     */
    @Test
    public void string_convert() {
        assertThat(ValueInfo.of("Hello, world!"), is(StringInfo.of("Hello, world!")));
    }

    /**
     * test for {@code Class} values.
     */
    @Test
    public void class_convert() {
        assertThat(ValueInfo.of(System.class), is(ClassInfo.of(System.class)));
    }

    /**
     * test for {@code Class} values.
     */
    @Test
    public void class_primitive() {
        assertThat(ClassInfo.of(int.class).getName(), is("int"));
    }

    /**
     * test for {@code Class} values.
     */
    @Test
    public void class_array() {
        assertThat(ClassInfo.of(String[][].class).getName(), is("java.lang.String[][]"));
    }

    /**
     * test for {@code List} values.
     */
    @Test
    public void list_convert_array() {
        assertThat(
                ValueInfo.of(new int[] { 1, 2, 3 }),
                is(ListInfo.of(IntInfo.of(1), IntInfo.of(2), IntInfo.of(3))));
    }

    /**
     * test for {@code List} values.
     */
    @Test
    public void list_convert_list() {
        assertThat(
                ValueInfo.of(Arrays.asList(1, 2, 3)),
                is(ListInfo.of(IntInfo.of(1), IntInfo.of(2), IntInfo.of(3))));
    }

    /**
     * test for {@code Enum} values.
     */
    @Test
    public void enum_convert() {
        assertThat(ValueInfo.of(ValueInfo.Kind.UNKNOWN), is(EnumInfo.of(ValueInfo.Kind.UNKNOWN)));
    }

    /**
     * test for {@code Annotation} values.
     */
    @Test
    public void annotation_convert() {
        Test annotation = description.getAnnotation(Test.class);
        AnnotationInfo info = (AnnotationInfo) ValueInfo.of(annotation);

        assertThat(info.getObject(), info.getDeclaringClass(), is(ClassInfo.of(Test.class)));
        assertThat(info.getElements(), hasEntry("expected", ClassInfo.of(annotation.expected())));
        assertThat(info.getElements(), hasEntry("timeout", LongInfo.of(annotation.timeout())));
    }

    private static void checkRestore(ValueInfo info) {
        InfoSerDe.checkRestore(ValueInfo.class, info);
        InfoSerDe.checkRestore(info.getClass(), info);
    }
}
