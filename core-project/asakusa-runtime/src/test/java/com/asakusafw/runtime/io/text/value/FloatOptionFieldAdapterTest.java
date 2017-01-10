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
}
