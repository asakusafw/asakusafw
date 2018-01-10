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
package com.asakusafw.runtime.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link VariableTable}.
 */
public class VariableTableTest {

    /**
     * parses raw string.
     */
    @Test
    public void raw() {
        VariableTable parser = new VariableTable();
        assertThat(parser.parse("Hello, world!"), is("Hello, world!"));
    }

    /**
     * parses a string w/ a variable.
     */
    @Test
    public void single() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("you", "Java");
        assertThat(parser.parse("Hello, ${you}!"), is("Hello, Java!"));
    }

    /**
     * parses a string w/ multiple variables.
     */
    @Test
    public void multi() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("a", "A");
        parser.defineVariable("b", "B");
        parser.defineVariable("c", "C");
        assertThat(parser.parse("${a}${b}${c}!"), is("ABC!"));
    }

    /**
     * restores variables from empty string.
     */
    @Test
    public void list_empty() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("");
        assertThat(vars.getVariables().size(), is(0));
    }

    /**
     * restores a variable from string.
     */
    @Test
    public void list_single() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key=value");
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get("key"), is("value"));
    }

    /**
     * restores multiple variables from string.
     */
    @Test
    public void list_multi() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key1=value1,key2=value2,key3=value3");
        assertThat(vars.getVariables().size(), is(3));
        assertThat(vars.getVariables().get("key1"), is("value1"));
        assertThat(vars.getVariables().get("key2"), is("value2"));
        assertThat(vars.getVariables().get("key3"), is("value3"));
    }

    /**
     * restores variables from string w/ escape sequence.
     */
    @Test
    public void list_escaped() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key1\\=value1\\,key2=value2,key3=\\\\value3");
        assertThat(vars.getVariables().size(), is(2));
        assertThat(vars.getVariables().get("key1=value1,key2"), is("value2"));
        assertThat(vars.getVariables().get("key3"), is("\\value3"));
    }

    /**
     * restores variables from string which ends w/ escape symbol.
     */
    @Test
    public void list_escapeSequenceFragment() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key1=value1\\");
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get("key1"), is("value1\\"));
    }

    /**
     * missing key-value separator.
     */
    @Test(expected = IllegalArgumentException.class)
    public void list_notKeyValue() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key1");
    }

    /**
     * duplicate variables.
     */
    @Test(expected = IllegalArgumentException.class)
    public void list_redefine() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("a=1,a=2");
    }

    /**
     * empty key name.
     */
    @Test
    public void list_emptyKey() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("=value");
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get(""), is("value"));
    }

    /**
     * empty value.
     */
    @Test
    public void list_emptyValue() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key=");
        assertThat(vars.getVariables().get("key"), is(""));
    }

    /**
     * empty key name and value.
     */
    @Test
    public void list_emptyKeyValue() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("=");
        assertThat(vars.getVariables().get(""), is(""));
    }

    /**
     * test for {@code toVariable()}.
     */
    @Test
    public void toVariable() {
        String exprHello = VariableTable.toVariable("hello");
        VariableTable vars = new VariableTable();
        vars.defineVariable("hello", "world");
        assertThat(vars.parse(exprHello), is("world"));
    }

    /**
     * serialize a variable.
     */
    @Test
    public void toSerialString_simple() {
        VariableTable vars = new VariableTable();
        vars.defineVariable("hello", "world");

        VariableTable copy = new VariableTable();
        copy.defineVariables(vars.toSerialString());
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get("hello"), is("world"));
    }

    /**
     * serialize multiple variables.
     */
    @Test
    public void toSerialString_multiple() {
        VariableTable vars = new VariableTable();
        vars.defineVariable("k1", "v1");
        vars.defineVariable("k2", "v2");
        vars.defineVariable("k3", "v3");

        VariableTable copy = new VariableTable();
        copy.defineVariables(vars.toSerialString());
        assertThat(vars.getVariables().size(), is(3));
        assertThat(vars.getVariables().get("k1"), is("v1"));
        assertThat(vars.getVariables().get("k2"), is("v2"));
        assertThat(vars.getVariables().get("k3"), is("v3"));
    }

    /**
     * serialize variables w/ meta-characters.
     */
    @Test
    public void toSerialString_escaped() {
        VariableTable vars = new VariableTable();
        vars.defineVariable("", "");
        vars.defineVariable("\\", "\\");
        vars.defineVariable("=", "=");
        vars.defineVariable(",", ",");

        VariableTable copy = new VariableTable();
        copy.defineVariables(vars.toSerialString());
        assertThat(vars.getVariables().size(), is(4));
        assertThat(vars.getVariables().get(""), is(""));
        assertThat(vars.getVariables().get("\\"), is("\\"));
        assertThat(vars.getVariables().get("="), is("="));
        assertThat(vars.getVariables().get(","), is(","));
    }

    /**
     * undefined variables.
     */
    @Test(expected = IllegalArgumentException.class)
    public void undefined() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("a", "A");
        parser.defineVariable("c", "C");
        assertThat(parser.parse("${a}${b}${c}!"), is("ABC!"));
    }

    /**
     * redefine variables.
     */
    @Test(expected = IllegalArgumentException.class)
    public void redefined() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("a", "A");
        parser.defineVariable("a", "B");
    }
}
