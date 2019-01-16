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

import org.junit.Test;

import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link StringOptionFieldAdapter}.
 */
public class StringOptionFieldAdapterTest {

    private static final String CP_BEER = new StringBuilder().appendCodePoint(0x1f37a).toString();

    /**
     * parse.
     */
    @Test
    public void parse() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkParse(adapter, "Hello, world!", new StringOption("Hello, world!"));
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkParse(adapter, "", new StringOption(""));
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().withNullFormat("").build();
        checkParse(adapter, "", new StringOption());
    }

    /**
     * parse - surrogate pair.
     */
    @Test
    public void parse_surrogate_pair() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkParse(adapter, CP_BEER, new StringOption(CP_BEER));
    }

    /**
     * parse - overflow.
     */
    @Test
    public void parse_overflow() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 257; i++) {
            buf.append('a');
        }
        checkParse(adapter, buf.toString(), new StringOption(buf.toString()));
    }

    /**
     * parse - broken UTF-16.
     */
    @Test
    public void parse_broken() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkMalformed(adapter, String.valueOf(CP_BEER.charAt(0)), new StringOption());
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkEmit(adapter, new StringOption("Hello, world!"), "Hello, world!");
    }

    /**
     * emit - empty.
     */
    @Test
    public void emit_empty() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkEmit(adapter, new StringOption(""), "");
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().build();
        checkEmit(adapter, new StringOption(), null);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null_formatted() {
        StringOptionFieldAdapter adapter = StringOptionFieldAdapter.builder().withNullFormat("null").build();
        checkEmit(adapter, new StringOption(), "null");
    }
}
