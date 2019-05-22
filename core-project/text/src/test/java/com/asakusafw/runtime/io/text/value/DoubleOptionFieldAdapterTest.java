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
package com.asakusafw.runtime.io.text.value;

import static com.asakusafw.runtime.io.text.value.TestUtil.*;

import java.text.DecimalFormatSymbols;

import org.junit.Test;

import com.asakusafw.runtime.value.DoubleOption;

/**
 * Test for {@link DoubleOptionFieldAdapter}.
 */
public class DoubleOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().build();
        checkParse(adapter, 0.d, new DoubleOption(0));
        checkParse(adapter, 1.d, new DoubleOption(1));
        checkParse(adapter, -1.d, new DoubleOption(-1));
        checkParse(adapter, Double.NaN, new DoubleOption(Double.NaN));
        checkParse(adapter, Double.POSITIVE_INFINITY, new DoubleOption(Double.POSITIVE_INFINITY));
        checkParse(adapter, Double.NEGATIVE_INFINITY, new DoubleOption(Double.NEGATIVE_INFINITY));
    }

    /**
     * parse - malformed.
     */
    @Test
    public void parse_malformed() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "A", new DoubleOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new DoubleOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new DoubleOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DoubleOption(0), 0.d);
        checkEmit(adapter, new DoubleOption(1), 1.d);
        checkEmit(adapter, new DoubleOption(-1), -1.d);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().build();
        checkEmit(adapter, new DoubleOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_formatted() {
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder().withNullFormat("").build();
        checkEmit(adapter, new DoubleOption(), "");
    }

    /**
     * w/ number format.
     */
    @Test
    public void number_format() {
        DecimalFormatSymbols syms = DecimalFormatSymbols.getInstance();
        DoubleOptionFieldAdapter adapter = DoubleOptionFieldAdapter.builder()
                .withNumberFormat("0.00")
                .withDecimalFormatSymbols(syms)
                .build();
        checkParse(adapter, 0, new DoubleOption(0));
        checkParse(adapter, 1, new DoubleOption(1));
        checkParse(adapter, -1, new DoubleOption(-1));
        checkParse(adapter, "-0", new DoubleOption(0));
        checkParse(adapter, syms.getNaN(), new DoubleOption(Double.NaN));
        checkParse(adapter, syms.getInfinity(), new DoubleOption(Double.POSITIVE_INFINITY));
        checkParse(adapter, "-" + syms.getInfinity(), new DoubleOption(Double.NEGATIVE_INFINITY));
        checkMalformed(adapter, "", new DoubleOption());
        checkMalformed(adapter, "Hello, world!", new DoubleOption());
        checkEmit(adapter, new DoubleOption(1), "1.00");
        checkEmit(adapter, new DoubleOption(-1), "-1.00");
        checkEmit(adapter, new DoubleOption(Math.PI), "3.14");
        checkEmit(adapter, new DoubleOption(12345), "12345.00");
        checkEmit(adapter, new DoubleOption(-0), "0.00");
        checkEmit(adapter, new DoubleOption(Double.NaN), syms.getNaN());
        checkEmit(adapter, new DoubleOption(Double.POSITIVE_INFINITY), syms.getInfinity());
        checkEmit(adapter, new DoubleOption(Double.NEGATIVE_INFINITY), "-" + syms.getInfinity());
    }
}
