/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
     * 変数なし。
     */
    @Test
    public void raw() {
        VariableTable parser = new VariableTable();
        assertThat(parser.parse("Hello, world!"), is("Hello, world!"));
    }

    /**
     * 単一の変数。
     */
    @Test
    public void single() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("you", "Java");
        assertThat(parser.parse("Hello, ${you}!"), is("Hello, Java!"));
    }

    /**
     * 複数の変数。
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
     * 空の変数表。
     */
    @Test
    public void list_empty() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("");
        assertThat(vars.getVariables().size(), is(0));
    }

    /**
     * 変数表を利用して単一の変数を追加。
     */
    @Test
    public void list_single() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key=value");
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get("key"), is("value"));
    }

    /**
     * 変数表を利用して複数の変数を追加。
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
     * 変数表を利用して複数の変数を追加。
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
     * 末尾に\が着た場合はそのままの文字として使う。
     */
    @Test
    public void list_escapeSequenceFragment() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key1=value1\\");
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get("key1"), is("value1\\"));
    }

    /**
     * キーと値の区切りが無い。
     */
    @Test(expected = IllegalArgumentException.class)
    public void list_notKeyValue() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key1");
    }

    /**
     * キーと値の区切りが無い。
     */
    @Test(expected = IllegalArgumentException.class)
    public void list_redefine() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("a=1,a=2");
    }

    /**
     * 空のキー。
     */
    @Test
    public void list_emptyKey() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("=value");
        assertThat(vars.getVariables().size(), is(1));
        assertThat(vars.getVariables().get(""), is("value"));
    }

    /**
     * 空の値。
     */
    @Test
    public void list_emptyValue() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("key=");
        assertThat(vars.getVariables().get("key"), is(""));
    }

    /**
     * 空の値。
     */
    @Test
    public void list_emptyKeyValue() {
        VariableTable vars = new VariableTable();
        vars.defineVariables("=");
        assertThat(vars.getVariables().get(""), is(""));
    }

    /**
     * 変数名を変数形式に変換。
     */
    @Test
    public void toVariable() {
        String exprHello = VariableTable.toVariable("hello");
        VariableTable vars = new VariableTable();
        vars.defineVariable("hello", "world");
        assertThat(vars.parse(exprHello), is("world"));
    }

    /**
     * 変数表を文字列に変換 (単一)。
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
     * 変数表を文字列に変換 (複数)。
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
     * 変数表を文字列に変換 (複数)。
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
     * 未定義。
     */
    @Test(expected = IllegalArgumentException.class)
    public void undefined() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("a", "A");
        parser.defineVariable("c", "C");
        assertThat(parser.parse("${a}${b}${c}!"), is("ABC!"));
    }

    /**
     * 再定義。
     */
    @Test(expected = IllegalArgumentException.class)
    public void redefined() {
        VariableTable parser = new VariableTable();
        parser.defineVariable("a", "A");
        parser.defineVariable("a", "B");
    }
}
