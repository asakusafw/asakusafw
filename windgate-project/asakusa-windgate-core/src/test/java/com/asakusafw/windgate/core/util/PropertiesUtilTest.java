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
package com.asakusafw.windgate.core.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

/**
 * Test for {@link PropertiesUtil}.
 */
public class PropertiesUtilTest {

    /**
     * Test method for {@link PropertiesUtil#createPrefixMap(java.util.Map, java.lang.String)}.
     */
    @Test
    public void createPrefixMap() {
        Properties properties = new Properties();
        properties.put("abcde", "abcde");
        properties.put("abc0", "abc0");
        properties.put("abc", "abc");
        properties.put("ab", "ab");
        properties.put("abbde", "abbde");
        properties.put("abd", "abd");
        properties.put("bcdef", "bcdef");
        char[] array = "abc[]".toCharArray();
        properties.put(array, "abc[]");
        properties.put("abc[]", array);

        Map<String, String> answer = new HashMap<>();
        answer.put("de", "abcde");
        answer.put("0", "abc0");
        answer.put("", "abc");
        assertThat(PropertiesUtil.createPrefixMap(properties, "abc"), is(answer));
    }

    /**
     * Test method for {@link PropertiesUtil#removeKeyPrefix(java.util.Properties, java.lang.String)}.
     */
    @Test
    public void removeKeyPrefix() {
        Properties properties = new Properties();
        properties.put("abcde", "abcde");
        properties.put("abc0", "abc0");
        properties.put("abc", "abc");
        properties.put("ab", "ab");
        properties.put("abbde", "abbde");
        properties.put("abd", "abd");
        properties.put("bcdef", "bcdef");
        char[] array = "abc[]".toCharArray();
        properties.put(array, "abc[]");
        properties.put("abc[]", array);

        Properties answer = new Properties();
        answer.put("ab", "ab");
        answer.put("abbde", "abbde");
        answer.put("abd", "abd");
        answer.put("bcdef", "bcdef");
        answer.put(array, "abc[]");

        PropertiesUtil.removeKeyPrefix(properties, "abc");
        assertThat(properties, is(answer));
    }

    /**
     * Test method for {@link PropertiesUtil#checkAbsentKey(java.util.Properties, java.lang.String)}.
     */
    @Test
    public void checkAbsentKey() {
        Properties properties = new Properties();
        properties.put("abcde", "abcde");
        properties.put("abc0", "abc0");
        properties.put("ab", "ab");
        properties.put("abd", "ab");
        char[] array = "abc".toCharArray();
        properties.put(array, "abc");
        PropertiesUtil.checkAbsentKey(properties, "abc");
        try {
            properties.put("abc", "abc");
            PropertiesUtil.checkAbsentKey(properties, "abc");
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Test method for {@link PropertiesUtil#checkAbsentKeyPrefix(java.util.Properties, java.lang.String)}.
     */
    @Test
    public void testCheckAbsentKeyPrefix() {
        Properties properties = new Properties();
        properties.put("ab", "ab");
        properties.put("abbde", "abbde");
        properties.put("abd", "abd");
        properties.put("bcdef", "bcdef");
        char[] array = "abc[]".toCharArray();
        properties.put(array, "abc[]");
        PropertiesUtil.checkAbsentKeyPrefix(properties, "abc");
        try {
            properties.put("abc", "abc");
            PropertiesUtil.checkAbsentKeyPrefix(properties, "abc");
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
            properties.remove("abc");
        }
        try {
            properties.put("abcde", "abcde");
            PropertiesUtil.checkAbsentKeyPrefix(properties, "abc");
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
            properties.remove("abcde");
        }
    }
}
