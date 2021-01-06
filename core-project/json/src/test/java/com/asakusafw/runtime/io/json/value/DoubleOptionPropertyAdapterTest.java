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

import com.asakusafw.runtime.value.DoubleOption;

/**
 * Test for {@link DoubleOptionPropertyAdapter}.
 */
public class DoubleOptionPropertyAdapterTest {

    final DoubleOptionPropertyAdapter.Builder builder = DoubleOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        DoubleOptionPropertyAdapter adapter = builder.build();
        DoubleOption option = new DoubleOption();

        adapter.read(new MockValue(1d), option);
        assertThat(option, is(new DoubleOption(1d)));
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        DoubleOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new DoubleOption(2d), writer);
        assertThat(writer.get(), is(2d));
    }
}
