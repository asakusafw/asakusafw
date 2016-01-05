/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.modelgen.emitter;

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
     * {@link JavaName#of(String)}のテスト (snake_name)。
     */
    @Test
    public void snake_name_of() {
        JavaName name = JavaName.of("snake_name");
        assertThat(name.getSegments(), contains("snake", "name"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (CONSTANT_NAME)。
     */
    @Test
    public void CONSTANT_NAME_OF() {
        JavaName name = JavaName.of("CONSTANT_NAME");
        assertThat(name.getSegments(), contains("constant", "name"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (memberName)。
     */
    @Test
    public void memberNameOf() {
        JavaName name = JavaName.of("memberName");
        assertThat(name.getSegments(), contains("member", "name"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (TypeName)。
     */
    @Test
    public void TypeNameOf() {
        JavaName name = JavaName.of("TypeName");
        assertThat(name.getSegments(), contains("type", "name"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (UPPER single word)。
     */
    @Test
    public void constantSingleWordOf() {
        JavaName name = JavaName.of("UPPER");
        assertThat(name.getSegments(), contains("upper"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (Capital single word)。
     */
    @Test
    public void capitalSingleWordOf() {
        JavaName name = JavaName.of("Capital");
        assertThat(name.getSegments(), contains("capital"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (lower single word)。
     */
    @Test
    public void lowerSingleWordOf() {
        JavaName name = JavaName.of("lower");
        assertThat(name.getSegments(), contains("lower"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (empty)。
     */
    @Test(expected = IllegalArgumentException.class)
    public void of_empty() {
        JavaName.of("_");
    }

    /**
     * {@link JavaName#of(String)}のテスト (only underscore)。
     */
    @Test(expected = IllegalArgumentException.class)
    public void of_underscore() {
        JavaName.of("_");
    }

    /**
     * {@link JavaName#of(String)}のテスト (reduplicate underscore)。
     */
    @Test
    public void of_reduplicate_underscore() {
        JavaName name = JavaName.of("HELLO__WORLD");
        assertThat(name.getSegments(), contains("hello", "world"));
    }

    /**
     * {@link JavaName#of(String)}のテスト (starts with underscore)。
     */
    @Test
    public void of_starts_with_underscore() {
        JavaName name = JavaName.of("_hello");
        assertThat(name.getSegments(), contains("hello"));
    }

    /**
     * {@link JavaName#toTypeName()}のテスト。
     */
    @Test
    public void toTypeName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toTypeName(), is("OneTwoThreeFour"));
    }

    /**
     * {@link JavaName#toMemberName()}のテスト。
     */
    @Test
    public void toMemberName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toMemberName(), is("oneTwoThreeFour"));
    }

    /**
     * {@link JavaName#toConstantName()}のテスト。
     */
    @Test
    public void toConstantName() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        assertThat(name.toConstantName(), is("ONE_TWO_THREE_FOUR"));
    }

    /**
     * {@link JavaName#addFirst(String)}のテスト。
     */
    @Test
    public void addFirst() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        name.addFirst("Zero");
        assertThat(name.getSegments(), contains("zero", "one", "two", "three", "four"));
    }

    /**
     * {@link JavaName#addLast(String)}のテスト。
     */
    @Test
    public void addLast() {
        JavaName name = JavaName.of("OneTwoThreeFour");
        name.addLast("Five");
        assertThat(name.getSegments(), contains("one", "two", "three", "four", "five"));
    }

    private <T> Matcher<? super List<T>> contains(T... values) {
        return is(Arrays.asList(values));
    }
}
