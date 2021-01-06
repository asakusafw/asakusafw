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

import com.asakusafw.runtime.value.ByteOption;

/**
 * Test for {@link ByteOptionFieldAdapter}.
 */
public class ByteOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().build();
        checkParse(adapter, 0, new ByteOption((byte) 0));
        checkParse(adapter, 1, new ByteOption((byte) 1));
        checkParse(adapter, -1, new ByteOption((byte) -1));
        checkParse(adapter, Byte.MAX_VALUE, new ByteOption(Byte.MAX_VALUE));
        checkParse(adapter, Byte.MIN_VALUE, new ByteOption(Byte.MIN_VALUE));
    }

    /**
     * parse - overflow.
     */
    @Test
    public void parse_overflow() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().build();
        checkMalformed(adapter, BigInteger.valueOf(Byte.MAX_VALUE).add(BigInteger.ONE).toString(), new ByteOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new ByteOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new ByteOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().build();
        checkEmit(adapter, new ByteOption((byte) 1), 1);
        checkEmit(adapter, new ByteOption((byte) -1), -1);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().build();
        checkEmit(adapter, new ByteOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_formatted() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder().withNullFormat("").build();
        checkEmit(adapter, new ByteOption(), "");
    }

    /**
     * w/ number format.
     */
    @Test
    public void number_format() {
        ByteOptionFieldAdapter adapter = ByteOptionFieldAdapter.builder()
                .withNumberFormat("00")
                .build();
        checkParse(adapter, 0, new ByteOption((byte) 0));
        checkParse(adapter, 1, new ByteOption((byte) 1));
        checkParse(adapter, -1, new ByteOption((byte) -1));
        checkParse(adapter, Byte.MAX_VALUE, new ByteOption(Byte.MAX_VALUE));
        checkParse(adapter, Byte.MIN_VALUE, new ByteOption(Byte.MIN_VALUE));
        checkMalformed(adapter, "", new ByteOption());
        checkMalformed(adapter, "Hello, world!", new ByteOption());
        checkMalformed(adapter, BigInteger.valueOf(Byte.MAX_VALUE).add(BigInteger.ONE).toString(), new ByteOption());
        checkEmit(adapter, new ByteOption((byte) 1), "01");
        checkEmit(adapter, new ByteOption((byte) -1), "-01");
        checkEmit(adapter, new ByteOption((byte) 100), "100");
        checkEmit(adapter, new ByteOption((byte) -100), "-100");
    }
}
