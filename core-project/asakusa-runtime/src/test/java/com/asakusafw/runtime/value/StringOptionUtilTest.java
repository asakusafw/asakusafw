/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.value;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;

import org.junit.Test;

/**
 * Test for {@link StringOptionUtil}.
 */
public class StringOptionUtilTest {

    /**
     * test for as reader.
     */
    @Test
    public void reader() {
        String read = dump(new StringOption("Hello, world!"));
        assertThat(read, is("Hello, world!"));
    }

    /**
     * trim - nothing.
     */
    @Test
    public void trim_nothing() {
        StringOption value = new StringOption("Hello, world!");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * trim - leading.
     */
    @Test
    public void trim_leading() {
        StringOption value = new StringOption("    Hello, world!");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * trim - nothing.
     */
    @Test
    public void trim_trailing() {
        StringOption value = new StringOption("Hello, world!    ");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * trim - rounding.
     */
    @Test
    public void trim_rounding() {
        StringOption value = new StringOption("  Hello, world!  ");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * append - string option.
     */
    @Test
    public void append_option() {
        StringOption value = new StringOption("Hello");
        StringOptionUtil.append(value, new StringOption(", world!"));
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    /**
     * append - string.
     */
    @Test
    public void append_string() {
        StringOption value = new StringOption("Hello");
        StringOptionUtil.append(value, ", world!");
        StringOptionUtil.trim(value);
        assertThat(value, is(new StringOption("Hello, world!")));
    }

    private static String dump(StringOption option) {
        try (Reader reader = StringOptionUtil.asReader(option)) {
            char[] buf = new char[256];
            StringBuffer results = new StringBuffer();
            while (true) {
                int read = reader.read(buf);
                if (read < 0) {
                    break;
                }
                results.append(buf, 0, read);
            }
            return results.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
