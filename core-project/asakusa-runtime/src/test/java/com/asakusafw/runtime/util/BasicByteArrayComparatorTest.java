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
package com.asakusafw.runtime.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link BasicByteArrayComparator}.
 */
public class BasicByteArrayComparatorTest {

    private static final ByteArrayComparator CMP = new BasicByteArrayComparator();

    /**
     * eq - simple case.
     */
    @Test
    public void eq() {
        assertThat(CMP.equals(array(1), 0, 1, array(1), 0, 1), is(true));
        assertThat(CMP.equals(array(1), 0, 1, array(2), 0, 1), is(false));
        assertThat(CMP.equals(array(1, 2), 0, 1, array(1, 3), 0, 1), is(true));
        assertThat(CMP.equals(array(1, 2), 1, 1, array(1, 3), 1, 1), is(false));
        assertThat(CMP.equals(array(1, 2), 0, 1, array(1, 2), 0, 2), is(false));
    }

    /**
     * eq - word.
     */
    @Test
    public void eq_word() {
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8), is(true));
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(0, 1, 2, 3, 4, 5, 6, 7, 8, 0), 1, 8), is(true));
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 0), 0, 8), is(false));
    }

    /**
     * eq - double words.
     */
    @Test
    public void eq_double_word() {
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8, 8, 7, 6, 5, 4, 3, 2, 1), 0, 16,
                array(1, 2, 3, 4, 5, 6, 7, 8, 8, 7, 6, 5, 4, 3, 2, 1), 0, 16), is(true));
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8, 8, 7, 6, 5, 4, 3, 2, 1), 0, 16,
                array(1, 2, 3, 4, 5, 6, 7, 8, 9, 7, 6, 5, 4, 3, 2, 1), 0, 16), is(false));
    }

    /**
     * eq - word + bytes.
     */
    @Test
    public void eq_word_rest() {
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), 0, 10), is(true));
        assertThat(CMP.equals(
                array(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 9, 1), 0, 10), is(false));
    }

    /**
     * cmp - simple case.
     */
    @Test
    public void cmp() {
        assertThat(CMP.compare(array(1), 0, 1, array(1), 0, 1), is(equalTo(0)));
        assertThat(CMP.compare(array(1), 0, 1, array(2), 0, 1), is(lessThan(0)));
        assertThat(CMP.compare(array(1), 0, 1, array(0), 0, 1), is(greaterThan(0)));
        assertThat(CMP.compare(array(1), 0, 1, array(0xff), 0, 1), is(lessThan(0)));

        assertThat(CMP.compare(array(1, 2), 0, 1, array(1, 3), 0, 1), is(equalTo(0)));
        assertThat(CMP.compare(array(1, 2), 1, 1, array(1, 3), 1, 1), is(lessThan(0)));
        assertThat(CMP.compare(array(1, 2), 1, 1, array(1, 1), 1, 1), is(greaterThan(0)));

        assertThat(CMP.compare(array(1, 2), 0, 2, array(1, 2, 0), 0, 3), is(lessThan(0)));
        assertThat(CMP.compare(array(1, 2, 0), 0, 3, array(1, 2), 0, 2), is(greaterThan(0)));
    }

    /**
     * cmp - word.
     */
    @Test
    public void cmp_word() {
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8), is(equalTo(0)));
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(0, 1, 2, 3, 4, 5, 6, 7, 8, 0), 1, 8), is(equalTo(0)));

        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 0), 0, 8), is(greaterThan(0)));
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 0), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8), is(lessThan(0)));

        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 0xff), 0, 8), is(lessThan(0)));
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 0xff), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 8), 0, 8), is(greaterThan(0)));

        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 0), 0, 8,
                array(0, 2, 3, 4, 5, 6, 7, 8), 0, 8), is(greaterThan(0)));
        assertThat(CMP.compare(
                array(0, 2, 3, 4, 5, 6, 7, 8), 0, 8,
                array(1, 2, 3, 4, 5, 6, 7, 0), 0, 8), is(lessThan(0)));
    }

    /**
     * cmp - word rest.
     */
    @Test
    public void cmp_rest() {
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10), is(equalTo(0)));
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10,
                array(0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 1, 0), 1, 10), is(equalTo(0)));

        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 2), 0, 10), is(lessThan(0)));
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 2), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10), is(greaterThan(0)));

        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 0xff), 0, 10), is(lessThan(0)));
        assertThat(CMP.compare(
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 0xff), 0, 10,
                array(1, 2, 3, 4, 5, 6, 7, 8, 0, 1), 0, 10), is(greaterThan(0)));
    }

    private byte[] array(int... bytes) {
        byte[] results = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            results[i] = (byte) bytes[i];
        }
        return results;
    }
}
