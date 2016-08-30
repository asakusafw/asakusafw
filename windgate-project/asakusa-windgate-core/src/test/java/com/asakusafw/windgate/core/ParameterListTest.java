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
package com.asakusafw.windgate.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link ParameterList}.
 */
public class ParameterListTest {

    /**
     * Simple testing.
     */
    @Test
    public void simple() {
        Map<String, String> map = new HashMap<>();
        map.put("message", "Hello, world!");
        ParameterList resolver = new ParameterList(map);
        assertThat(resolver.replace("${message}", true), is("Hello, world!"));
        assertThat(resolver.getPairs(), is(map));
    }

    /**
     * Replace a variable in line.
     */
    @Test
    public void inLine() {
        Map<String, String> map = new HashMap<>();
        map.put("message", "Hello, world!");
        ParameterList resolver = new ParameterList(map);
        assertThat(resolver.replace(">>> ${message} <<<", true), is(">>> Hello, world! <<<"));
    }

    /**
     * Replace multiple variables.
     */
    @Test
    public void multiple() {
        Map<String, String> map = new HashMap<>();
        map.put("m1", "Hello1");
        map.put("m2", "Hello2");
        map.put("m3", "Hello3");
        ParameterList resolver = new ParameterList(map);
        assertThat(resolver.replace("${m1}, ${m2}, ${m3}!", true), is("Hello1, Hello2, Hello3!"));
    }

    /**
     * present variable w/ default value.
     */
    @Test
    public void with_default_present() {
        Map<String, String> map = new HashMap<>();
        map.put("message", "Hello, world!");
        ParameterList resolver = new ParameterList(map);
        assertThat(resolver.replace("${message-MISSING}", true), is("Hello, world!"));
    }

    /**
     * absent variable w/ default value.
     */
    @Test
    public void with_default_missing() {
        Map<String, String> map = new HashMap<>();
        ParameterList resolver = new ParameterList(map);
        assertThat(resolver.replace("${message-MISSING}", true), is("MISSING"));
    }

    /**
     * Unknown variable.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_strict() {
        Map<String, String> map = new HashMap<>();
        ParameterList resolver = new ParameterList(map);
        resolver.replace("${MISSING}", true);
    }

    /**
     * Unknown variable.
     */
    @Test
    public void invalid_keep() {
        Map<String, String> map = new HashMap<>();
        ParameterList resolver = new ParameterList(map);
        assertThat(resolver.replace("${MISSING}", false), is("${MISSING}"));
    }
}
