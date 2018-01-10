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

import org.junit.Test;

import com.asakusafw.runtime.value.BooleanOption;

/**
 * Test for {@link ByteOptionFieldAdapter}.
 */
public class BooleanOptionFieldAdapterTest {

    /**
     * parse.
     */
    @Test
    public void parse() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder().build();
        checkParse(adapter, true, new BooleanOption(true));
        checkParse(adapter, false, new BooleanOption(false));
    }

    /**
     * parse - null.
     */
    @Test
    public void parse_null() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder().build();
        checkParse(adapter, null, new BooleanOption());
    }

    /**
     * parse - empty.
     */
    @Test
    public void parse_empty() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder().build();
        checkMalformed(adapter, "", new BooleanOption());
    }

    /**
     * parse - null formatted.
     */
    @Test
    public void parse_null_formatted() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkParse(adapter, "", new BooleanOption());
    }

    /**
     * parse - custom format.
     */
    @Test
    public void parse_format() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder()
                .withTrueFormat("1")
                .withFalseFormat("0")
                .build();
        checkParse(adapter, 1, new BooleanOption(true));
        checkParse(adapter, 0, new BooleanOption(false));
    }

    /**
     * parse - custom format.
     */
    @Test
    public void parse_predicate() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder()
                .withTrueFormat("T", s -> s.length() > 0 && "F".contentEquals(s) == false)
                .withFalseFormat("F", s -> s.length() == 0 || "F".contentEquals(s))
                .build();
        checkParse(adapter, "F", new BooleanOption(false));
        checkParse(adapter, "T", new BooleanOption(true));

        checkParse(adapter, "", new BooleanOption(false));
        checkParse(adapter, "abc", new BooleanOption(true));
        checkParse(adapter, "true", new BooleanOption(true));
        checkParse(adapter, "false", new BooleanOption(true));
    }

    /**
     * emit.
     */
    @Test
    public void emit() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder().build();
        checkEmit(adapter, new BooleanOption(true), true);
        checkEmit(adapter, new BooleanOption(false), false);
    }

    /**
     * emit - null.
     */
    @Test
    public void emit_null() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder().build();
        checkEmit(adapter, new BooleanOption(), null);
    }

    /**
     * emit - null formatted.
     */
    @Test
    public void emit_null_formatted() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder()
                .withNullFormat("")
                .build();
        checkEmit(adapter, new BooleanOption(), "");
    }

    /**
     * emit - w/ format.
     */
    @Test
    public void emit_format() {
        BooleanOptionFieldAdapter adapter = BooleanOptionFieldAdapter.builder()
                .withTrueFormat("1")
                .withFalseFormat("0")
                .build();
        checkEmit(adapter, new BooleanOption(true), "1");
        checkEmit(adapter, new BooleanOption(false), "0");
    }
}
