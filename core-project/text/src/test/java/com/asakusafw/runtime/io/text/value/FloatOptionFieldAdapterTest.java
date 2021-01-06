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

import java.text.DecimalFormatSymbols;

import org.junit.Test;

import com.asakusafw.runtime.value.FloatOption;

/**
 * Test for {@link FloatOptionFieldAdapter}.
 */
public class FloatOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().build();
        checkParse(adapter, 0.f, new FloatOption(0));
        checkParse(adapter, 1.f, new FloatOption(1));
        checkParse(adapter, -1.f, new FloatOption(-1));
        checkParse(adapter, Float.NaN, new FloatOption(Float.NaN));
        checkParse(adapter, Float.POSITIVE_INFINITY, new FloatOption(Float.POSITIVE_INFINITY));
        checkParse(adapter, Float.NEGATIVE_INFINITY, new FloatOption(Float.NEGATIVE_INFINITY));
    }

    /**
     * parse - malformed.
     */
    @Test
    public void parse_malformed() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "A", new FloatOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new FloatOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new FloatOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().build();
        checkEmit(adapter, new FloatOption(0), 0.f);
        checkEmit(adapter, new FloatOption(1), 1.f);
        checkEmit(adapter, new FloatOption(-1), -1.f);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().build();
        checkEmit(adapter, new FloatOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_formatted() {
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder().withNullFormat("").build();
        checkEmit(adapter, new FloatOption(), "");
    }

    /**
     * w/ number format.
     */
    @Test
    public void number_format() {
        DecimalFormatSymbols syms = DecimalFormatSymbols.getInstance();
        FloatOptionFieldAdapter adapter = FloatOptionFieldAdapter.builder()
                .withNumberFormat("0.00")
                .withDecimalFormatSymbols(syms)
                .build();
        checkParse(adapter, 0, new FloatOption(0));
        checkParse(adapter, 1, new FloatOption(1));
        checkParse(adapter, -1, new FloatOption(-1));
        checkParse(adapter, "-0", new FloatOption(0));
        checkParse(adapter, syms.getNaN(), new FloatOption(Float.NaN));
        checkParse(adapter, syms.getInfinity(), new FloatOption(Float.POSITIVE_INFINITY));
        checkParse(adapter, "-" + syms.getInfinity(), new FloatOption(Float.NEGATIVE_INFINITY));
        checkMalformed(adapter, "", new FloatOption());
        checkMalformed(adapter, "Hello, world!", new FloatOption());
        checkEmit(adapter, new FloatOption(1), "1.00");
        checkEmit(adapter, new FloatOption(-1), "-1.00");
        checkEmit(adapter, new FloatOption((float) Math.PI), "3.14");
        checkEmit(adapter, new FloatOption(12345), "12345.00");
        checkEmit(adapter, new FloatOption(-0), "0.00");
        checkEmit(adapter, new FloatOption(Float.NaN), syms.getNaN());
        checkEmit(adapter, new FloatOption(Float.POSITIVE_INFINITY), syms.getInfinity());
        checkEmit(adapter, new FloatOption(Float.NEGATIVE_INFINITY), "-" + syms.getInfinity());
    }
}
