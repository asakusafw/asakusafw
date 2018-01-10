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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.math.BigDecimal;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.IOUtils.NullOutputStream;
import org.junit.Test;

/**
 * Test for {@link DecimalOption}.
 */
@SuppressWarnings("deprecation")
public class DecimalOptionTest extends ValueOptionTestRoot {

    /**
     * test for initial state.
     */
    @Test
    public void init() {
        DecimalOption option = new DecimalOption();
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for get.
     */
    @Test
    public void get() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(100));
        assertThat(option.get(), is(decimal(100)));
        assertThat(option.isNull(), is(false));
    }

    /**
     * test for or w/ null.
     */
    @Test
    public void or() {
        DecimalOption option = new DecimalOption();
        assertThat(option.or(decimal(30)), is(decimal(30)));
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for or w/ present value.
     */
    @Test
    public void orNotNull() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(100));
        assertThat(option.or(decimal(30)), is(decimal(100)));
    }

    /**
     * test for copyFrom.
     */
    @Test
    public void copy() {
        DecimalOption option = new DecimalOption();
        DecimalOption other = new DecimalOption();
        other.modify(decimal(50));
        option.copyFrom(other);
        assertThat(option.get(), is(decimal(50)));

        option.modify(decimal(0));
        assertThat(other.get(), is(decimal(50)));
    }

    /**
     * test for copyFrom w/ null.
     */
    @Test
    public void copyNull() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(100));

        DecimalOption other = new DecimalOption();
        option.copyFrom(other);
        assertThat(option.isNull(), is(true));
        option.modify(decimal(100));

        option.copyFrom(null);
        assertThat(option.isNull(), is(true));
    }

    /**
     * test for comapre.
     */
    @Test
    public void compareTo() {
        DecimalOption a = new DecimalOption();
        DecimalOption b = new DecimalOption();
        DecimalOption c = new DecimalOption();
        DecimalOption d = new DecimalOption();
        DecimalOption e = new DecimalOption();

        a.modify(decimal(-10));
        b.modify(decimal(0));
        c.modify(decimal(50));
        d.modify(decimal(-10));
        e.modify(decimal(-30));

        assertThat(compare(a, b), lessThan(0));
        assertThat(compare(b, c), lessThan(0));
        assertThat(compare(c, a), greaterThan(0));
        assertThat(compare(a, c), lessThan(0));
        assertThat(compare(b, a), greaterThan(0));
        assertThat(compare(c, b), greaterThan(0));
        assertThat(compare(a, d), is(0));
        assertThat(compare(a, e), greaterThan(0));
    }

    /**
     * test for compare w/ different scales.
     */
    @Test
    public void compareTo_scale() {
        DecimalOption a1 = new DecimalOption();
        DecimalOption a2 = new DecimalOption();
        DecimalOption b1 = new DecimalOption();
        DecimalOption b2 = new DecimalOption();
        DecimalOption c1 = new DecimalOption();
        DecimalOption c2 = new DecimalOption();
        DecimalOption d1 = new DecimalOption();
        DecimalOption d2 = new DecimalOption();

        a1.modify(decimal("10000000."));
        a2.modify(decimal("10000000.0"));

        b1.modify(decimal("1."));
        b2.modify(decimal("1.0000000"));

        c1.modify(decimal("0.0000001"));
        c2.modify(decimal("0.00000010"));

        d1.modify(decimal("-1."));
        d2.modify(decimal("-1.0000000"));

        assertThat(compare(a1, a2), equalTo(0));
        assertThat(compare(b1, b2), equalTo(0));
        assertThat(compare(c1, c2), equalTo(0));
        assertThat(compare(d1, d2), equalTo(0));

        assertThat(compare(a1, b1), greaterThan(0));
        assertThat(compare(a1, b2), greaterThan(0));
        assertThat(compare(a1, c1), greaterThan(0));
        assertThat(compare(a1, c2), greaterThan(0));
        assertThat(compare(b1, c1), greaterThan(0));
        assertThat(compare(b1, c2), greaterThan(0));

        assertThat(compare(b1, a1), lessThan(0));
        assertThat(compare(b1, a2), lessThan(0));
        assertThat(compare(c1, a1), lessThan(0));
        assertThat(compare(c1, a2), lessThan(0));
        assertThat(compare(c1, b1), lessThan(0));
        assertThat(compare(c1, b2), lessThan(0));
    }

    /**
     * test for compare w/ null.
     */
    @Test
    public void compareNull() {
        DecimalOption a = new DecimalOption();
        DecimalOption b = new DecimalOption();
        DecimalOption c = new DecimalOption();

        a.modify(decimal(Long.MIN_VALUE));

        assertThat(compare(a, b), greaterThan(0));
        assertThat(compare(b, a), lessThan(0));
        assertThat(compare(b, c), is(0));
    }

    /**
     * test for Writable.
     */
    @Test
    public void write() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal("3.14"));
        DecimalOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ max.
     */
    @Test
    public void write_max() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(Long.MAX_VALUE).add(decimal(Long.MAX_VALUE)));
        DecimalOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ min.
     */
    @Test
    public void write_min() {
        DecimalOption option = new DecimalOption();
        option.modify(decimal(Long.MIN_VALUE).add(decimal(Long.MAX_VALUE)));
        DecimalOption restored = restore(option);
        assertThat(restored.get(), is(option.get()));
    }

    /**
     * test for Writable w/ null.
     */
    @Test
    public void writeNull() {
        DecimalOption option = new DecimalOption();
        DecimalOption restored = restore(option);
        assertThat(restored.isNull(), is(true));
    }

    /**
     * heavy write.
     * @throws Exception if failed
     */
    @Test
    public void stress_write() throws Exception {
        int count = 10000000;
        DecimalOption option = new DecimalOption(new BigDecimal("3.14"));
        DataOutput out = new DataOutputStream(new NullOutputStream());
        for (int i = 0; i < count; i++) {
            option.write(out);
        }
    }

    /**
     * heavy read.
     * @throws Exception if failed
     */
    @Test
    public void stress_read() throws Exception {
        int count = 10000000;
        DecimalOption value = new DecimalOption(new BigDecimal("3.14"));
        byte[] bytes = toBytes(value);
        DecimalOption buf = new DecimalOption();
        DataInputBuffer in = new DataInputBuffer();
        for (int i = 0; i < count; i++) {
            in.reset(bytes, bytes.length);
            buf.readFields(in);
            if (i == 0) {
                assertThat(buf, is(value));
            }
        }
    }

    /**
     * heavy read.
     * @throws Exception if failed
     */
    @Test
    public void stress_read_large() throws Exception {
        int count = 10000000;
        DecimalOption value = new DecimalOption(new BigDecimal(Long.MAX_VALUE).multiply(BigDecimal.TEN));
        byte[] bytes = toBytes(value);
        DecimalOption buf = new DecimalOption();
        DataInputBuffer in = new DataInputBuffer();
        for (int i = 0; i < count; i++) {
            in.reset(bytes, bytes.length);
            buf.readFields(in);
            if (i == 0) {
                assertThat(buf, is(value));
            }
        }
    }

    /**
     * heavy restore.
     * @throws Exception if failed
     */
    @Test
    public void stress_restore() throws Exception {
        int count = 10000000;
        DecimalOption value = new DecimalOption(new BigDecimal("3.14"));
        byte[] bytes = toBytes(value);
        DecimalOption buf = new DecimalOption();
        for (int i = 0; i < count; i++) {
            buf.restore(bytes, 0, bytes.length);
            if (i == 0) {
                assertThat(buf, is(value));
            }
        }
    }

    /**
     * heavy restore.
     * @throws Exception if failed
     */
    @Test
    public void stress_restore_large() throws Exception {
        int count = 10000000;
        DecimalOption value = new DecimalOption(new BigDecimal(Long.MAX_VALUE).multiply(BigDecimal.TEN));
        byte[] bytes = toBytes(value);
        DecimalOption buf = new DecimalOption();
        for (int i = 0; i < count; i++) {
            buf.restore(bytes, 0, bytes.length);
            if (i == 0) {
                assertThat(buf, is(value));
            }
        }
    }

    /**
     * heavy compare bytes.
     * @throws Exception if failed
     */
    @Test
    public void stress_compareBytes_same_scale() throws Exception {
        int count = 10000000;
        byte[] a = toBytes(new DecimalOption(new BigDecimal("3.14")));
        byte[] b = toBytes(new DecimalOption(new BigDecimal("1.41")));
        for (int i = 0; i < count; i++) {
            DecimalOption.compareBytes(a, 0, a.length, b, 0, b.length);
        }
    }

    /**
     * heavy compare bytes.
     * @throws Exception if failed
     */
    @Test
    public void stress_compareBytes_diff_scale() throws Exception {
        int count = 10000000;
        byte[] a = toBytes(new DecimalOption(new BigDecimal("3.14")));
        byte[] b = toBytes(new DecimalOption(new BigDecimal("1.414")));
        for (int i = 0; i < count; i++) {
            DecimalOption.compareBytes(a, 0, a.length, b, 0, b.length);
        }
    }

    private BigDecimal decimal(long value) {
        return new BigDecimal(value);
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
