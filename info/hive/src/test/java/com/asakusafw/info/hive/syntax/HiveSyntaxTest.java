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
package com.asakusafw.info.hive.syntax;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link HiveSyntax}.
 */
public class HiveSyntaxTest {

    /**
     * quote identifier.
     */
    @Test
    public void quote_identifier() {
        assertThat(HiveSyntax.quoteIdentifier("HELLO"), is("HELLO"));
        assertThat(HiveSyntax.quoteIdentifier("Hello"), is("Hello"));
        assertThat(HiveSyntax.quoteIdentifier("Hello, world!"), is("`Hello, world!`"));
    }

    /**
     * quote identifier.
     */
    @Test
    public void quote_string_single() {
        assertThat(HiveSyntax.quoteLiteral('\'', "HELLO"), is("'HELLO'"));
        assertThat(HiveSyntax.quoteLiteral('\'', "Hello"), is("'Hello'"));
        assertThat(HiveSyntax.quoteLiteral('\'', "Hello, world!"), is("'Hello, world!'"));
        assertThat(HiveSyntax.quoteLiteral('\'', "'Hello!'"), is("'\\'Hello!\\''"));
        assertThat(HiveSyntax.quoteLiteral('\'', "\"Hello!\""), is("'\"Hello!\"'"));
        assertThat(HiveSyntax.quoteLiteral('\'', "\\Hello/"), is("'\\\\Hello/'"));
    }

    /**
     * quote identifier.
     */
    @Test
    public void quote_string_double() {
        assertThat(HiveSyntax.quoteLiteral('"', "HELLO"), is("\"HELLO\""));
        assertThat(HiveSyntax.quoteLiteral('"', "Hello"), is("\"Hello\""));
        assertThat(HiveSyntax.quoteLiteral('"', "Hello, world!"), is("\"Hello, world!\""));
        assertThat(HiveSyntax.quoteLiteral('"', "'Hello!'"), is("\"'Hello!'\""));
        assertThat(HiveSyntax.quoteLiteral('"', "\"Hello!\""), is("\"\\\"Hello!\\\"\""));
        assertThat(HiveSyntax.quoteLiteral('"', "\\Hello/"), is("\"\\\\Hello/\""));
    }
}
