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
package com.asakusafw.runtime.io.json.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.value.ShortOption;

/**
 * Test for {@link ShortOptionPropertyAdapter}.
 */
public class ShortOptionPropertyAdapterTest {

    final ShortOptionPropertyAdapter.Builder builder = ShortOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        ShortOptionPropertyAdapter adapter = builder.build();
        ShortOption option = new ShortOption();

        adapter.read(new MockValue(100), option);
        assertThat(option, is(new ShortOption((short) 100)));
    }

    /**
     * read with overflow.
     * @throws Exception if failed
     */
    @Test(expected = ArithmeticException.class)
    public void read_overflow() throws Exception {
        ShortOptionPropertyAdapter adapter = builder.build();
        ShortOption option = new ShortOption();

        adapter.read(new MockValue(Short.MAX_VALUE + 1), option);
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        ShortOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new ShortOption((short) -100), writer);
        assertThat(writer.get(), is(-100));
    }
}
