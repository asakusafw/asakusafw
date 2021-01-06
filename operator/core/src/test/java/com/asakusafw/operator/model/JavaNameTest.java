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
package com.asakusafw.operator.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * Test for {@link JavaName}.
 */
public class JavaNameTest {

    /**
     * Test for {@link JavaName#of(String)} (snake_name).
     */
    @Test
    public void snake_name_of() {
        JavaName name = JavaName.of("snake_name");
        assertThat(name.getSegments(), contains("snake", "name"));
    }

    /**
     * Test for {@link JavaName#of(String)} (CONSTANT_NAME).
     */
    @Test
    public void CONSTANT_NAME_OF() {
        JavaName name = JavaName.of("CONSTANT_NAME");
        assertThat(name.getSegments(), contains("constant", "name"));
    }

    /**
     * Test for {@link JavaName#of(String)} (memberName).
     */
    @Test
    public void memberNameOf() {
        JavaName name = JavaName.of("memberName");
        assertThat(name.getSegments(), contains("member", "name"));
    }

    /**
     * Test for {@link JavaName#of(String)} (TypeName).
     */
    @Test
    public void TypeNameOf() {
        JavaName name = JavaName.of("TypeName");
        assertThat(name.getSegments(), contains("type", "name"));
    }

    /**
     * Test for {@link JavaName#of(String)} (UPPER single word).
     */
    @Test
    public void constantSingleWordOf() {
        JavaName name = JavaName.of("UPPER");
        assertThat(name.getSegments(), contains("upper"));
    }

    /**
     * Test for {@link JavaName#of(String)} (Capital single word).
     */
    @Test
    public void capitalSingleWordOf() {
        JavaName name = JavaName.of("Capital");
        assertThat(name.getSegments(), contains("capital"));
    }

    /**
     * Test for {@link JavaName#of(String)} (lower single word).
     */
    @Test
    public void lowerSingleWordOf() {
        JavaName name = JavaName.of("lower");
        assertThat(name.getSegments(), contains("lower"));
    }

    /**
     * Test for {@link JavaName#of(String)} (empty).
     */
    @Test(expected = IllegalArgumentException.class)
    public void of_empty() {
        JavaName.of("");
    }

    /**
     * Test for {@link JavaName#of(String)} (only underscore).
     */
    @Test
    public void of_underscore() {
        JavaName name = JavaName.of("_");
        assertThat(name.getSegments().size(), is(0));
        assertThat(name.toConstantName(), is("_"));
        assertThat(name.toTypeName(), is("_"));
        assertThat(name.toMemberName(), is("_"));
    }

    /**
     * Test for {@link JavaName#of(String)} (reduplicate underscore).
     */
    @Test
    public void of_reduplicate_underscore() {
        JavaName name = JavaName.of("HELLO__WORLD");
        assertThat(name.getSegments(), contains("hello", "world"));
    }

    /**
     * Test for {@link JavaName#of(String)} (starts with underscore).
     */
    @Test
    public void of_starts_with_underscore() {
        JavaName name = JavaName.of("_hello");
        assertThat(name.getSegments(), contains("hello"));
    }

    /**
     * Test for {@link JavaName#toTypeName()}.
     */
    @Test
    public void toTypeName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toTypeName(), is("OneTwoThreeFour"));
    }

    /**
     * Test for {@link JavaName#toMemberName()}.
     */
    @Test
    public void toMemberName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toMemberName(), is("oneTwoThreeFour"));
    }

    /**
     * Test for {@link JavaName#toConstantName()}.
     */
    @Test
    public void toConstantName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toConstantName(), is("ONE_TWO_THREE_FOUR"));
    }

    /**
     * Test for {@link JavaName#toSnakeName()}.
     */
    @Test
    public void toSnakeName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toSnakeName(), is("one_two_three_four"));
    }

    /**
     * Test for {@link JavaName#addFirst(String)}.
     */
    @Test
    public void addFirst() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        name.addFirst("Zero");
        assertThat(name.getSegments(), contains("zero", "one", "two", "three", "four"));
    }

    /**
     * Test for {@link JavaName#addLast(String)}.
     */
    @Test
    public void addLast() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        name.addLast("Five");
        assertThat(name.getSegments(), contains("one", "two", "three", "four", "five"));
    }

    @SafeVarargs
    private static <T> Matcher<? super List<T>> contains(T... values) {
        return is(Arrays.asList(values));
    }
}
