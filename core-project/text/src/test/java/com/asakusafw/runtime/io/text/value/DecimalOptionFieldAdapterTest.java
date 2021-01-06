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
package com.asakusafw.runtime.io.text.value;

import static com.asakusafw.runtime.io.text.value.TestUtil.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.asakusafw.runtime.io.text.value.DecimalOptionFieldAdapter.OutputStyle;
import com.asakusafw.runtime.value.DecimalOption;

/**
 * Test for {@link DecimalOptionFieldAdapter}.
 */
public class DecimalOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder().build();
        checkParse(adapter, 0, new DecimalOption(d("0")));
        checkParse(adapter, "12345.67890", new DecimalOption(d("12345.67890")));
    }

    /**
     * parse - malformed.
     */
    @Test
    public void parse_malform() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "Hello, world!", new DecimalOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new DecimalOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_null() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkParse(adapter, "", new DecimalOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DecimalOption(d("0")), "0");
        checkEmit(adapter, new DecimalOption(d("12345.67890")), "12345.67890");
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DecimalOption(), null);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null_formatted() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkEmit(adapter, new DecimalOption(), "");
    }

    /**
     * emit - scientific.
     */
    @Test
    public void emit_default_style() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)), "1.2E+11");
    }

    /**
     * emit - plain.
     */
    @Test
    public void emit_plain() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder()
                .withOutputStyle(OutputStyle.PLAIN)
                .build();
        checkEmit(adapter, new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)), "120000000000");
    }

    /**
     * emit - engineering.
     */
    @Test
    public void emit_engineering() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder()
                .withOutputStyle(OutputStyle.ENGINEERING)
                .build();
        checkEmit(adapter, new DecimalOption(new BigDecimal(BigInteger.valueOf(12), -10)), "120E+9");
    }

    /**
     * w/ number format.
     */
    @Test
    public void number_format() {
        DecimalOptionFieldAdapter adapter = DecimalOptionFieldAdapter.builder()
                .withNumberFormat("0.00")
                .build();
        checkParse(adapter, 0, new DecimalOption(d("0")));
        checkParse(adapter, 1, new DecimalOption(d("1")));
        checkParse(adapter, -1, new DecimalOption(d("-1")));
        checkMalformed(adapter, "", new DecimalOption());
        checkMalformed(adapter, "Hello, world!", new DecimalOption());
        checkEmit(adapter, new DecimalOption(d("1")), "1.00");
        checkEmit(adapter, new DecimalOption(d("-1")), "-1.00");
        checkEmit(adapter, new DecimalOption(d("3.1415")), "3.14");
        checkEmit(adapter, new DecimalOption(d("12345")), "12345.00");
    }

    private BigDecimal d(String s) {
        return new BigDecimal(s);
    }
}
