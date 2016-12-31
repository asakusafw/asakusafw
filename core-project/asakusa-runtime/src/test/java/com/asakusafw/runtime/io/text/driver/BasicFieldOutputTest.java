/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * Test for {@link BasicFieldOutput}.
 */
public class BasicFieldOutputTest {

    private final BasicFieldOutput output = new BasicFieldOutput();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        output.put("Hello, world!");
        assertThat(output.getContent(), hasContent("Hello, world!"));
    }

    /**
     * put charsequence with range.
     */
    @Test
    public void put_range() {
        output.put("0123456789", 2, 7);
        assertThat(output.getContent(), hasContent("23456"));
    }

    /**
     * put null.
     */
    @Test
    public void put_null() {
        output.putNull();
        assertThat(output.getContent(), is(nullValue()));
    }

    /**
     * reset.
     */
    @Test
    public void reset() {
        output.put("first");
        output.reset();
        output.put("second");
        assertThat(output.getContent(), hasContent("second"));
    }

    /**
     * reset null output.
     */
    @Test
    public void reset_null() {
        output.putNull();
        output.reset();
        output.put("non-null");
        assertThat(output.getContent(), hasContent("non-null"));
    }

    /**
     * attempt to put content for null output.
     */
    @Test(expected = IllegalStateException.class)
    public void put_content_null() {
        output.putNull();
        output.put("INVALID");
    }

    /**
     * put null.
     */
    @Test(expected = IllegalStateException.class)
    public void put_null_not_empty() {
        output.put("Hello").putNull();
    }

    private static Matcher<CharSequence> hasContent(String value) {
        return new FeatureMatcher<CharSequence, String>(equalTo(value), "content", "content") {
            @Override
            protected String featureValueOf(CharSequence actual) {
                return actual == null ? null : actual.toString();
            }
        };
    }
}
