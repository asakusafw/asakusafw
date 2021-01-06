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
package com.asakusafw.runtime.io.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link InvertOrder}.
 */
public class InvertOrderTest extends WritableTestRoot {

    /**
     * fundamental testing.
     * @throws Exception if failed
     */
    @Test
    public void fundamental() throws Exception {
        IntOption entity = new IntOption(100);
        InvertOrder invert = new InvertOrder(entity);
        assertThat(invert.getEntity(), sameInstance((Object) entity));
    }

    /**
     * Serialization testing.
     * @throws Exception if failed
     */
    @Test
    public void serialize() throws Exception {
        IntOption entity = new IntOption(100);
        InvertOrder invert = new InvertOrder(entity);

        byte[] serialized = ser(invert);
        InvertOrder restored = des(new InvertOrder(new IntOption()), serialized);

        assertThat(restored.getEntity(), is((Object) entity));
        assertThat(restored.getEntity(), not(sameInstance((Object) entity)));
    }

    /**
     * Comparison testing.
     * @throws Exception if failed
     */
    @Test
    public void compare() throws Exception {
        InvertOrder a = new InvertOrder(new IntOption(100));
        InvertOrder b = new InvertOrder(new IntOption(101));
        InvertOrder c = new InvertOrder(new IntOption(100));

        assertThat(cmp(a, b), is(greaterThan(0)));
        assertThat(cmp(b, a), is(lessThan(0)));
        assertThat(cmp(a, c), is(equalTo(0)));
    }
}
