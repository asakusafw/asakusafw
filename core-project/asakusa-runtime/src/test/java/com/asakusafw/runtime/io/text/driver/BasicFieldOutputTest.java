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

import java.util.Objects;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.runtime.io.text.driver.FieldOutput.Option;

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
        assertThat(output.get(), hasContent("Hello, world!"));
    }

    /**
     * put charsequence with range.
     */
    @Test
    public void put_range() {
        output.put("0123456789", 2, 7);
        assertThat(output.get(), hasContent("23456"));
    }

    /**
     * put null.
     */
    @Test
    public void put_null() {
        output.putNull();
        assertThat(output.get(), is(nullValue()));
    }

    /**
     * reset.
     */
    @Test
    public void reset() {
        output.put("first");
        output.reset();
        output.put("second");
        assertThat(output.get(), hasContent("second"));
    }

    /**
     * reset null output.
     */
    @Test
    public void reset_null() {
        output.putNull();
        output.reset();
        output.put("non-null");
        assertThat(output.get(), hasContent("non-null"));
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

    /**
     * w/ option.
     */
    @Test
    public void option() {
        output.addOption(new MockOption("Hello, world!"));
        assertThat(output.getOptions(), contains(new MockOption("Hello, world!")));
        assertThat(output.reset().getOptions(), hasSize(0));
    }

    /**
     * w/ options.
     */
    @Test
    public void options() {
        output.addOptions(new MockOption("A"), new MockOption("B"), new MockOption("C"));
        assertThat(output.getOptions(), containsInAnyOrder(
                new MockOption("A"), new MockOption("B"), new MockOption("C")));
        assertThat(output.reset().getOptions(), hasSize(0));
    }

    private static Matcher<CharSequence> hasContent(String value) {
        return new FeatureMatcher<CharSequence, String>(equalTo(value), "content", "content") {
            @Override
            protected String featureValueOf(CharSequence actual) {
                return actual == null ? null : actual.toString();
            }
        };
    }

    private static class MockOption implements Option {

        private final String value;

        MockOption(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(value);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MockOption other = (MockOption) obj;
            if (!Objects.equals(value, other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
