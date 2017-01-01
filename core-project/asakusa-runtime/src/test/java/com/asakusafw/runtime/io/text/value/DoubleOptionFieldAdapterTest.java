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
}
