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
package com.asakusafw.yaess.core.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link PropertiesUtil}.
 */
public class PropertiesUtilTest {

    /**
     * Test method for {@link PropertiesUtil#getChildKeys(Map, String, String)}
     *
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @Test
    public void getChildKeys() {
        Properties properties = new Properties();
        properties.put("aaa", "");
        properties.put("aaa.bbb", "");
        properties.put("aaa.ccc.1", "");
        properties.put("aaa.ccc.2", "");
        properties.put("aaa.ddd", "");
        properties.put("aaa.ddd.1", "");
        properties.put("aab.1", "");
        properties.put("aa9.1", "");

        Set<String> keys = PropertiesUtil.getChildKeys(properties, "aaa.", ".");
        assertThat(keys.size(), is(3));
        assertThat(keys, hasItem("aaa.bbb"));
        assertThat(keys, hasItem("aaa.ccc"));
        assertThat(keys, hasItem("aaa.ddd"));
    }

    /**
     * Test method for {@link PropertiesUtil#createPrefixMap(Map, String)}.
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
}
