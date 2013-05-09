/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link PropertyName}.
 */
public class PropertyNameTest {

    /**
     * Simple testing.
     */
    @Test
    public void simple() {
        PropertyName name = PropertyName.newInstance("hello");
        assertThat(name, is(PropertyName.newInstance("hello")));
        assertThat(name, not(PropertyName.newInstance("world")));
        assertThat(name, not(PropertyName.newInstance("hello", "world")));
    }

    /**
     * Multiword.
     */
    @Test
    public void multi_word() {
        PropertyName name = PropertyName.newInstance("hello", "world");
        assertThat(name, not(PropertyName.newInstance("hello")));
        assertThat(name, not(PropertyName.newInstance("world")));
        assertThat(name, is(PropertyName.newInstance("hello", "world")));
        assertThat(name, not(PropertyName.newInstance("hello", "world", "hoge")));
    }

    /**
     * number after underscore.
     */
    @Test
    public void number_after_underscore() {
        PropertyName name = PropertyName.newInstance("hello", "0");
        assertThat(name, is(PropertyName.newInstance("hello", "0")));
        assertThat(name, is(PropertyName.newInstance("hello0")));
        assertThat(name, not(PropertyName.newInstance("hello", "1")));
        assertThat(name, not(PropertyName.newInstance("hello1")));
    }
}
