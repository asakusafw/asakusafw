/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link IntOptionFieldAdapter}.
 */
public class IntOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().build();
        checkParse(adapter, 0, new IntOption(0));
        checkParse(adapter, 1, new IntOption(1));
        checkParse(adapter, -1, new IntOption(-1));
        checkParse(adapter, Integer.MAX_VALUE, new IntOption(Integer.MAX_VALUE));
        checkParse(adapter, Integer.MIN_VALUE, new IntOption(Integer.MIN_VALUE));
    }

    /**
     * parse - overflow.
     */
    @Test
    public void parse_overflow() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().build();
        checkMalformed(adapter, BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE).toString(), new IntOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new IntOption());
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new IntOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().build();
        checkEmit(adapter, new IntOption(1), 1);
        checkEmit(adapter, new IntOption(-1), -1);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().build();
        checkEmit(adapter, new IntOption(), null);
    }

    /**
     * emit - null w/ format.
     */
    @Test
    public void emit_null_formatted() {
        IntOptionFieldAdapter adapter = IntOptionFieldAdapter.builder().withNullFormat("").build();
        checkEmit(adapter, new IntOption(), "");
    }
}
