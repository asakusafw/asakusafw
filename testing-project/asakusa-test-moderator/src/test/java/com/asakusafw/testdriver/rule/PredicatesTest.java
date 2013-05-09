/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.testdriver.rule;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;

import org.junit.Test;

/**
 * Test for {@link Predicates}.
 * @since 0.2.0
 */
public class PredicatesTest {

    /**
     * Test method for {@link Predicates#equalTo(java.lang.Object)}.
     */
    @Test
    public void equalTo() {
        ValuePredicate<Object> p = Predicates.equalTo(100);
        assertThat(p.accepts(100, 100), is(true));
        assertThat(p.accepts(200, 100), is(true));
        assertThat(p.accepts(100, 200), is(false));

        // no exceptions
        p.describeExpected(100, 200);
    }

    /**
     * Test method for {@link Predicates#isNull()}.
     */
    @Test
    public void isNull() {
        ValuePredicate<Object> p = Predicates.isNull();
        assertThat(p.accepts(100, null), is(true));
        assertThat(p.accepts(200, 100), is(false));
        assertThat(p.accepts(null, 200), is(false));

        // no exceptions
        p.describeExpected(100, 200);
    }

    /**
     * Test method for {@link Predicates#not(ValuePredicate)}.
     */
    @Test
    public void not() {
        ValuePredicate<Object> p = Predicates.not(Predicates.equalTo(100));
        assertThat(p.accepts(100, 100), is(false));
        assertThat(p.accepts(200, 100), is(false));
        assertThat(p.accepts(100, 200), is(true));

        // no exceptions
        p.describeExpected(100, 200);
    }

    /**
     * Test method for {@link Predicates#equals()}.
     */
    @Test
    public void equals() {
        ValuePredicate<Object> p = Predicates.equals();
        assertThat(p.accepts(100, 100), is(true));
        assertThat(p.accepts(200, 100), is(false));
        assertThat(p.accepts(100, 200), is(false));

        // no exceptions
        p.describeExpected(100, 200);
    }

    /**
     * Test method for {@link Predicates#floatRange(double, double)}.
     */
    @Test
    public void floatRange() {
        ValuePredicate<Number> p = Predicates.floatRange(-1.0, +2.0);
        assertThat(p.accepts(10.0, 10.0), is(true));
        assertThat(p.accepts(10.0, 9.0), is(true));
        assertThat(p.accepts(10.0, 12.0), is(true));
        assertThat(p.accepts(10.0, 8.9), is(false));
        assertThat(p.accepts(10.0, 12.1), is(false));

        // no exceptions
        p.describeExpected(100.0, 200.0);
    }

    /**
     * Test method for {@link Predicates#integerRange(long, long)}.
     */
    @Test
    public void integerRange() {
        ValuePredicate<Number> p = Predicates.integerRange(-1, +2);
        assertThat(p.accepts(10, 10), is(true));
        assertThat(p.accepts(10, 9), is(true));
        assertThat(p.accepts(10, 12), is(true));
        assertThat(p.accepts(10, 8), is(false));
        assertThat(p.accepts(10, 13), is(false));

        // no exceptions
        p.describeExpected(100, 200);
    }

    /**
     * Test method for {@link Predicates#decimalRange(java.math.BigDecimal, java.math.BigDecimal)}.
     */
    @Test
    public void decimalRange() {
        ValuePredicate<BigDecimal> p = Predicates.decimalRange(new BigDecimal(-1), new BigDecimal(+2));
        assertThat(p.accepts(new BigDecimal(10.0), new BigDecimal(10.0)), is(true));
        assertThat(p.accepts(new BigDecimal(10.0), new BigDecimal(9.0)), is(true));
        assertThat(p.accepts(new BigDecimal(10.0), new BigDecimal(12.0)), is(true));
        assertThat(p.accepts(new BigDecimal(10.0), new BigDecimal(8.9)), is(false));
        assertThat(p.accepts(new BigDecimal(10.0), new BigDecimal(12.1)), is(false));

        // no exceptions
        p.describeExpected(new BigDecimal(100), new BigDecimal(200));
    }

    /**
     * Test method for {@link Predicates#dateRange(int, int)}.
     */
    @Test
    public void dateRange() {
        ValuePredicate<Calendar> p = Predicates.dateRange(-1, +2);
        assertThat(p.accepts(d(2011, 5, 6), d(2011, 5, 6)), is(true));
        assertThat(p.accepts(d(2011, 5, 6), d(2011, 5, 5)), is(true));
        assertThat(p.accepts(d(2011, 5, 6), d(2011, 5, 8)), is(true));
        assertThat(p.accepts(d(2011, 5, 6), d(2011, 5, 4)), is(false));
        assertThat(p.accepts(d(2011, 5, 6), d(2011, 5, 9)), is(false));

        p.describeExpected(d(2011, 12, 13), d(2012, 3, 1));
    }

    private Calendar d(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, y);
        c.set(Calendar.MONTH, m - 1);
        c.set(Calendar.DATE, d);
        return c;
    }

    /**
     * Test method for {@link Predicates#timeRange(int, int)}.
     */
    @Test
    public void timeRange() {
        ValuePredicate<Calendar> p = Predicates.timeRange(-1, +2);
        assertThat(p.accepts(t(12, 34, 56), t(12, 34, 56)), is(true));
        assertThat(p.accepts(t(12, 34, 56), t(12, 34, 55)), is(true));
        assertThat(p.accepts(t(12, 34, 56), t(12, 34, 58)), is(true));
        assertThat(p.accepts(t(12, 34, 56), t(12, 34, 54)), is(false));
        assertThat(p.accepts(t(12, 34, 56), t(12, 34, 59)), is(false));

        p.describeExpected(t(0, 1, 2), t(23, 59, 59));
    }

    private Calendar t(int h, int m, int s) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, 2011);
        c.set(Calendar.MONTH, 2);
        c.set(Calendar.DATE, 31);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, s);
        return c;
    }

    /**
     * Test method for compares date/time.
     */
    @Test
    public void dateAndTime() {
        ValuePredicate<Calendar> p = Predicates.timeRange(0, 0);
        assertThat(p.accepts(d(2000, 1, 1), t(2000, 1, 1, 0, 0, 0)), is(true));
        assertThat(p.accepts(t(2000, 1, 1, 0, 0, 0), d(2000, 1, 1)), is(true));
    }

    private Calendar t(
            int y, int mo, int d,
            int h, int mi, int s) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, y);
        c.set(Calendar.MONTH, mo -1);
        c.set(Calendar.DATE, d);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, mi);
        c.set(Calendar.SECOND, s);
        return c;
    }

    /**
     * Test method for {@link Predicates#containsString()}.
     */
    @Test
    public void containsString() {
        ValuePredicate<String> p = Predicates.containsString();
        assertThat(p.accepts("abc", "abc"), is(true));
        assertThat(p.accepts("abc", "abcde"), is(true));
        assertThat(p.accepts("abc", "deabcde"), is(true));
        assertThat(p.accepts("abc", "abbc"), is(false));
        assertThat(p.accepts("abc", "ab"), is(false));

        p.describeExpected("abc", "def");
    }
}
