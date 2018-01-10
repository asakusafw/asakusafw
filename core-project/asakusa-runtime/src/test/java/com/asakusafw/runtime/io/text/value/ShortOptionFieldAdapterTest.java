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

import static com.asakusafw.runtime.io.text.value.TestUtil.*;

import java.math.BigInteger;

import org.junit.Test;

import com.asakusafw.runtime.value.ShortOption;

/**
 * Test for {@link ShortOptionFieldAdapter}.
 */
public class ShortOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().build();
        checkParse(adapter, 0, new ShortOption((short) 0));
        checkParse(adapter, 1, new ShortOption((short) 1));
        checkParse(adapter, -1, new ShortOption((short) -1));
        checkParse(adapter, Short.MAX_VALUE, new ShortOption(Short.MAX_VALUE));
        checkParse(adapter, Short.MIN_VALUE, new ShortOption(Short.MIN_VALUE));
    }

    /**
     * parse - overflow.
     */
    @Test
    public void parse_overflow() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().build();
        checkMalformed(adapter, BigInteger.valueOf(Short.MAX_VALUE).add(BigInteger.ONE).toString(), new ShortOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new ShortOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new ShortOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().build();
        checkEmit(adapter, new ShortOption((short) 1), 1);
        checkEmit(adapter, new ShortOption((short) -1), -1);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().build();
        checkEmit(adapter, new ShortOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_formatted() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder().withNullFormat("").build();
        checkEmit(adapter, new ShortOption(), "");
    }

    /**
     * w/ number format.
     */
    @Test
    public void number_format() {
        ShortOptionFieldAdapter adapter = ShortOptionFieldAdapter.builder()
                .withNumberFormat("0,000")
                .build();
        checkParse(adapter, 0, new ShortOption((short) 0));
        checkParse(adapter, 1, new ShortOption((short) 1));
        checkParse(adapter, -1, new ShortOption((short) -1));
        checkParse(adapter, Short.MAX_VALUE, new ShortOption(Short.MAX_VALUE));
        checkParse(adapter, Short.MIN_VALUE, new ShortOption(Short.MIN_VALUE));
        checkMalformed(adapter, "", new ShortOption());
        checkMalformed(adapter, "Hello, world!", new ShortOption());
        checkMalformed(adapter, BigInteger.valueOf(Short.MAX_VALUE).add(BigInteger.ONE).toString(), new ShortOption());
        checkEmit(adapter, new ShortOption((short) 1), "0,001");
        checkEmit(adapter, new ShortOption((short) -1), "-0,001");
        checkEmit(adapter, new ShortOption((short) 1000), "1,000");
        checkEmit(adapter, new ShortOption((short) 10000), "10,000");
    }
}
