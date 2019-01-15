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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * Test for Difference.
 * @since 0.2.2
 */
public class DifferenceTest {

    /**
     * format for String.
     */
    @Test
    public void string() {
        String value = Difference.format("hoge");
        assertThat(value, is("\"hoge\""));
    }

    /**
     * format for String with meta characters.
     */
    @Test
    public void string_escape() {
        String value = Difference.format("\t\r\n\"\\");
        assertThat(value, is("\"\\t\\r\\n\\\"\\\\\""));
    }

    /**
     * format for Calendar.
     */
    @Test
    public void calendar() {
        String value = Difference.format(new GregorianCalendar(2011, 1, 2, 13, 10, 15));
        assertThat(value, is("2011-02-02 13:10:15"));
    }

    /**
     * format for BigDecimal
     */
    @Test
    public void bigdecimal() {
        String value = Difference.format(new BigDecimal("0.000000000000000000000001"));
        assertThat(value, is("0.000000000000000000000001"));
    }
}
