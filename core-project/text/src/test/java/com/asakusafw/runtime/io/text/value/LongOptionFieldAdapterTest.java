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

import java.math.BigInteger;

import org.junit.Test;

import com.asakusafw.runtime.value.LongOption;

/**
 * Test for {@link LongOptionFieldAdapter}.
 */
public class LongOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().build();
        checkParse(adapter, 0, new LongOption(0));
        checkParse(adapter, 1, new LongOption(1));
        checkParse(adapter, -1, new LongOption(-1));
        checkParse(adapter, Long.MAX_VALUE, new LongOption(Long.MAX_VALUE));
        checkParse(adapter, Long.MIN_VALUE, new LongOption(Long.MIN_VALUE));
    }

    /**
     * parse - overflow.
     */
    @Test
    public void parse_overflow() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().build();
        checkMalformed(adapter, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString(), new LongOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new LongOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new LongOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().build();
        checkEmit(adapter, new LongOption(1), 1);
        checkEmit(adapter, new LongOption(-1), -1);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().build();
        checkEmit(adapter, new LongOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_formatted() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder().withNullFormat("").build();
        checkEmit(adapter, new LongOption(), "");
    }

    /**
     * w/ number format.
     */
    @Test
    public void number_format() {
        LongOptionFieldAdapter adapter = LongOptionFieldAdapter.builder()
                .withNumberFormat("0,000")
                .build();
        checkParse(adapter, 0, new LongOption(0));
        checkParse(adapter, 1, new LongOption(1));
        checkParse(adapter, -1, new LongOption(-1));
        checkParse(adapter, Long.MAX_VALUE, new LongOption(Long.MAX_VALUE));
        checkParse(adapter, Long.MIN_VALUE, new LongOption(Long.MIN_VALUE));
        checkMalformed(adapter, "", new LongOption());
        checkMalformed(adapter, "Hello, world!", new LongOption());
        checkMalformed(adapter, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString(), new LongOption());
        checkEmit(adapter, new LongOption(1), "0,001");
        checkEmit(adapter, new LongOption(-1), "-0,001");
        checkEmit(adapter, new LongOption(1000), "1,000");
        checkEmit(adapter, new LongOption(10000), "10,000");
    }
}
